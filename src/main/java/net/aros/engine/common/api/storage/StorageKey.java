package net.aros.engine.common.api.storage;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

@SuppressWarnings("unused")
public record StorageKey<T>(ResourceLocation key) implements Comparable<StorageKey<?>> {
    public static final Codec<StorageKey<?>> CODEC = ResourceLocation.CODEC.xmap(StorageKey::new, StorageKey::key);
    public static final StreamCodec<RegistryFriendlyByteBuf, StorageKey<?>> STREAM_CODEC = ResourceLocation.STREAM_CODEC
            .map((Function<? super ResourceLocation, StorageKey<?>>) StorageKey::new, StorageKey::key).cast();

    @Override
    public @NotNull String toString() {
        return key.toString();
    }

    @Contract(" -> new")
    public @NotNull Component name() {
        return Component.translatable(key.toLanguageKey("storage").replace('/', '.'));
    }

    @Override
    public int compareTo(@NotNull StorageKey<?> o) {
        return key.compareTo(o.key);
    }
}
