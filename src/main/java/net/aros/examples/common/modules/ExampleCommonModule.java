package net.aros.examples.common.modules;

import net.aros.engine.common.ArosEngine;
import net.aros.engine.common.api.storage.DirectStorage;
import net.aros.engine.common.api.storage.StorageKey;
import net.aros.engine.common.api.storage.type.StorageType;
import net.aros.engine.common.systems.CustomStorageSystem;
import net.aros.examples.common.modules.txt.Txt;
import net.aros.examples.common.modules.txt.TxtLoader;

public class ExampleCommonModule implements CustomStorageSystem {
    @Override
    public void onStorageRegistration(Context ctx) {
        var storage = ctx.registerStorage(new DirectStorage<>(
                new StorageKey<>(ArosEngine.id("txt")),
                ctx.registerStorageType(ArosEngine.id("txt"), new StorageType<>(Txt.CODEC, Txt.STREAM_CODEC, Txt.class))
        ));

        storage.attachLoader(new TxtLoader(ArosEngine.id("dir_0"), 0));
        storage.attachLoader(new TxtLoader(ArosEngine.id("dir_1"), 1));
    }
}
