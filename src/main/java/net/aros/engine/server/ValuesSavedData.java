package net.aros.engine.server;

import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacket;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.lowdragmc.lowdraglib2.syncdata.rpc.RPCSender;
import com.mojang.logging.LogUtils;
import net.aros.engine.common.ArosEngine;
import net.aros.engine.common.api.value.Value;
import net.aros.engine.common.api.value.ValueType;
import net.aros.engine.common.api.value.Values;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ValuesSavedData extends SavedData {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Factory<ValuesSavedData> FACTORY = new Factory<>(ValuesSavedData::new, ValuesSavedData::readNbt, DataFixTypes.LEVEL);
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
    private static ValuesSavedData readNbt(@NotNull CompoundTag nbt, HolderLookup.Provider registryLookup) {
        var data = new ValuesSavedData();
        for (String key : nbt.getAllKeys()) {
            data.values.put(key, Value.CODEC.parse(NbtOps.INSTANCE, nbt.get(key)).getOrThrow());
        }
        return data;
    }

    public static @NotNull ValuesSavedData of(@NotNull MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, ArosEngine.MOD_ID);
    }

    @RPCPacket("get_value")
    public static void getValue(RPCSender sender, String receiver, String path) {
        ServerPlayer player = sender.asPlayer();
        if (player == null || !player.hasPermissions(2)) return;

        Value<?> value = of(player.server).values.get(path);
        RPCPacketDistributor.rpcToPlayer(player, receiver, value);
    }

    @RPCPacket("get_all_values")
    public static void getAllValues(RPCSender sender, String receiver) {
        ServerPlayer player = sender.asPlayer();
        if (player == null || !player.hasPermissions(2)) return;

        var state = of(player.server);
        RPCPacketDistributor.rpcToPlayer(player, receiver, new Values(state.values));
    }

    @RPCPacket("set_value")
    public static void setValue(RPCSender sender, String path, Value<?> value) {
        ServerPlayer player = sender.asPlayer();
        if (player == null || !player.hasPermissions(2)) return;

        var savedData = of(player.server);
        savedData.values.put(path, value);
        savedData.setDirty();
    }
}
