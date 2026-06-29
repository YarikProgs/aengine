package net.aros.engine.neoforge.client;

import net.aros.engine.client.ArosEngineClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

import static net.aros.engine.ArosEngine.MOD_ID;

@Mod(value = MOD_ID, dist = Dist.CLIENT)
public class ArosEngineNeoForgeClient {
    public ArosEngineNeoForgeClient() {
        ArosEngineClient.init();
    }
}
