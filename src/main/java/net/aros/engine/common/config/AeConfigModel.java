package net.aros.engine.common.config;

import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.*;
import net.neoforged.fml.loading.FMLLoader;

import static net.aros.engine.common.ArosEngine.MOD_ID;

@Config(name = "aengine", wrapperName = "AeConfig")
@Modmenu(modId = MOD_ID)
@SuppressWarnings("unused")
public class AeConfigModel {
    @SectionHeader("common")
    @Nest public Debug debug = new Debug();
    @Nest public Dialogue dialogue = new Dialogue();

    @SectionHeader("client")
    @Nest public Colors colors = new Colors();
    @Nest public StorageUI storageUI = new StorageUI();

    public static class Debug {
        @Sync(Option.SyncMode.OVERRIDE_CLIENT)
        @RestartRequired public boolean examplesEnabled = !FMLLoader.isProduction();
    }

    public static class Dialogue {
        /** Chars per second: server lower ack threshold and client opt-in auto-ack timer. */
        public int readingSpeedCps = 16;
        /** Upper ack threshold: extra milliseconds on top of the reading time. */
        public int maxAckWaitMs = 20000;
        /** Server wait for all clients to open the dialogue UI, milliseconds. */
        public int startAckTimeoutMs = 5000;
        /** Server wait for all clients to close the UI before the next dialogue, milliseconds. */
        public int endAckTimeoutMs = 5000;
        /** Disconnect hold: how long a member's place is kept (ТЗ п. 13), seconds. */
        public int disconnectHoldSeconds = 60;
        /** Default on_audience_change policy for dialogues that don't specify one. */
        public String defaultOnAudienceChange = "continue";
    }

    public static class StorageUI {
        public int okTimeThresholdMs = 2000;
        public int warnTimeThresholdMs = 10000;
    }

    public static class Colors {
        @RestartRequired public String softGreen  = "#FFA8FCA4";
        @RestartRequired public String softYellow = "#FFF3FCA4";
        @RestartRequired public String softRed    = "#FFFCA4A4";
    }
}
