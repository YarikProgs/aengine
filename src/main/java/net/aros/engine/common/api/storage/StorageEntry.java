package net.aros.engine.common.api.storage;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.aros.engine.common.api.value.Value;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public record StorageEntry<T>(ResourceLocation key, Value<T> value) {
    public static final Codec<StorageEntry<?>> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("key").forGetter(StorageEntry::key),
            Value.CODEC.fieldOf("value").forGetter(StorageEntry::value)
    ).apply(inst, StorageEntry::new));
    public static final Codec<Set<StorageEntry<?>>> SET_CODEC = CODEC.listOf().xmap(ImmutableSet::copyOf, ImmutableList::copyOf);

    public static final StreamCodec<RegistryFriendlyByteBuf, StorageEntry<?>> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, StorageEntry::key,
            Value.STREAM_CODEC, StorageEntry::value,
            StorageEntry::new
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, Set<StorageEntry<?>>> SET_STREAM_CODEC
            = ByteBufCodecs.collection(HashSet::new, StorageEntry.STREAM_CODEC);
}
