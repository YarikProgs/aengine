package net.aros.engine.common.api.storage.storage.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.aros.engine.common.api.storage.storage.StorageEntry;
import net.aros.engine.common.api.storage.storage.StorageKey;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Set;

public record ContentUpdate<T>(StorageKey<T> key, Set<StorageEntry<T>> entries) {
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static final Codec<ContentUpdate<?>> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            StorageKey.CODEC.fieldOf("key").forGetter(ContentUpdate::key),
            StorageEntry.SET_CODEC.fieldOf("entries").forGetter(ce -> (Set) ce.entries)
    ).apply(inst, (key, set) -> new ContentUpdate<>(key, (Set) set)));

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final StreamCodec<RegistryFriendlyByteBuf, ContentUpdate<?>> STREAM_CODEC = StreamCodec.composite(
            StorageKey.STREAM_CODEC, ContentUpdate::key,
            StorageEntry.SET_STREAM_CODEC, cu -> (Set) cu.entries,
            (key, entries) -> new ContentUpdate<>(key, (Set) entries)
    );
}
