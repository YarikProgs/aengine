package net.aros.engine.client.camera;

import net.aros.engine.client.api.camera.AeCameras;
import net.aros.engine.client.systems.ClientTickSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.jetbrains.annotations.NotNull;

public final class CameraClientSystem implements ClientTickSystem {
    public CameraClientSystem() {
        NeoForge.EVENT_BUS.addListener(LevelEvent.Unload.class, this::onLevelUnload);
    }

    @Override
    public void tick(@NotNull Context ctx) {
        AeCameras.tick(ctx.mc());
    }

    private void onLevelUnload(@NotNull LevelEvent.Unload event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (event.getLevel() instanceof ClientLevel && event.getLevel() == minecraft.level) {
            AeCameras.reset();
        }
    }
}
