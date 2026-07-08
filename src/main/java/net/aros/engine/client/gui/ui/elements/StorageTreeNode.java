package net.aros.engine.client.gui.ui.elements;

import com.lowdragmc.lowdraglib2.gui.util.ITreeNode;
import net.aros.engine.common.api.storage.storage.StorageKey;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class StorageTreeNode implements ITreeNode<String, StorageKey<?>> {
    @Nullable
    public final StorageTreeNode parent;
    public final int dimension;
    private final String key;
    @Nullable
    private StorageKey<?> content;
    @Nullable
    private List<StorageTreeNode> children;
    @Nullable
    private Predicate<StorageTreeNode> valid;
    private boolean isClient;

    public StorageTreeNode(String key) {
        this(null, 0, key);
    }

    public StorageTreeNode(@Nullable StorageTreeNode parent, int dimension, String key) {
        this.parent = parent;
        this.dimension = dimension;
        this.key = key;
    }

    @Contract(value = "_, _, _ -> new", pure = true)
    private @NotNull StorageTreeNode createNode(@Nullable StorageTreeNode parent, int dimension, String key) {
        return new StorageTreeNode(parent, dimension, key);
    }

    @Override
    public int getDimension() {
        return dimension;
    }

    @Override
    public @NotNull String getKey() {
        return key;
    }

    @Override
    public @Nullable StorageKey<?> getContent() {
        return content;
    }

    @Override
    public @Nullable ITreeNode<String, StorageKey<?>> getParent() {
        return parent;
    }

    @Nonnull
    public List<? extends StorageTreeNode> getChildren() {
        if (children == null) return Collections.emptyList();
        if (valid == null) return children;
        return children.stream().filter(valid).collect(Collectors.toList());
    }

    public StorageTreeNode setValid(Predicate<StorageTreeNode> valid) {
        this.valid = valid;
        return this;
    }

    public StorageTreeNode getOrCreateChild(String childKey) {
        StorageTreeNode result;
        if (children != null) {
            result = children.stream().filter(child -> areKeysEqual(child.key, childKey)).findFirst().orElseGet(() -> {
                StorageTreeNode newNode = createNode(this, dimension + 1, childKey).setValid(valid);
                children.add(newNode);
                return newNode;
            });
        } else {
            children = new ArrayList<>();
            result = createNode(this, dimension + 1, childKey).setValid(valid);
            children.add(result);
        }
        return result;
    }

    public StorageTreeNode createChild(String childKey) {
        if (children == null) {
            children = new ArrayList<>();
        }
        StorageTreeNode result = createNode(this, dimension + 1, childKey).setValid(valid);
        children.add(result);
        return result;
    }

    public void setContent(@Nullable StorageKey<?> content) {
        this.content = content;
    }

    public StorageTreeNode setClient(boolean client) {
        isClient = client;
        return this;
    }

    public void removeChild(String key) {
        if (children != null) {
            for (StorageTreeNode child : children) {
                if (areKeysEqual(child.key, key)) {
                    children.remove(child);
                    return;
                }
            }
        }
    }

    public void removeChild(StorageTreeNode child) {
        if (children != null) {
            children.remove(child);
        }
    }

    public boolean areKeysEqual(String key1, String key2) {
        return Objects.equals(key1, key2);
    }

    public boolean areContentsEqual(StorageKey<?> content1, StorageKey<?> content2) {
        return Objects.equals(content1, content2);
    }

    @Override
    public String toString() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StorageTreeNode that = (StorageTreeNode) o;
        return dimension == that.dimension &&
                areKeysEqual(key, that.key) &&
                areContentsEqual(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dimension, key, content);
    }

    public void clearChildren() {
        if (children != null)
            children.clear();
    }

    public boolean isClient() {
        return isClient;
    }
}
