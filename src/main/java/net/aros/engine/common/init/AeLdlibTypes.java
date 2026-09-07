package net.aros.engine.common.init;

import com.lowdragmc.lowdraglib2.syncdata.AccessorRegistries;
import com.lowdragmc.lowdraglib2.syncdata.accessor.direct.CustomDirectAccessor;
import net.aros.engine.common.api.storage.StorageKey;
import net.aros.engine.common.api.storage.metadata.StorageMetadata;
import net.aros.engine.common.api.value.Value;
import net.aros.engine.common.api.value.Values;
import net.aros.engine.common.storage.ContentUpdate;
import net.aros.engine.common.storage.InitialData;

public class AeLdlibTypes {
    @SuppressWarnings("unchecked")
    public static void init() {
        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder(StorageMetadata.class)
                .codec(StorageMetadata.CODEC)
                .streamCodec(StorageMetadata.STREAM_CODEC)
                .build());
        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder((Class<StorageKey<?>>) (Class<?>) StorageKey.class)
                .codec(StorageKey.CODEC)
                .streamCodec(StorageKey.STREAM_CODEC)
                .build());
        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder(InitialData.class)
                .codec(InitialData.CODEC)
                .streamCodec(InitialData.STREAM_CODEC)
                .build());
        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder((Class<ContentUpdate<?>>) (Class<?>) ContentUpdate.class)
                .codec(ContentUpdate.CODEC)
                .streamCodec(ContentUpdate.STREAM_CODEC)
                .build());
        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder((Class<Value<?>>) (Class<?>) Value.class)
                .codec(Value.CODEC)
                .streamCodec(Value.STREAM_CODEC)
                .build());
        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder(Values.class)
                .codec(Values.CODEC)
                .streamCodec(Values.STREAM_CODEC)
                .build());
    }
}
