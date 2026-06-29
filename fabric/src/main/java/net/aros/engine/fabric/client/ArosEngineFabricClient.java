package net.aros.engine.fabric.client;

import net.aros.engine.client.ArosEngineClient;
import net.fabricmc.api.ClientModInitializer;

public final class ArosEngineFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ArosEngineClient.init();
    }
}
