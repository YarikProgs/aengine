package net.aros.engine.common.api.storage.storage;

import net.minecraft.resources.ResourceLocation;

public record StorageEntry<T>(ResourceLocation key, T value) {
}
