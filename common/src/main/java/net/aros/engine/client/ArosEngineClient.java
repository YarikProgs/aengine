package net.aros.engine.client;

public final class ArosEngineClient {
    private static ClientEngine engine;

    public static void init() {
        engine = new ClientEngine();
        engine.initializeClient();
    }

    public static ClientEngine getEngine() {
        if (engine == null)
            throw new IllegalStateException("getEngine call too early");
        return engine;
    }
}
