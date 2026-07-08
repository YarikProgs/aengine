package net.aros.engine.client.init;

import com.lowdragmc.lowdraglib2.syncdata.AccessorRegistries;
import com.lowdragmc.lowdraglib2.syncdata.accessor.direct.CustomDirectAccessor;
import net.aros.engine.common.api.storage.storage.metadata.StorageMetadataMap;

public class CustomLdlibTypes {
    public static void init() {
        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder(StorageMetadataMap.class)
                .codec(StorageMetadataMap.CODEC)
                .streamCodec(StorageMetadataMap.STREAM_CODEC)
                .build());
    }
}
