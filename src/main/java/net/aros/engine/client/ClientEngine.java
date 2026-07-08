package net.aros.engine.client;

import net.aros.engine.client.api.ui.ModuleUiProvider;
import net.aros.engine.client.systems.ClientTickSystem;
import net.aros.engine.client.systems.ModuleUiSystem;
import net.aros.engine.common.api.storage.storage.AbstractStorage;
import net.aros.engine.common.internal.managers.StorageManager;
import net.aros.engine.common.internal.systems.CustomStorageSystem;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.apache.commons.lang3.function.Consumers;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ClientEngine {
    private final List<ClientTickSystem> tickSystems = new ArrayList<>();

    private final Map<ResourceLocation, ModuleUiProvider> uiProviders = new HashMap<>();
    private final StorageManager storageManager = new StorageManager(Consumers.nop());

    public void initializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::clientTick);
    }

    public void register(Object system) {
        if (system instanceof ClientTickSystem tickSystem) {
            tickSystems.add(tickSystem);
        }
        if (system instanceof CustomStorageSystem customStorageSystem) {
            registerStoragesFrom(customStorageSystem);
        }
        if (system instanceof ModuleUiSystem moduleUiSystem) {
            registerUiProviders(moduleUiSystem);
        }
    }

    public void clientTick(Minecraft mc) {
        for (ClientTickSystem tickSystem : tickSystems) {
            tickSystem.tick(new ClientTickSystem.Context(mc, storageManager().storageAccess()));
        }
    }

    private void registerStoragesFrom(@NotNull CustomStorageSystem customStorageSystem) {
        var ctx = new CustomStorageSystem.Context();
        customStorageSystem.onStorageRegistration(ctx);

        storageManager().putAll(ctx.getRegisteredStorages());
        for (AbstractStorage<?> storage : ctx.getRegisteredStorages().values()) {
            storage.setMetadataDestination(storageManager::setMetadata);
            ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(storage.getReloadListener());
        }
    }

    private void registerUiProviders(@NotNull ModuleUiSystem moduleUiSystem) {
        var ctx = new ModuleUiSystem.Context();
        moduleUiSystem.onModuleUiRegister(ctx);
        uiProviders.putAll(ctx.getProviders());
    }

    public Map<ResourceLocation, ModuleUiProvider> getUiProviders() {
        return Collections.unmodifiableMap(uiProviders);
    }

    public StorageManager storageManager() {
        return storageManager;
    }
}
