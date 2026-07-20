package net.aros.engine.client.gui.ui.util;

import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.SupplierDataSource;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.BindableValue;
import net.aros.engine.client.gui.ui.StorageUI;
import net.aros.engine.common.api.storage.StorageEntry;
import net.aros.engine.common.api.storage.StorageKey;
import net.aros.engine.common.api.storage.metadata.StorageMetadata;

import java.util.Set;

public abstract class StorageDependentUIElement extends UIElement {
    protected final BindableValue<StorageMetadata> storageMetadata = new BindableValue<>();
    protected final BindableValue<Set<StorageEntry<?>>> storageEntries = new BindableValue<>();
    protected StorageKey<?> selectedKey;
    protected boolean selectedIsClient;

    public StorageDependentUIElement(
            StorageUI.StorageInfo serverInfo,
            StorageUI.StorageInfo clientInfo
    ) {
        storageMetadata
                .bindObserver(this::onStorageMetadataChanged)
                .bindDataSource(SupplierDataSource.of(() -> (selectedIsClient ? clientInfo : serverInfo).metadata().get(selectedKey)), true);
        storageEntries
                .bindObserver(this::onStorageContentsChanged)
                .bindDataSource(SupplierDataSource.of(() -> (selectedIsClient ? clientInfo : serverInfo).contents().get(selectedKey)));
        addChildren(storageMetadata, storageEntries);
    }

    public void setSelectedStorage(StorageKey<?> key, boolean isClient) {
        selectedKey = key;
        selectedIsClient = isClient;
    }

    protected abstract void onStorageContentsChanged(Set<StorageEntry<?>> entries);

    protected abstract void onStorageMetadataChanged(StorageMetadata selectedStorage);
}
