package net.aros.examples.client;

import net.aros.engine.client.ArosEngineClient;
import net.aros.engine.client.modules.StorageClientModule;

public class ArosEngineClientExamples {
    public ArosEngineClientExamples() {
        ArosEngineClient.getEngine().register(new StorageClientModule());
    }
}
