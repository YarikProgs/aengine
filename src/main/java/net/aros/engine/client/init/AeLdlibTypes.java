package net.aros.engine.client.init;

import com.lowdragmc.lowdraglib2.syncdata.AccessorRegistries;
import com.lowdragmc.lowdraglib2.syncdata.accessor.direct.CustomDirectAccessor;
import net.aros.engine.common.api.storage.storage.StorageKey;
import net.aros.engine.common.api.storage.storage.metadata.InitialData;
import net.aros.engine.common.api.storage.storage.metadata.StorageMetadata;
import net.aros.engine.common.api.storage.storage.type.ContentUpdate;

public class AeLdlibTypes {
    public static void init() {
        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder(StorageMetadata.class)
                .codec(StorageMetadata.CODEC)
                .streamCodec(StorageMetadata.STREAM_CODEC)
                .build());
        //noinspection unchecked
        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder((Class<StorageKey<?>>) (Class<?>) StorageKey.class)
                .codec(StorageKey.CODEC)
                .streamCodec(StorageKey.STREAM_CODEC)
                .build());
        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder(InitialData.class)
                .codec(InitialData.CODEC)
                .streamCodec(InitialData.STREAM_CODEC)
                .build());
        //noinspection unchecked
        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder((Class<ContentUpdate<?>>) (Class<?>) ContentUpdate.class)
                .codec(ContentUpdate.CODEC)
                .streamCodec(ContentUpdate.STREAM_CODEC)
                .build());
    }
}
