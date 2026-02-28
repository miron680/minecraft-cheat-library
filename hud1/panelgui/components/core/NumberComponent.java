package i.chuppachups.reek.client.ui.panelgui.components.core;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import i.chuppachups.reek.client.module.setting.impl.NumberSetting;
import i.chuppachups.reek.client.util.impl.render.RenderUtil;

import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author chuppachups1337
 * @since 04/10/2025
 */

public class NumberComponent extends SettingComponent<NumberSetting> {

    private boolean dragging = false;
    private float targetValue;
    private final float ANIMATION_SPEED = 0.8f;
    private final float SLIDER_HEIGHT = 4f;
    private final float CIRCLE_RADIUS = 4f;

    public NumberComponent(NumberSetting setting) {
        super(setting);
        this.height = 20f;
        this.targetValue = setting.getValue();
    }

    @Override
    public void draw(GuiGraphics g, float mouseX, float mouseY, float partialTicks, float parentAlpha) {

        float currentValue = setting.getValue();
        float interpolatedValue = Mth.lerp(ANIMATION_SPEED * partialTicks, currentValue, targetValue);
        setting.setValue(interpolatedValue);
        float currentValueForRender = setting.getValue();

        int baseAlpha = (int) (parentAlpha * 255);
        int sliderBgAlpha = (int) (parentAlpha * 45);
        int accentAlpha = (int) (parentAlpha * 255);
        int textAlpha = (int) (parentAlpha * 255);
        int valueAlpha = (int) (parentAlpha * 170);
        int textBgAlpha = (int) (parentAlpha * 120);

        float sliderX = x + 10;
        float sliderWidth = width - 20;
        float sliderY = y + 10;
        final float textHeight = 14f;

        String valueStr = new BigDecimal(currentValueForRender).setScale(2, RoundingMode.HALF_UP).toString();

        float valueWidth = fontManager.SFPD.getSize(textHeight).getStringWidth(valueStr);
        float valueX = x + width - valueWidth - 10;
        float textY = y - 1f;

        int nameColor = new Color(255, 255, 255, textAlpha).getRGB();
        int valueColor = new Color(180, 180, 180, textAlpha).getRGB();
        int filledColor1 = new Color(90, 36, 60, valueAlpha).getRGB();

        final float padding = 2f;

        RenderUtil.drawRoundedRect(g, valueX - padding, textY - padding +1, valueWidth + 2 * padding, textHeight -4, 1f, filledColor1);

        fontManager.SFPD.getSize(textHeight).drawString(g, setting.getName(), x + 10f, textY, nameColor);
        fontManager.SFPD.getSize(textHeight).drawString(g, valueStr, valueX, textY, valueColor);

        int bgColor = new Color(255, 255, 255, sliderBgAlpha).getRGB();
        RenderUtil.drawRoundedRectOutline(g, sliderX, sliderY -0.5f, sliderWidth, SLIDER_HEIGHT +1, 1f,0.1f, bgColor,1);

        float range = setting.getMax() - setting.getMin();
        float percentage = (currentValueForRender - setting.getMin()) / range;
        float filledWidth = sliderWidth * percentage;
        int filledColor = new Color(90, 36, 60, accentAlpha).getRGB();
        RenderUtil.drawRoundedRect(g, sliderX, sliderY, filledWidth, SLIDER_HEIGHT, 1f, filledColor);

        float circleCenterX = sliderX + filledWidth -4;
        float circleCenterY = sliderY + SLIDER_HEIGHT / 2f -4;

        int circleColor = new Color(255, 255, 255, baseAlpha).getRGB();
        RenderUtil.drawRoundedRect(g, circleCenterX, circleCenterY, 8, 8, 3, circleColor);
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY, int button) {
        float hitAreaX = x + 10 - CIRCLE_RADIUS;
        float hitAreaWidth = width - 20 + 2 * CIRCLE_RADIUS;
        float hitAreaY = y;
        float hitAreaHeight = height;

        if (button == 0 && isHovered(mouseX, mouseY, hitAreaX, hitAreaY, hitAreaWidth, hitAreaHeight)) {
            dragging = true;
            updateValue(mouseX);
            return true;
        }
        return false;
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button) {
        if (dragging && button == 0) {
            updateValue((float) mouseX);
        }
    }

    @Override
    public void mouseReleased(float mouseX, float mouseY, int button) {
        if (button == 0) {
            dragging = false;
        }
    }

    private void updateValue(float mouseX) {
        float sliderX = x + 10;
        float sliderWidth = width - 20;
        float min = setting.getMin();
        float max = setting.getMax();
        float percentage = Mth.clamp((mouseX - sliderX) / sliderWidth, 0f, 1f);
        float calculatedValue = min + (max - min) * percentage;
        float increment = setting.getIncrement();

        calculatedValue = Math.round(calculatedValue / increment) * increment;
        calculatedValue = Mth.clamp(calculatedValue, min, max);

        targetValue = calculatedValue;
    }
}