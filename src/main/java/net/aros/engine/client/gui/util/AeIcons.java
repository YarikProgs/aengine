package net.aros.engine.client.gui.util;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import net.aros.engine.common.ArosEngine;
import net.aros.engine.common.api.storage.metadata.LoadStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class AeIcons {
    public static final IGuiTexture CHECK = SpriteTexture
            .of(ArosEngine.id("textures/gui/icon/check.png"))
            .setSprite(0, 0, 32, 32);
    public static final IGuiTexture MORE_HORIZ = SpriteTexture
            .of(ArosEngine.id("textures/gui/icon/more_horiz.png"))
            .setSprite(0, 0, 32, 32);
    public static final IGuiTexture ERROR = SpriteTexture
            .of(ArosEngine.id("textures/gui/icon/error.png"))
            .setSprite(0, 0, 32, 32);

    @Contract(pure = true)
    public static @NotNull IGuiTexture getIconForLoadStatus(@NotNull LoadStatus status) {
        return switch (status) {
            case OK -> CHECK;
            case RELOADING -> MORE_HORIZ;
            case FAIL -> ERROR;
        };
    }
}
