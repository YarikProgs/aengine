package net.aros.engine.common.init;

import net.aros.engine.common.ArosEngine;
import net.aros.engine.common.api.storage.type.StorageType;
import net.aros.engine.common.api.value.ValueType;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class AeRegistries {
    public static final ResourceKey<Registry<ValueType<?>>> VALUE_TYPE_KEY
            = ResourceKey.createRegistryKey(ArosEngine.id("value_type"));
    public static final Registry<ValueType<?>> VALUE_TYPE
            = FabricRegistryBuilder.createSimple(VALUE_TYPE_KEY).buildAndRegister();

    public static final ResourceKey<Registry<StorageType<?>>> STORAGE_TYPE_KEY
            = ResourceKey.createRegistryKey(ArosEngine.id("storage_type"));
    public static final Registry<StorageType<?>> STORAGE_TYPE
            = FabricRegistryBuilder.createSimple(STORAGE_TYPE_KEY).attribute(RegistryAttribute.SYNCED).buildAndRegister();

    public static void touch() {
    }
}
