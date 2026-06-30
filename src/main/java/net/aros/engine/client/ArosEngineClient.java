package net.aros.engine.client;

import net.aros.engine.client.dialogue.StorageClientSystem;
import net.aros.engine.client.keybind.AEngineKeyBinds;
import net.aros.engine.common.ArosEngine;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = ArosEngine.MOD_ID, dist = Dist.CLIENT)
public final class ArosEngineClient {
    private static ClientEngine engine;

    public ArosEngineClient() {
        AEngineKeyBinds.init();

        engine = new ClientEngine();
        engine.initializeClient();

        engine.register(new StorageClientSystem());
    }

    public static ClientEngine getEngine() {
        if (engine == null)
            throw new IllegalStateException("getEngine call too early");
        return engine;
    }
}
