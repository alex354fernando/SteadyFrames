package com.steadyframes;

import java.util.Arrays;

import net.minecraft.client.Minecraft;

/**
 * Mide frametimes y ajusta la distancia de render en pasos de 1 chunk para
 * mantener el frametime estable. No usa mixins: solo la API publica de opciones.
 * La distancia pedida al servidor NO se modifica (no se llama a broadcastOptions),
 * asi que los chunks siguen llegando; solo se limita cuantos se dibujan.
 */
public final class FrameGovernor {
    private static final int CAP = 4096;
    private static final long EVAL_NS = 2_000_000_000L;
    private static final long WARMUP_NS = 15_000_000_000L;
    private static final long FAIL_MEMORY_NS = 300_000_000_000L;

    private final float[] samples = new float[CAP];
    private final float[] scratch = new float[CAP];
    private int n = 0;

    private long lastFrame = 0;
    private long evalStart = 0;
    private long lastChange = 0;
    private Object lastLevel = null;

    private int badStreak = 0;
    private int goodStreak = 0;
    private int failedLevel = Integer.MAX_VALUE;
    private long failedUntil = 0;

    private volatile float lastAvgMs = 0;
    private volatile float lastP95Ms = 0;
    private volatile int lastDistance = 0;

    public void onFrame() {
        Minecraft mc = Minecraft.getInstance();
        long now = System.nanoTime();

        if (mc.level == null || mc.options == null) {
            lastFrame = now;
            lastLevel = null;
            return;
        }

        if (mc.level != lastLevel) { // mundo nuevo: periodo de calentamiento
            lastLevel = mc.level;
            lastChange = now;
            evalStart = now;
            n = 0;
            badStreak = goodStreak = 0;
        }

        long dt = now - lastFrame;
        lastFrame = now;

        if (!mc.isWindowActive() || dt <= 0 || dt > 500_000_000L) { // pausa/stall: no cuenta
            evalStart = now;
            n = 0;
            return;
        }

        if (n < CAP) {
            samples[n++] = dt / 1_000_000f;
        }

        if (now - evalStart < EVAL_NS) {
            return;
        }
        evalStart = now;
        evaluate(mc, now);
        n = 0;
    }

    private void evaluate(Minecraft mc, long now) {
        if (n < 30) return;

        System.arraycopy(samples, 0, scratch, 0, n);
        Arrays.sort(scratch, 0, n);
        float sum = 0;
        for (int i = 0; i < n; i++) sum += samples[i];
        float avg = sum / n;
        float p95 = scratch[Math.min(n - 1, (int) (n * 0.95f))];

        int cur = mc.options.renderDistance().get();
        lastAvgMs = avg;
        lastP95Ms = p95;
        lastDistance = cur;

        try {
            if (!Config.ENABLED.get()) return;

            float target = 1000f / Config.TARGET_FPS.get();
            int lo = Math.min(Config.MIN_DISTANCE.get(), Config.MAX_DISTANCE.get());
            int hi = Math.max(Config.MIN_DISTANCE.get(), Config.MAX_DISTANCE.get());
            long cooldown = Config.COOLDOWN_SECONDS.get() * 1_000_000_000L;

            if (p95 > target * 1.25f) {
                badStreak++;
                goodStreak = 0;
            } else if (p95 <= target * 1.10f) {
                goodStreak++;
                badStreak = 0;
            } else {
                badStreak = 0;
                goodStreak = 0;
            }

            if (now - lastChange < Math.max(cooldown, WARMUP_NS / 3)) return;

            if (badStreak >= 2 && cur > lo) {
                failedLevel = cur;
                failedUntil = now + FAIL_MEMORY_NS;
                apply(mc, cur - 1, now);
            } else if (goodStreak >= 5 && cur < hi) {
                int next = cur + 1;
                boolean blocked = now < failedUntil && next >= failedLevel;
                if (!blocked) apply(mc, next, now);
            }
        } catch (IllegalStateException ignored) {
            // La config aun no estaba cargada; se reintenta en la siguiente evaluacion.
        }
    }

    private void apply(Minecraft mc, int value, long now) {
        mc.options.renderDistance().set(value);
        lastDistance = value;
        lastChange = now;
        badStreak = 0;
        goodStreak = 0;
    }

    public void resetLearning() {
        failedLevel = Integer.MAX_VALUE;
        failedUntil = 0;
        badStreak = goodStreak = 0;
    }

    public String status() {
        float avg = lastAvgMs, p95 = lastP95Ms;
        return String.format(
                "[SteadyFrames] dist=%d chunks | media=%.1f ms (%.0f fps) | p95=%.1f ms (%.0f fps)",
                lastDistance, avg, avg > 0 ? 1000f / avg : 0f, p95, p95 > 0 ? 1000f / p95 : 0f);
    }
}
