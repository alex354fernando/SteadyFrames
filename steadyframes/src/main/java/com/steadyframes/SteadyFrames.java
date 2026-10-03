package com.steadyframes;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = SteadyFrames.MODID, dist = Dist.CLIENT)
public class SteadyFrames {
    public static final String MODID = "steadyframes";

    private static final FrameGovernor GOVERNOR = new FrameGovernor();

    public SteadyFrames(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
        NeoForge.EVENT_BUS.addListener(SteadyFrames::onFrame);
        NeoForge.EVENT_BUS.addListener(SteadyFrames::onCommands);
    }

    private static void onFrame(RenderFrameEvent.Post event) {
        GOVERNOR.onFrame();
    }

    private static void onCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("steadyframes")
                        .then(Commands.literal("status").executes(ctx -> {
                            String msg = GOVERNOR.status();
                            ctx.getSource().sendSuccess(() -> Component.literal(msg), false);
                            return 1;
                        }))
                        .then(Commands.literal("reset").executes(ctx -> {
                            GOVERNOR.resetLearning();
                            ctx.getSource().sendSuccess(
                                    () -> Component.literal("[SteadyFrames] Aprendizaje reiniciado."), false);
                            return 1;
                        })));
    }
}
