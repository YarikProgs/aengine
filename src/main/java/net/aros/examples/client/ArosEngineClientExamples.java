package net.aros.examples.client;

import net.aros.engine.client.ArosEngineClient;
import net.aros.examples.client.modules.ExampleClientModule;

public class ArosEngineClientExamples {
    public ArosEngineClientExamples() {
        ArosEngineClient.getEngine().register(new ExampleClientModule());
    }
}
