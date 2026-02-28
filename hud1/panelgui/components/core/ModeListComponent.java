package i.chuppachups.reek.client.ui.panelgui.components.core;

import net.minecraft.client.gui.GuiGraphics;
import i.chuppachups.reek.client.module.setting.impl.ModeListSetting;
import i.chuppachups.reek.client.util.impl.render.RenderUtil;

import java.awt.Color;
import java.util.List;

/**
 * @author chuppachups1337
 * @since 04/10/2025
 */
public class ModeListComponent extends SettingComponent<ModeListSetting> {

    private static final float PADDING_X = 10f;
    private static final float PADDING_Y = 1f;
    private static final float TEXT_HEIGHT = 10f;
    private static final float MODE_SPACING_X = PADDING_X * 0.8f;
    private static final float FONT_SIZE = 14f;
    private static final float MODE_FONT_SIZE = 13f;
    private static final float BACKGROUND_PADDING = 4f;

    public ModeListComponent(ModeListSetting setting) {
        super(setting);
        this.height = 15f + (setting.getAllModes().size() * 12f);
    }

    @Override
    public float getHeight() {
        int modeLines = calculateModeLines();
        float totalHeight = PADDING_Y + TEXT_HEIGHT + PADDING_Y;
        totalHeight += modeLines * TEXT_HEIGHT;
        totalHeight += (modeLines > 0 ? (modeLines - 1) * PADDING_Y : 0);
        totalHeight += PADDING_Y;
        return totalHeight;
    }

    private int calculateModeLines() {
        if (width <= 0) return 1;

        int lines = 1;
        float currentX = PADDING_X;
        List<String> modes = setting.getAllModes();

        for (String mode : modes) {
            float modeWidth = fontManager.SFPD.getSize(MODE_FONT_SIZE).getStringWidth(mode);
            float itemWidth = modeWidth + MODE_SPACING_X;

            if (currentX + itemWidth > width - PADDING_X) {
                lines++;
                currentX = PADDING_X + itemWidth;
            } else {
                currentX += itemWidth;
            }
        }
        return lines;
    }

    @Override
    public void draw(GuiGraphics g, float mouseX, float mouseY, float partialTicks, float parentAlpha) {
        int headerTextAlpha = (int) (parentAlpha * 255);
        int modeTextAlpha = (int) (parentAlpha * 180);

        String headerText = setting.getName();
        int headerColor = new Color(255, 255, 255, headerTextAlpha).getRGB();
        fontManager.SFPD.getSize(FONT_SIZE).drawString(g, headerText, x + PADDING_X, y + PADDING_Y , headerColor);
        fontManager.SFPD.getSize(FONT_SIZE).drawString(g,  setting.getSelectedModes().size() + " из " + setting.getAllModes().size(), x + PADDING_X + 76, y + PADDING_Y , headerColor);
        List<String> modes = setting.getAllModes();
        float currentX = x + PADDING_X;
        float currentY = y + TEXT_HEIGHT + PADDING_Y * 2;

        for (String mode : modes) {
            float modeWidth = fontManager.SFPD.getSize(MODE_FONT_SIZE).getStringWidth(mode);
            float itemWidth = modeWidth + MODE_SPACING_X;

            if (currentX + itemWidth > x + width - PADDING_X) {
                currentX = x + PADDING_X;
                currentY += TEXT_HEIGHT + PADDING_Y;
            }

            int textColor = setting.isModeSelected(mode)
                    ? new Color(255, 255, 255, modeTextAlpha).getRGB()
                    : new Color(180, 180, 180, modeTextAlpha).getRGB();

            if (setting.isModeSelected(mode)) {
                int backgroundColor = new Color(90, 36, 60, (int) (parentAlpha * 255)).getRGB();
                RenderUtil.drawRoundedRect(g, currentX - BACKGROUND_PADDING / 2, currentY - PADDING_Y + 2, modeWidth + BACKGROUND_PADDING + 2  , TEXT_HEIGHT, 2, backgroundColor);
            }

            fontManager.SFPD.getSize(MODE_FONT_SIZE).drawString(g, mode, currentX, currentY +2, textColor);
            currentX += itemWidth;
        }
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY, int button) {
        if (isHovered(mouseX, mouseY, x, y, width, getHeight())) {

            float modesYStart = y + TEXT_HEIGHT + PADDING_Y;

            if (mouseY < modesYStart) {
                return false;
            } else {
                List<String> modes = setting.getAllModes();
                float currentX = x + PADDING_X;
                float currentY = modesYStart + PADDING_Y;

                for (String mode : modes) {
                    float modeWidth = fontManager.SFPD.getSize(MODE_FONT_SIZE).getStringWidth(mode);
                    float itemWidth = modeWidth + MODE_SPACING_X;

                    if (currentX + itemWidth > x + width - PADDING_X) {
                        currentX = x + PADDING_X;
                        currentY += TEXT_HEIGHT + PADDING_Y;
                    }

                    if (isHovered(mouseX, mouseY, currentX, currentY - PADDING_Y + 2, modeWidth + BACKGROUND_PADDING, TEXT_HEIGHT)) {
                        setting.toggleMode(mode);
                        return true;
                    }

                    currentX += itemWidth;
                }
            }
        }
        return false;
    }

    public boolean isHovered(float mouseX, float mouseY, float x, float y, float w, float h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }
}
