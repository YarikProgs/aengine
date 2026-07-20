package net.aros.engine.common.api.storage;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.aros.engine.common.api.storage.type.StorageType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public record StorageEntry<T>(StorageType<T> type, ResourceLocation key, T value) {
    public static final Codec<StorageEntry<?>> CODEC = StorageType.CODEC.dispatch(StorageEntry::type, StorageEntry::codecFromType);
    public static final Codec<Set<StorageEntry<?>>> SET_CODEC = CODEC.listOf().xmap(ImmutableSet::copyOf, ImmutableList::copyOf);

    public static final StreamCodec<RegistryFriendlyByteBuf, StorageEntry<?>> STREAM_CODEC = StorageType.STREAM_CODEC
            .dispatch(StorageEntry::type, StorageEntry::streamCodecFromType);
    public static final StreamCodec<RegistryFriendlyByteBuf, Set<StorageEntry<?>>> SET_STREAM_CODEC
            = ByteBufCodecs.collection(HashSet::new, StorageEntry.STREAM_CODEC);

    private static <T> StreamCodec<RegistryFriendlyByteBuf, StorageEntry<T>> streamCodecFromType(StorageType<T> storageType) {
        return StreamCodec.composite(
                ResourceLocation.STREAM_CODEC, StorageEntry::key,
                storageType.streamCodec(), StorageEntry::value,
                (key, value) -> new StorageEntry<>(storageType, key, value)
        );
    }

    private static <T> MapCodec<StorageEntry<T>> codecFromType(StorageType<T> storageType) {
        return RecordCodecBuilder.mapCodec(inst -> inst.group(
                ResourceLocation.CODEC.fieldOf("key").forGetter(StorageEntry::key),
                storageType.codec().fieldOf("value").forGetter(StorageEntry::value)
        ).apply(inst, (key, value) -> new StorageEntry<>(storageType, key, value)));
    }
}
