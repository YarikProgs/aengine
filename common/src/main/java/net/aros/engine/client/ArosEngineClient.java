package net.aros.engine.client;

import net.aros.engine.client.dialogue.StorageClientSystem;
import net.aros.engine.client.keybind.AEngineKeyBinds;

public final class ArosEngineClient {
    private static ClientEngine engine;

    public static void init() {
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
