package net.aros.engine.common.config;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.Nest;
import io.wispforest.owo.config.annotation.SectionHeader;
import net.neoforged.fml.loading.FMLLoader;

import static net.aros.engine.common.ArosEngine.MOD_ID;

@Config(name = "aengine", wrapperName = "AeConfig")
@Modmenu(modId = MOD_ID)
@SuppressWarnings("unused")
public class AeConfigModel {
    @SectionHeader("common")
    @Nest
    public Debug debug = new Debug();

    @SectionHeader("client")
    @Nest
    public Colors colors = new Colors();
    @Nest
    public StorageUI storageUI = new StorageUI();

    public static class Debug {
        public boolean examplesEnabled = !FMLLoader.isProduction();
    }

    public static class StorageUI {
        public int okTimeThresholdMs = 2000;
        public int warnTimeThresholdMs = 10000;
    }

    public static class Colors {
        public String softGreen  = "#FFA8FCA4";
        public String softYellow = "#FFF3FCA4";
        public String softRed    = "#FFFCA4A4";
    }
}
