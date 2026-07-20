package net.aros.engine.client.gui.screen.widets;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class IconButtonWidget extends AbstractButton {
    private final WidgetSprites iconSprite;
    private final Runnable onPress;

    public IconButtonWidget(int x, int y, int width, int height, WidgetSprites iconSprite, Runnable onPress) {
        super(x, y, width, height, Component.empty());
        this.iconSprite = iconSprite;
        this.onPress = onPress;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        guiGraphics.blitSprite(iconSprite.get(active, isHoveredOrFocused()), getX(), getY(), getWidth(), getHeight());
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public void onPress() {
        this.playDownSound(Minecraft.getInstance().getSoundManager());
        onPress.run();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
