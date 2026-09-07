package net.aros.examples.client;

import net.aros.engine.client.events.RegisterClientStorageEvent;
import net.aros.engine.common.ArosEngine;
import net.aros.engine.common.storage.AbstractStorage;
import net.aros.engine.common.storage.DirectStorage;
import net.aros.engine.common.storage.StorageKey;
import net.aros.examples.common.ArosEngineExamples;
import net.aros.examples.common.modules.txt.Txt;
import net.aros.examples.common.modules.txt.TxtLoader;
import net.neoforged.bus.api.IEventBus;
import org.jetbrains.annotations.NotNull;

public class ArosEngineClientExamples {
    public static final AbstractStorage<Txt> CLIENT_TXT_STORAGE = new DirectStorage<>(
            new StorageKey<>(ArosEngine.id("client_txt")),
            ArosEngineExamples.TXT_TYPE
    ).attachLoader(new TxtLoader(ArosEngine.id("client_dir_2"), 2));

    public ArosEngineClientExamples(@NotNull IEventBus bus) {
        bus.addListener(RegisterClientStorageEvent.class, event ->
            event.register(CLIENT_TXT_STORAGE)
        );
    }
}
