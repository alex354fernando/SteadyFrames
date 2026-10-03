package com.steadyframes;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLED = B
            .comment("Activa la distancia de render adaptativa.")
            .define("adaptiveRenderDistance", true);

    public static final ModConfigSpec.IntValue TARGET_FPS = B
            .comment("FPS objetivo. Si el 95% de los frames no cumple este objetivo, se baja 1 chunk.")
            .defineInRange("targetFps", 60, 20, 360);

    public static final ModConfigSpec.IntValue MIN_DISTANCE = B
            .comment("Distancia de render minima que el mod permitira (en chunks).")
            .defineInRange("minRenderDistance", 16, 2, 32);

    public static final ModConfigSpec.IntValue MAX_DISTANCE = B
            .comment("Distancia de render maxima a la que el mod puede subir (en chunks).")
            .defineInRange("maxRenderDistance", 32, 2, 32);

    public static final ModConfigSpec.IntValue COOLDOWN_SECONDS = B
            .comment("Segundos minimos entre dos cambios de distancia (evita recargas constantes).")
            .defineInRange("cooldownSeconds", 10, 3, 120);

    public static final ModConfigSpec SPEC = B.build();

    private Config() {}
}
