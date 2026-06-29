package net.aros.engine.fabric;

import net.aros.engine.ArosEngine;
import net.fabricmc.api.ModInitializer;

public final class ArosEngineFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ArosEngine.init();
    }
}
