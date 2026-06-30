package net.aros.engine.common.storage.storage;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public record StorageKey<T>(ResourceLocation key) {
    @Override
    public @NotNull String toString() {
        return key.toString();
    }
}
