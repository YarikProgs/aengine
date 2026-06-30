package net.aros.engine.client.ui;

import com.mojang.math.Axis;
import net.aros.engine.client.ArosEngineClient;
import net.aros.engine.client.ui.widets.IconButtonWidget;
import net.aros.engine.common.ArosEngine;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class EngineScreen extends Screen {
    public static final ResourceLocation ENGINE = ArosEngine.id("widget/engine");
    public static final WidgetSprites VALUES = new WidgetSprites(
            ArosEngine.id("widget/values"),
            ArosEngine.id("widget/values_disabled"),
            ArosEngine.id("widget/values_selected")
    );
    public static final WidgetSprites STORAGE = new WidgetSprites(
            ArosEngine.id("widget/storage"),
            ArosEngine.id("widget/storage_disabled"),
            ArosEngine.id("widget/storage_selected")
    );

    private static final int ICON_WIDTH = 64;
    private static final int ICON_HEIGHT = 64;
    private static final int DEFAULT_RADIUS = 100;
    private static final double WIND_SPEED = 0.002;
    private static final double WIND_AMPLITUDE = 2.5;
    private static final int WIRE_COLOR = 0xFFFF0000;

    private final List<IconButtonWidget> iconButtons = new ArrayList<>();
    private double[] iconButtonsRadi;
    private int selectedSector = -1;

    public EngineScreen() {
        super(Component.empty());
    }

    @Override
    protected void init() {
        super.init();

        ArosEngineClient.getEngine().getUis().forEach((location, factory) -> {
            createIconButton(new WidgetSprites(
                    location.withPrefix("widget/"),
                    location.withPrefix("widget/").withSuffix("_disabled"),
                    location.withPrefix("widget/").withSuffix("_selected")
            ), () -> {
                if (minecraft == null) return;
                Screen screen = factory.get();
                if (screen != null) minecraft.setScreen(screen);
            });
        });

        iconButtonsRadi = new double[iconButtons.size()];
    }

    private void createIconButton(WidgetSprites icon, Runnable onClick) {
        var instance = addRenderableWidget(new IconButtonWidget(0, 0, ICON_WIDTH, ICON_HEIGHT, icon, onClick));
        iconButtons.add(instance);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // TODO: click on icon button in selected sector
        if (selectedSector != -1) {
            iconButtons.get(selectedSector).onPress();
            return true;
        }
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        super.renderBackground(guiGraphics, i, j, f);
        drawBackground(guiGraphics, i, j, f);
    }

    private void drawBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int centerX = width / 2;
        int centerY = height / 2;

        selectedSector = getSelectedScreenSector(mouseX - centerX, mouseY - centerY);
        updateRadi();
        arrangeIconButtons(centerX, centerY, ICON_WIDTH, ICON_HEIGHT, WIND_SPEED, WIND_AMPLITUDE);
        drawConnections(graphics, centerX, centerY);
        graphics.blitSprite(ENGINE, centerX - ICON_WIDTH / 2, centerY - ICON_HEIGHT / 2, ICON_WIDTH, ICON_HEIGHT);
    }

    private void updateRadi() {
        for (int i = 0; i < iconButtons.size(); i++) {
            boolean hovered = selectedSector == i;
            iconButtons.get(i).setFocused(hovered);

            iconButtonsRadi[i] = Mth.lerp(0.2f, iconButtonsRadi[i], hovered
                    ? DEFAULT_RADIUS + 32
                    : DEFAULT_RADIUS
            );
        }
    }

    private int getSelectedScreenSector(float dx, float dy) {
        // Dead zone
        if (Math.sqrt(dx * dx + dy * dy) < ICON_WIDTH / 2f) return -1;

        double angle = Math.atan2(dy, dx);
        if (angle < 0) angle += 2 * Math.PI;

        double sectorSize = (2 * Math.PI) / iconButtons.size();
        return (int) Math.floor(angle / sectorSize + 0.5) % iconButtons.size();
    }

    @SuppressWarnings("SameParameterValue")
    private void arrangeIconButtons(int centerX, int centerY, int iconWidth, int iconHeight, double windSpeed,
                                    double windAmplitude) {
        int count = iconButtons.size();
        long time = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            var iconButton = iconButtons.get(i);

            double angle = (2 * Math.PI * i) / count /* + (3 * Math.PI) / 2*/;
            double phaseShift = i * 1.5;

            double baseX = centerX + iconButtonsRadi[i] * Math.cos(angle);
            double baseY = centerY + iconButtonsRadi[i] * Math.sin(angle);

            int x = (int) (baseX - (iconWidth / 2.0));
            int y = (int) (baseY - (iconHeight / 2.0));

            if (!iconButton.isHoveredOrFocused()) {
                x += (int) (windAmplitude * Math.sin(time * windSpeed + phaseShift));
                y += (int) (windAmplitude * Math.cos(time * windSpeed * 0.8 + phaseShift));
            }

            iconButton.setPosition(x, y);
        }
    }

    private void drawConnections(GuiGraphics graphics, int centerX, int centerY) {
        for (int i = 0; i < iconButtons.size(); i++) {
            IconButtonWidget iconButton = iconButtons.get(i);
            drawThickLine(
                    graphics,
                    centerX,
                    centerY,
                    iconButton.getX() + ICON_WIDTH / 2d,
                    iconButton.getY() + ICON_HEIGHT / 2d,
                    -1,
                    selectedSector == i ? 1 : 2, WIRE_COLOR
            );
        }
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
}
