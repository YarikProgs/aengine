package net.aros.examples.common.modules.txt;

import com.mojang.serialization.DataResult;
import net.aros.engine.common.api.storage.loader.AbstractResourceLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;
import java.nio.charset.Charset;

public class TxtLoader extends AbstractResourceLoader<Txt> {
    private final boolean isGood;

    public TxtLoader(ResourceLocation key, int subdirectory) {
        super(key, "txts_" + subdirectory, "txt");
        isGood = subdirectory % 2 == 0;
    }

    @Override
    public DataResult<Txt> parse(ResourceLocation path, Resource resource) throws Exception {
        if (!isGood) throw new IOException("Failed to... be good?");
        Thread.sleep(5000);
        try (var is = resource.open()) {
            return DataResult.success(new Txt(new String(is.readAllBytes(), Charset.defaultCharset())));
        }
    }
}
