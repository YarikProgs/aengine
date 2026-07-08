package net.aros.engine.client.gui.ui;

import com.lowdragmc.lowdraglib2.editor.ui.Editor;
import com.lowdragmc.lowdraglib2.editor.ui.View;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.SupplierDataSource;
import com.lowdragmc.lowdraglib2.gui.ui.elements.BindableValue;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacket;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.lowdragmc.lowdraglib2.syncdata.rpc.RPCSender;
import com.mojang.logging.LogUtils;
import net.aros.engine.client.ArosEngineClient;
import net.aros.engine.client.gui.ui.elements.StorageTreeList;
import net.aros.engine.client.gui.ui.elements.StorageViewer;
import net.aros.engine.common.api.storage.storage.StorageKey;
import net.aros.engine.common.api.storage.storage.metadata.StorageMetadataMap;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import static net.aros.engine.common.ArosEngine.translation;

public class StorageEditor extends Editor {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final StorageTreeList storageTreeList = new StorageTreeList();
    private final StorageViewer storageViewer = new StorageViewer();

    private StorageMetadataMap serverStoragesMetadata, clientStoragesMetadata;

    public StorageEditor() {
        menuContainer.removeSelf();
        bottomWindow.getParentWindow().removeSplitWindow(bottomWindow);
        historyView.removeSelf();
        inspectorView.removeSelf();

        placeView(createStorageListView(), () -> leftWindow.getLeftTop());
        placeView(createCurrentStorageView(), () -> centerWindow.getLeftTop());

        placeView(createLoadersView(), () -> rightWindow.getLeftTop());

        addChild(
                new BindableValue<StorageMetadataMap>()
                        .bindObserver(metadataMap -> {
                            storageTreeList.setClientData(metadataMap);
                            clientStoragesMetadata = metadataMap;
                        })
                        .bindDataSource(SupplierDataSource.of(()
                                -> ArosEngineClient.getEngine().storageManager().getStoragesMetadataMap()), true)
        );
        RPCPacketDistributor.rpcToServer("requestServerStoragesMetadata");
    }

    @Override
    protected @NotNull Editor createNewEditorInstance() {
        return new StorageEditor();
    }

    private @NotNull View createStorageListView() {
        var storagesView = new View();
        storagesView.setDynamicName(() -> translation("view", "storages"));

        storagesView.addChild(
                new ScrollerView()
                        .addScrollViewChild(
                                storageTreeList
                                        .setOnSelectedChanged(nodes -> {
                                            if (nodes.size() != 1) return;
                                            var node = nodes.iterator().next();
                                            if (node.getContent() == null) return;
                                            setCurrentStorage(node.getContent(), node.isClient());
                                        })
                        )
                        .layout(layout -> {
                            layout.widthPercent(100);
                            layout.heightPercent(100);
                        })
        );

        return storagesView;
    }

    private View createCurrentStorageView() {
        var contentView = new View();
        contentView.setDynamicName(() -> translation("view", "content"));
        contentView.addChild(storageViewer);
        return contentView;
    }

    private @NotNull View createLoadersView() {
        var loadersView = new View();
        loadersView.setDynamicName(() -> translation("view", "loaders"));
        loadersView.addChild(new Label().setText("Nobody here but us chickens!"));
        return loadersView;
    }

    protected void setCurrentStorage(StorageKey<?> key, boolean isClient) {
        storageViewer.setCurrentStorage(key, (isClient ? clientStoragesMetadata : serverStoragesMetadata).values().get(key));
    }

    @RPCPacket("setServerStoragesMetadata")
    public static void setServerStoragesMetadata(@NotNull RPCSender sender, StorageMetadataMap metadataMap) {
        if (sender.isRemote()) {
            LOGGER.error("Received 'setServerStorageMetadata' RPCPacket from remote");
            return;
        }
        if (!(Minecraft.getInstance().screen instanceof ModularUIScreen modularUIScreen)) {
            return;
        }
        if (!(modularUIScreen.modularUI.ui.rootElement instanceof StorageEditor storageEditor)) {
            return;
        }
        storageEditor.storageTreeList.setServerData(metadataMap);
        storageEditor.serverStoragesMetadata = metadataMap;
    }
}