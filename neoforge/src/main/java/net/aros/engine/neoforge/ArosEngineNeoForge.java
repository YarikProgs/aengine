package net.aros.engine.neoforge;

import net.aros.engine.ArosEngine;
import net.neoforged.fml.common.Mod;

@Mod(ArosEngine.MOD_ID)
public final class ArosEngineNeoForge {
    public ArosEngineNeoForge() {
        ArosEngine.init();
    }
}
