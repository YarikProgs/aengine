package net.aros.engine.client;

import net.aros.engine.client.systems.ClientTickSystem;
import net.aros.engine.client.systems.UiSystem;
import net.aros.engine.common.storage.storage.AbstractStorage;
import net.aros.engine.common.storage.storage.StorageAccess;
import net.aros.engine.common.storage.storage.StorageKey;
import net.aros.engine.common.systems.StorageSystem;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.function.Supplier;

public class ClientEngine {
    private final List<ClientTickSystem> tickSystems = new ArrayList<>();
    private final Map<StorageKey<?>, AbstractStorage<?>> storages = new HashMap<>();
    private final Map<ResourceLocation, Supplier<Screen>> uis = new HashMap<>();

    public void initializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::clientTick);
    }

    public void register(Object system) {
        if (system instanceof ClientTickSystem tickSystem) {
            tickSystems.add(tickSystem);
        }
        if (system instanceof StorageSystem storageSystem) {
            registerStoragesFrom(storageSystem);
        }
        if (system instanceof UiSystem uiSystem) {
            registerUi(uiSystem);
        }
    }

    public void clientTick(Minecraft mc) {
        for (ClientTickSystem tickSystem : tickSystems) {
            tickSystem.tick(new ClientTickSystem.Context(mc, StorageAccess.wrap(storages)));
        }
    }

    private void registerStoragesFrom(@NotNull StorageSystem storageSystem) {
        var ctx = new StorageSystem.Context();
        storageSystem.onStorageRegistration(ctx);

        storages.putAll(ctx.getRegisteredStorages());
        for (AbstractStorage<?> storage : ctx.getRegisteredStorages().values()) {
            ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(storage.getReloadListener());
        }
    }

    private void registerUi(@NotNull UiSystem uiSystem) {
        var ctx = new UiSystem.Context();
        uiSystem.onUiRegister(ctx);

        uis.putAll(ctx.getRegisteredUis());
    }

    @Unmodifiable
    public Map<ResourceLocation, Supplier<Screen>> getUis() {
        return Collections.unmodifiableMap(uis);
    }
}
