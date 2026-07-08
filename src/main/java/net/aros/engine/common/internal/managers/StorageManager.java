package net.aros.engine.common.internal.managers;

import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacket;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.lowdragmc.lowdraglib2.syncdata.rpc.RPCSender;
import net.aros.engine.common.ArosEngine;
import net.aros.engine.common.api.storage.storage.AbstractStorage;
import net.aros.engine.common.api.storage.storage.StorageAccess;
import net.aros.engine.common.api.storage.storage.StorageKey;
import net.aros.engine.common.api.storage.storage.metadata.StorageMetadata;
import net.aros.engine.common.api.storage.storage.metadata.StorageMetadataMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class StorageManager {
    private final Map<StorageKey<?>, AbstractStorage<?>> storages = new HashMap<>();
    private final Map<StorageKey<?>, StorageMetadata> storagesMetadata = new ConcurrentHashMap<>();
    private final Consumer<StorageMetadataMap> changeListener;

    private final StorageMetadataMap cachedMetadataMap = new StorageMetadataMap(Collections.unmodifiableMap(storagesMetadata));
    private final StorageAccess storageAccess = StorageAccess.wrap(storages);

    public StorageManager(Consumer<StorageMetadataMap> changeListener) {
        this.changeListener = changeListener;
    }

    public void setMetadata(StorageKey<?> key, StorageMetadata metadata) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(metadata);
        storagesMetadata.put(key, metadata);
        changeListener.accept(cachedMetadataMap);
    }

    public void putAll(Map<StorageKey<?>, AbstractStorage<?>> storages) {
        this.storages.putAll(storages);
    }

    public StorageAccess storageAccess() {
        return storageAccess;
    }

    public StorageMetadataMap getStoragesMetadataMap() {
        return cachedMetadataMap;
    }

    @RPCPacket("requestServerStoragesMetadata")
    public static void requestServerStoragesMetadata(RPCSender sender) {
        if (sender.isRemote()) {
            //noinspection DataFlowIssue
            RPCPacketDistributor.rpcToPlayer(sender.asPlayer(), "setServerStoragesMetadata", ArosEngine.getEngine().storageManager().cachedMetadataMap);
        }
    }
}
