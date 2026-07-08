package net.aros.examples.common.modules.txt;

import com.mojang.serialization.DataResult;
import net.aros.engine.common.api.storage.loader.AbstractResourceLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.nio.charset.Charset;

public class TxtLoader extends AbstractResourceLoader<Txt> {
    public TxtLoader(ResourceLocation key, int subdirectory) {
        super(key, "txts_" + subdirectory, "txt");
    }

    @Override
    public DataResult<Txt> parse(ResourceLocation path, Resource resource) throws Exception {
        try (var is = resource.open()) {
            return DataResult.success(new Txt(new String(is.readAllBytes(), Charset.defaultCharset())));
        }
    }
}
