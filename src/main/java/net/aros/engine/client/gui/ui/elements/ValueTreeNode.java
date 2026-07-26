package net.aros.engine.client.gui.ui.elements;

import com.lowdragmc.lowdraglib2.gui.util.TreeNode;
import org.jetbrains.annotations.Nullable;

public class ValueTreeNode extends TreeNode<String, String> {
    private int id;

    public ValueTreeNode(String key) {
        super(key);
    }

    public ValueTreeNode(@Nullable TreeNode<String, String> parent, int dimension, String key) {
        super(parent, dimension, key);
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void clearChildren() {
        if (children != null) children.clear();
    }

    public TreeNode<String, String> getOrCreateChild(String childKey, int id) {
        return ((ValueTreeNode) super.getOrCreateChild(childKey)).setId(id);
    }

    private ValueTreeNode setId(int id) {
        this.id = id;
        return this;
    }

    @Override
    protected TreeNode<String, String> createNode(@Nullable TreeNode<String, String> parent, int dimension, String key) {
        return new ValueTreeNode(parent, dimension, key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValueTreeNode that = (ValueTreeNode) o;
        return dimension == that.dimension &&
                areKeysEqual(key, that.getKey()) &&
                areContentsEqual(content, that.getContent()) &&
                id == that.id;
    }
}
