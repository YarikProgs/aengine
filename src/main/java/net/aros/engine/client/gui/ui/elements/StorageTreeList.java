package net.aros.engine.client.gui.ui.elements;

import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TreeList;
import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.aros.engine.client.gui.util.AeColors;
import net.aros.engine.client.gui.util.AeIcons;
import net.aros.engine.common.api.storage.StorageKey;
import net.aros.engine.common.api.storage.metadata.LoadStatus;
import net.aros.engine.common.api.storage.metadata.StorageMetadata;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StorageTreeList extends TreeList<StorageTreeNode> {
    private final StorageTreeNode storages = new StorageTreeNode("Storages");
    private final StorageTreeNode server = storages.createChild("Server");
    private final StorageTreeNode client = storages.createChild("Client").setClient(true);

    public StorageTreeList() {
        setNodeUISupplier(new StorageNodeUISupplier());
        setRoot(storages);
        setClickToExpand(true);
    }

    public void setServerData(Map<StorageKey<?>, StorageMetadata> metadataMap) {
        addDataInto(server, metadataMap);
    }

    public void setClientData(Map<StorageKey<?>, StorageMetadata> metadataMap) {
        addDataInto(client, metadataMap);
    }

    private void addDataInto(StorageTreeNode node, Map<StorageKey<?>, StorageMetadata> metadataMap) {
        node.clearChildren();
        addNodesInto(node, metadataMap);
        reloadList();
        expandAllNodesIf(storages, node_ -> !node_.getChildren().isEmpty());
    }

    private void addNodesInto(StorageTreeNode node, Map<StorageKey<?>, StorageMetadata> map) {
        List<StorageKey<?>> list = new ArrayList<>(map.keySet());
        list.sort(StorageKey::compareTo);
        for (var key : list) {
            String path = key.key().getPath();
            StorageTreeNode current = node;

            for (String part : path.split("/")) {
                current = current.getOrCreateChild(part);
            }
            current.setContent(key);
            if (current.parent != null) {
                current.setClient(current.parent.isClient());
                current.setStatus(map.get(key).loadStatus());
            }
        }
    }

    private static class StorageNodeUISupplier implements UIElementProvider<StorageTreeNode> {
        @Override
        public @NotNull UIElement createUI(@NotNull StorageTreeNode node) {
            var container = new UIElement().layout(layout -> {
                layout.flexDirection(FlexDirection.ROW);
                layout.gapAll(2);
                layout.height(10);
            }).addChildren();
            var icon = new UIElement().layout(layout -> {
                layout.setAspectRatio(1);
                layout.heightPercent(100);
            }).style(style -> style.backgroundTexture(node.getContent() == null ? Icons.FOLDER : Icons.RESOURCE));
            var label = new TextElement()
                    .textStyle(style -> style.textWrap(TextWrap.HOVER_ROLL).textAlignVertical(Vertical.CENTER))
                    .setText(node.getContent() == null ? Component.translatable(node.getKey()) : node.getContent().name())
                    .layout(layout -> {
                        layout.heightPercent(100);
                        layout.flex(1);
                    }).setOverflowVisible(false);
            var statusIcon = new UIElement().layout(layout -> {
                layout.setAspectRatio(1);
                layout.heightPercent(100);
            }).style(style -> {
                LoadStatus status = node.loadStatus();
                if (status != null) {
                    style.backgroundTexture(AeIcons.getIconForLoadStatus(status));
                    style.color(AeColors.getStatusColor(status));
                }
            });

            container.addChild(icon);
            container.addChild(label);
            if (node.getContent() != null) container.addChild(statusIcon);

            return container.layout(l -> l.flex(1));
        }
    }
}
