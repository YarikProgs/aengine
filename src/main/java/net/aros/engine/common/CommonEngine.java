package net.aros.engine.common;

import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import net.aros.engine.common.api.storage.storage.AbstractStorage;
import net.aros.engine.common.api.storage.value.ValueAccess;
import net.aros.engine.common.internal.managers.StorageManager;
import net.aros.engine.common.internal.systems.CustomStorageSystem;
import net.aros.engine.common.internal.systems.ServerTickSystem;
import net.aros.engine.server.StorageSavedData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CommonEngine {
    private final StorageManager storageManager = new StorageManager(map -> {
        if (ServerLifecycleHooks.getCurrentServer() != null)
            RPCPacketDistributor.rpcToAllPlayers("setServerStoragesMetadata", map);
    });
    private final List<ServerTickSystem> tickSystems = new ArrayList<>();
    private ValueAccess valueAccess;

    public void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(this::serverTick);
        ServerLifecycleEvents.SERVER_STARTED.register(this::serverStarted);
    }

    public void register(Object system) {
        if (system instanceof ServerTickSystem tickSystem) {
            tickSystems.add(tickSystem);
        }
        if (system instanceof CustomStorageSystem customStorageSystem) {
            registerStoragesFrom(customStorageSystem);
        }
    }

    private void serverStarted(MinecraftServer server) {
        valueAccess = ValueAccess.wrap(StorageSavedData.of(server));
    }

    public void serverTick(MinecraftServer server) {
        var ctx = new ServerTickSystem.Context(server, valueAccess, storageManager().storageAccess());
        for (ServerTickSystem tickSystem : tickSystems) {
            tickSystem.tick(ctx);
        }
    }

    private void registerStoragesFrom(@NotNull CustomStorageSystem customStorageSystem) {
        var ctx = new CustomStorageSystem.Context();
        customStorageSystem.onStorageRegistration(ctx);

        storageManager().putAll(ctx.getRegisteredStorages());
        for (AbstractStorage<?> storage : ctx.getRegisteredStorages().values()) {
            storage.setMetadataDestination(storageManager::setMetadata);
            ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(storage.getReloadListener());
        }
    }

    public ValueAccess values() {
        if (valueAccess == null) throw new IllegalStateException("Called values too early");
        return valueAccess;
    }

    public StorageManager storageManager() {
        return storageManager;
    }
}
