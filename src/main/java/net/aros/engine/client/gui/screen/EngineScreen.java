package net.aros.engine.client.gui.screen;

import com.mojang.math.Axis;
import net.aros.engine.client.api.ModuleUIs;
import net.aros.engine.client.gui.screen.widets.IconButtonWidget;
import net.aros.engine.common.ArosEngine;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class EngineScreen extends Screen {
    public static final ResourceLocation ENGINE = ArosEngine.id("widget/engine");

    public static final int ICON_WIDTH = 64;
    public static final int ICON_HEIGHT = 64;
    private static final int DEFAULT_RADIUS = 100;
    private static final double WIND_SPEED = 0.002;
    private static final double WIND_AMPLITUDE = 2.5;
    private static final int ENABLED_WIRE_COLOR = 0xFFff0000;
    private static final int DISABLED_WIRE_COLOR = 0xFFffffff;
    private static final int DISABLED_WIRE_COLOR_2 = 0xFFffaaaa;
    private static final int SHAKE_DURATION_MS = 400;
    private static final double SHAKE_AMPLITUDE = 4.0;
    private static final double SHAKE_SPEED = 0.05;

    private IconButtonState[] states;
    private int selectedSector;

    public EngineScreen() {
        super(Component.empty());
    }

    @Override
    protected void init() {
        super.init();
        if (minecraft == null) return;

        var uiProviders = ModuleUIs.getProviders();

        int size = uiProviders.size();
        states = new IconButtonState[size];

        int index = 0;

        for (var entry : uiProviders.entrySet()) {
            var location = entry.getKey();
            var provider = entry.getValue();
            Screen screen = provider.createUI(minecraft.player);
            //noinspection DataFlowIssue
            createIconButton(new WidgetSprites(
                    location.withPrefix("widget/"),
                    null,
                    location.withPrefix("widget/").withSuffix("_selected")
            ), () -> {
                if (minecraft != null && screen != null) minecraft.setScreen(screen);
            }, index++, screen != null);
        }
    }

    private void createIconButton(WidgetSprites icon, Runnable onClick, int index, boolean enabled) {
        var instance = addRenderableWidget(new IconButtonWidget(0, 0, icon, onClick));
        IconButtonState state = new IconButtonState(instance);
        state.button.setLocked(!enabled);
        states[index] = state;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (selectedSector != -1) {
            if (states[selectedSector].button.isLocked()) {
                states[selectedSector].shakeTimestamp = System.currentTimeMillis();
                playLockedSound();
            } else {
                states[selectedSector].button.onPress();
            }
            return true;
        }
        return false;
    }

    private void playLockedSound() {
        if (minecraft == null || minecraft.player == null) return;
        minecraft.getSoundManager().play(SimpleSoundInstance
                .forUI(
                        SoundEvents.NOTE_BLOCK_IRON_XYLOPHONE.value(),
                        0.4f + (minecraft.player.getRandom().nextFloat() - 0.5f) / 2f
                )
        );
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics guiGraphics, int i, int j, float f) {
        super.renderBackground(guiGraphics, i, j, f);
        drawBackground(guiGraphics, i, j);
    }

    private void drawBackground(GuiGraphics graphics, int mouseX, int mouseY) {
        int centerX = width / 2;
        int centerY = height / 2;

        selectedSector = getSelectedScreenSector(mouseX - centerX, mouseY - centerY);
        updateRadi();
        arrangeIconButtons(centerX, centerY, ICON_WIDTH, ICON_HEIGHT, WIND_SPEED, WIND_AMPLITUDE);
        drawConnections(graphics, centerX, centerY);
        graphics.blitSprite(ENGINE, centerX - ICON_WIDTH / 2, centerY - ICON_HEIGHT / 2, ICON_WIDTH, ICON_HEIGHT);
    }

    private void updateRadi() {
        for (int i = 0; i < states.length; i++) {
            boolean hovered = selectedSector == i;
            var state = states[i];
            state.button.setFocused(hovered);

            state.radius = Mth.lerp(0.2f, state.radius, hovered
                    ? state.button.isLocked()
                      ? DEFAULT_RADIUS + 8
                      : DEFAULT_RADIUS + 32
                    : DEFAULT_RADIUS
            );
        }
    }

    private int getSelectedScreenSector(float dx, float dy) {
        // Dead zone
        if (Math.sqrt(dx * dx + dy * dy) < ICON_WIDTH / 2f) return -1;

        double angle = Math.atan2(dy, dx);
        if (angle < 0) angle += 2 * Math.PI;

        double sectorSize = (2 * Math.PI) / states.length;
        return (int) Math.floor(angle / sectorSize + 0.5) % states.length;
    }

    @SuppressWarnings("SameParameterValue")
    private void arrangeIconButtons(int centerX, int centerY, int iconWidth, int iconHeight, double windSpeed,
                                    double windAmplitude) {
        int count = states.length;
        long time = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            var state = states[i];

            double angle = (2 * Math.PI * i) / count /* + (3 * Math.PI) / 2*/;
            double phaseShift = i * 1.5;

            double baseX = centerX + state.radius * Math.cos(angle);
            double baseY = centerY + state.radius * Math.sin(angle);

            int x = (int) (baseX - (iconWidth / 2.0));
            int y = (int) (baseY - (iconHeight / 2.0));

            long timeSinceShake = time - state.shakeTimestamp;
            if (timeSinceShake < SHAKE_DURATION_MS) {
                double progress = (double) timeSinceShake / SHAKE_DURATION_MS;
                double currentAmplitude = SHAKE_AMPLITUDE * (1.0 - progress);
                x += (int) (currentAmplitude * Math.sin(timeSinceShake * SHAKE_SPEED));
                y += (int) (currentAmplitude * Math.sin(timeSinceShake * SHAKE_SPEED * 2));
            }
            if (!state.button.isHoveredOrFocused()) {
                x += (int) (windAmplitude * Math.sin(time * windSpeed + phaseShift));
                y += (int) (windAmplitude * Math.cos(time * windSpeed * 0.8 + phaseShift));
            }

            state.button.setPosition(x, y);
        }
    }

    private void drawConnections(GuiGraphics graphics, int centerX, int centerY) {
        for (int i = 0; i < states.length; i++) {
            IconButtonWidget iconButton = states[i].button;
            drawThickLine(
                    graphics,
                    centerX,
                    centerY,
                    iconButton.getX() + ICON_WIDTH / 2d,
                    iconButton.getY() + ICON_HEIGHT / 2d,
                    -1,
                    selectedSector == i ? 1 : 2,
                    getWireColor(i)
            );
        }
    }

    private int getWireColor(int index) {
        long timeSinceShake = System.currentTimeMillis() - states[index].shakeTimestamp;
        if (timeSinceShake < SHAKE_DURATION_MS) {
            float progress = timeSinceShake / (float) SHAKE_DURATION_MS;
            return getWireColor(progress);
        }

        return states[index].button.isLocked() ? DISABLED_WIRE_COLOR : ENABLED_WIRE_COLOR;
    }

    private static int getWireColor(float progress) {
        float factor = (float) Math.sin(progress * Math.PI);

        int r = (int) Mth.lerp(factor, DISABLED_WIRE_COLOR >> 16 & 0xFF, DISABLED_WIRE_COLOR_2 >> 16 & 0xFF);
        int g = (int) Mth.lerp(factor, DISABLED_WIRE_COLOR >> 8 & 0xFF, DISABLED_WIRE_COLOR_2 >> 8 & 0xFF);
        int b = (int) Mth.lerp(factor, DISABLED_WIRE_COLOR & 0xFF, DISABLED_WIRE_COLOR_2 & 0xFF);

        return (0xFF << 24) | (r << 16) | (g << 8) | b;
    }

    private static void drawThickLine(@NotNull GuiGraphics graphics, double x1, double y1, double x2, double y2, int z, int thickness, int color) {
        float dx = (float) (x2 - x1);
        float dy = (float) (y2 - y1);
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        float angle = (float) Math.atan2(dy, dx);
        int intLength = Math.round(length);

        graphics.pose().pushPose();
        graphics.pose().translate(x1, y1, 0);
        graphics.pose().mulPose(Axis.ZP.rotation(angle));
        graphics.fill(0, 0, intLength, thickness, z, color);
        graphics.pose().popPose();
    }

    private static class IconButtonState {
        public final IconButtonWidget button;
        public double radius;
        public long shakeTimestamp;

        public IconButtonState(IconButtonWidget button) {
            this.button = button;
        }
    }
}
