package net.aros.examples.client.modules;

import net.aros.engine.common.ArosEngine;
import net.aros.engine.common.api.storage.storage.DirectStorage;
import net.aros.engine.common.api.storage.storage.StorageKey;
import net.aros.engine.common.systems.CustomStorageSystem;
import net.aros.examples.common.modules.txt.Txt;
import net.aros.examples.common.modules.txt.TxtLoader;

public class ExampleClientModule implements CustomStorageSystem {
    @Override
    public void onStorageRegistration(Context ctx) {
        var storage = ctx.registerStorage(new DirectStorage<>(
                new StorageKey<>(ArosEngine.id("client_txt")),
                ctx.getStorageType(ArosEngine.id("txt"), Txt.class).orElseThrow()
        ));

        storage.attachLoader(new TxtLoader(ArosEngine.id("client_dir_2"), 2));
    }
}
