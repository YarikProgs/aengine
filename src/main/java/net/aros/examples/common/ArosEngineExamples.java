package net.aros.examples.common;

import net.aros.engine.common.ArosEngine;
import net.aros.examples.common.modules.ExampleCommonModule;

public class ArosEngineExamples {
    public ArosEngineExamples() {
        ArosEngine.getEngine().register(new ExampleCommonModule());
    }
}
