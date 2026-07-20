package net.aros.engine.common.api.storage.metadata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.aros.engine.common.api.storage.StorageKey;
import net.aros.engine.common.api.storage.type.ContentUpdate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;

public record InitialData(Map<StorageKey<?>, StorageMetadata> metadataMap,
                          List<ContentUpdate<?>> contentsList) {

    public static final Codec<InitialData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.unboundedMap(StorageKey.CODEC, StorageMetadata.CODEC).fieldOf("metadataMap").forGetter(InitialData::metadataMap),
            ContentUpdate.CODEC.listOf().fieldOf("contentsList").forGetter(InitialData::contentsList)
    ).apply(inst, InitialData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, InitialData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map((IntFunction<Map<StorageKey<?>, StorageMetadata>>) HashMap::new, StorageKey.STREAM_CODEC, StorageMetadata.STREAM_CODEC),
            InitialData::metadataMap,
            ByteBufCodecs.collection((IntFunction<List<ContentUpdate<?>>>) ArrayList::new, ContentUpdate.STREAM_CODEC),
            InitialData::contentsList,
            InitialData::new
    );
}
