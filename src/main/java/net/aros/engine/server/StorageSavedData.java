package net.aros.engine.server;

import com.mojang.logging.LogUtils;
import net.aros.engine.common.ArosEngine;
import net.aros.engine.common.api.storage.value.Value;
import net.aros.engine.common.api.storage.value.ValueType;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class StorageSavedData extends SavedData {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Factory<StorageSavedData> FACTORY = new Factory<>(StorageSavedData::new, StorageSavedData::readNbt, DataFixTypes.LEVEL);
    private final Map<String, Value<?>> values = new HashMap<>();

    public <T> Optional<Value<T>> get(String key, ValueType<T> type) {
        if (!values.containsKey(key)) return Optional.empty();

        Value<?> value = values.get(key);
        if (!Objects.equals(type, value.type())) {
            LOGGER.error("Expected value '{}' to be {}, got {}", key, type, value.type());
            return Optional.empty();
        }

        //noinspection unchecked
        return Optional.of((Value<T>) value);
    }

    public <T> void set(String path, @NotNull Value<T> value) {
        values.put(path, Objects.requireNonNull(value));
    }

    public void clear(String path) {
        values.remove(path);
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        values.forEach((key, value) -> nbt.put(key, Value.CODEC.encodeStart(NbtOps.INSTANCE, value).getOrThrow()));
        return nbt;
    }

    @Contract("_, _ -> new")
    @NotNull
    private static StorageSavedData readNbt(@NotNull CompoundTag nbt, HolderLookup.Provider registryLookup) {
        var data = new StorageSavedData();
        for (String key : nbt.getAllKeys()) {
            data.values.put(key, Value.CODEC.parse(NbtOps.INSTANCE, nbt.get(key)).getOrThrow());
        }
        return data;
    }

    public static @NotNull StorageSavedData of(@NotNull MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, ArosEngine.MOD_ID);
    }
}
