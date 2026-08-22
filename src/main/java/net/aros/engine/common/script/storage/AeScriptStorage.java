package net.aros.engine.common.script.storage;

import net.aros.engine.common.api.script.AeScriptApi;
import net.aros.engine.common.api.script.compilation.AeScript;
import net.aros.engine.common.api.storage.DirectStorage;
import net.aros.engine.common.api.storage.StorageKey;
import net.aros.engine.common.init.AeValueTypes;

public class AeScriptStorage extends DirectStorage<AeScript> {
    public AeScriptStorage(StorageKey<AeScript> key) {
        super(key, AeValueTypes.SCRIPT);
    }

    @Override
    protected void beforeReload() {
        AeScriptApi.clearRegistrations();
    }

    @Override
    protected void afterReload() {
        AeScriptApi.runScripts(entrySet());
    }
}
