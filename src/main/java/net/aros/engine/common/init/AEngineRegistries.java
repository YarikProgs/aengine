package net.aros.engine.common.init;

import net.aros.engine.common.ArosEngine;
import net.aros.engine.common.api.storage.value.ValueType;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class AEngineRegistries {
    public static final ResourceKey<Registry<ValueType<?>>> VALUE_TYPE_KEY
            = ResourceKey.createRegistryKey(ArosEngine.id("value_type"));
    public static final Registry<ValueType<?>> VALUE_TYPE
            = FabricRegistryBuilder.createSimple(VALUE_TYPE_KEY).buildAndRegister();

    public static void touch() {
    }
}
