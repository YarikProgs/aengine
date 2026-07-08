package net.aros.examples.common.modules;

import net.aros.engine.common.ArosEngine;
import net.aros.engine.common.api.storage.storage.DirectStorage;
import net.aros.engine.common.api.storage.storage.StorageKey;
import net.aros.engine.common.internal.systems.CustomStorageSystem;
import net.aros.examples.common.modules.txt.Txt;
import net.aros.examples.common.modules.txt.TxtLoader;

public class ExampleCommonModule implements CustomStorageSystem {
    @Override
    public void onStorageRegistration(Context ctx) {
        var storage = ctx.registerStorage(new DirectStorage<Txt>(new StorageKey<>(ArosEngine.id("txt"))));

        storage.attachLoader(new TxtLoader(ArosEngine.id("dir_0"), 0));
        storage.attachLoader(new TxtLoader(ArosEngine.id("dir_1"), 1));
    }
}
