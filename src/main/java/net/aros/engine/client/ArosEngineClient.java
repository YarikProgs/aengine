package net.aros.engine.client;

import net.aros.engine.client.init.AeLdlibTypes;
import net.aros.engine.client.init.AeKeybinds;
import net.aros.engine.client.modules.StorageClientModule;
import net.aros.engine.common.ArosEngine;
import net.aros.examples.client.ArosEngineClientExamples;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = ArosEngine.MOD_ID, dist = Dist.CLIENT)
public final class ArosEngineClient {
    private static ClientEngine engine;

    public ArosEngineClient() {
        AeKeybinds.init();
        AeLdlibTypes.init();

        engine = new ClientEngine();
        engine.initializeClient();
        engine.register(new StorageClientModule());

        //noinspection ConstantValue TODO replace with config entry
        if (true) {
            new ArosEngineClientExamples();
        }
    }

    public static ClientEngine getEngine() {
        if (engine == null)
            throw new IllegalStateException("getEngine call too early");
        return engine;
    }
}
