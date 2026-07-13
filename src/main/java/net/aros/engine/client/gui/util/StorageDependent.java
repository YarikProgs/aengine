package net.aros.engine.client.gui.util;

import net.aros.engine.common.api.storage.storage.StorageKey;
import net.aros.engine.common.api.storage.storage.metadata.StorageMetadata;

public interface StorageDependent {
    default void setSelectedStorage(StorageKey<?> key, StorageMetadata metadata) {
        onSelectedStorageChanged(new SelectedStorage(key, metadata));
    }

    void onSelectedStorageChanged(SelectedStorage selectedStorage);

    record SelectedStorage(StorageKey<?> key, StorageMetadata metadata) {
    }
}
