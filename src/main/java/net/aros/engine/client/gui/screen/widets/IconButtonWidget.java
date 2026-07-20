package net.aros.engine.client.gui.screen.widets;

import com.mojang.blaze3d.systems.RenderSystem;
import net.aros.engine.client.gui.screen.EngineScreen;
import net.aros.engine.common.ArosEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class IconButtonWidget extends AbstractButton {
    private static final ResourceLocation DISABLED_FG = ArosEngine.id("widget/disabled_fg");
    private static final int WIDTH = EngineScreen.ICON_WIDTH;
    private static final int HEIGHT = EngineScreen.ICON_HEIGHT;
    private final WidgetSprites iconSprite;
    private final Runnable onPress;
    private boolean locked;

    public IconButtonWidget(int x, int y, WidgetSprites iconSprite, Runnable onPress) {
        super(x, y, WIDTH, HEIGHT, Component.empty());
        this.iconSprite = iconSprite;
        this.onPress = onPress;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int i, int j, float f) {
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        if (locked)
            guiGraphics.setColor(0.3F, 0.3F, 0.3F, 1.0F);
        else
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blitSprite(iconSprite.get(active, isHoveredOrFocused() && !locked), getX(), getY(), WIDTH, HEIGHT);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        if (locked)
            guiGraphics.blitSprite(DISABLED_FG, getX(), getY(), WIDTH, HEIGHT);
    }

    @Override
    public void onPress() {
        this.playDownSound(Minecraft.getInstance().getSoundManager());
        onPress.run();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isLocked() {
        return locked;
    }
}
