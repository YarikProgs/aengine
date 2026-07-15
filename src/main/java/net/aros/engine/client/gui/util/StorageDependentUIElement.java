package net.aros.engine.client.gui.util;

import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.SupplierDataSource;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.BindableValue;
import net.aros.engine.common.api.storage.storage.StorageKey;
import net.aros.engine.common.api.storage.storage.metadata.StorageMetadata;

import java.util.Map;

public abstract class StorageDependentUIElement extends UIElement {
    protected final BindableValue<StorageMetadata> storageMetadata = new BindableValue<>();
    protected StorageKey<?> selectedKey;
    protected boolean selectedIsClient;

    public StorageDependentUIElement(
            Map<StorageKey<?>, StorageMetadata> serverMetadata,
            Map<StorageKey<?>, StorageMetadata> clientMetadata
    ) {
        storageMetadata
                .bindObserver(metadata -> onSelectedStorageChanged(new SelectedStorage(selectedKey, metadata)))
                .bindDataSource(SupplierDataSource.of(() -> (selectedIsClient ? clientMetadata : serverMetadata).get(selectedKey)), true);
        addChild(storageMetadata);
    }

    public void setSelectedStorage(StorageKey<?> key, boolean isClient) {
        selectedKey = key;
        selectedIsClient = isClient;
    }

    protected abstract void onSelectedStorageChanged(SelectedStorage selectedStorage);

    protected record SelectedStorage(StorageKey<?> key, StorageMetadata metadata) {
    }
}
