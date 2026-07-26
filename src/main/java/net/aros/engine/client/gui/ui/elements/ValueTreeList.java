package net.aros.engine.client.gui.ui.elements;

import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TreeList;
import com.lowdragmc.lowdraglib2.gui.ui.utils.UIElementProvider;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.aros.engine.common.api.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ValueTreeList extends TreeList<ValueTreeNode> {
    private final ValueTreeNode root = new ValueTreeNode("root");

    public ValueTreeList() {
        setRoot(root);
        setNodeUISupplier(new ValueNodeUISupplier());
        setClickToExpand(true);
    }

    public void setValues(Map<String, Value<?>> values) {
        root.clearChildren();
        createNodes(root, values);
        reloadList();
        expandNode(root);
    }

    private static void createNodes(ValueTreeNode root, @NotNull Map<String, Value<?>> values) {
        List<String> keys = new ArrayList<>(values.keySet());
        keys.sort(String::compareTo);
        int counter = 1;
        for (var key : keys) {
            ValueTreeNode current = root;
            for (String part : key.split("/")) {
                current = (ValueTreeNode) current.getOrCreateChild(part, counter++);
            }
            current.setContent(key);
        }
    }

//    private @NotNull String getPrefix(ValueTreeNode node) {
//        StringBuilder builder = new StringBuilder();
//        ValueTreeNode current = node;
//        while (current != null && current != root) {
//            builder.insert(0, node.getKey() + "/");
//            current = (ValueTreeNode) current.getParent();
//        }
//        return builder.toString();
//    }
//
//    private static void createNewNode(@NotNull String prefix) {
//
//    }
//
//    private static void renameGroup(@NotNull String prefix) {
//
//    }
//
//    private static void removeGroup(@NotNull String prefix) {
//
//    }
//
//    private static void rename(@NotNull String path) {
//
//    }
//
//    private static void remove(@NotNull String path) {
//
//    }
//
//    private static void showMenu(UIEvent event, TreeBuilder.Menu menu) {
//        if (menu.isEmpty()) return;
//        var mui = event.currentElement.getModularUI();
//        if (mui == null) return;
//        var root = mui.ui.rootElement;
//        var layoutOffset = root.worldToLocalLayoutOffset(new Vector2f(event.x, event.y));
//        root.addChild(new com.lowdragmc.lowdraglib2.gui.ui.elements.Menu<>(menu.build(), TreeBuilder.Menu::uiProvider)
//                .setHoverTextureProvider(TreeBuilder.Menu::hoverTextureProvider)
//                .setOnNodeClicked(TreeBuilder.Menu::handle)
//                .layout(layout -> {
//                    layout.left(layoutOffset.x);
//                    layout.top(layoutOffset.y);
//                }));
//    }

    private static class ValueNodeUISupplier implements UIElementProvider<ValueTreeNode> {

        @Override
        public @NotNull UIElement createUI(@NotNull ValueTreeNode node) {
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
                    .setText(node.getKey())
                    .layout(layout -> {
                        layout.heightPercent(100);
                        layout.flex(1);
                    }).setOverflowVisible(false);

            container.addChild(icon);
            container.addChild(label);

//            container.addEventListener(UIEvents.CLICK, event -> nodeClick(event, node));

            return container.layout(l -> l.flex(1));
        }

        // todo: menu on click. not mvp on 26.07.26 :(
//        private void nodeClick(UIEvent event, @NotNull ValueTreeNode node) {
//            if (event.button != GLFW.GLFW_MOUSE_BUTTON_RIGHT) return;
//
//            var menu = TreeBuilder.Menu.start();
//
//            if (node.getContent() == null) {
//                menu
//                        .leaf("create_new", () -> createNewNode(getPrefix(node)))
//                        .leaf("rename", () -> renameGroup(getPrefix(node)))
//                        .leaf("remove", () -> removeGroup(getPrefix(node)));
//            } else {
//                menu
//                        .leaf("rename", () -> rename(node.getContent()))
//                        .leaf("remove", () -> remove(node.getContent()));
//            }
//
//            showMenu(event, menu);
//        }
    }
}
