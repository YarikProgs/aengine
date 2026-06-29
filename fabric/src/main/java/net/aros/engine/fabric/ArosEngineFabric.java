package net.aros.engine.fabric;

import net.aros.engine.common.ArosEngine;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

public final class ArosEngineFabric implements ModInitializer {
    public static MinecraftServer currentServer;

    @Override
    public void onInitialize() {
        ArosEngine.init();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> currentServer = server);
        ServerLifecycleEvents.SERVER_STOPPED.register(__ -> currentServer = null);
    }
}
