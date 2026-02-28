package i.chuppachups.reek.client.ui.panelgui;

import net.minecraft.client.gui.GuiGraphics;
import i.chuppachups.reek.client.util.impl.render.RenderUtil;
import i.chuppachups.reek.client.util.type.impl.IMinecraft;
import i.chuppachups.reek.client.util.type.impl.IRender;

import java.awt.*;

/**
 * @author chuppachups1337
 * @since 04/10/2025
 */

public class SearchComponent implements IMinecraft, IRender {

    private float x = 0, y = 0;
    private float width = 300f;
    private float height = 24f;
    private String text = "";
    private boolean focused = false;

    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }

    public void init() {
        text = "";
        focused = false;
    }

    /**
     * Отрисовка компонента. Принимает родительский alpha для анимации.
     * @param parentAlpha Текущее значение alpha (прозрачности) от родительской панели (0.0f до 1.0f).
     */

    public void draw(GuiGraphics g, float mouseX, float mouseY, float partialTicks, float parentAlpha) {

        int rectAlpha = (int) (parentAlpha * 230);
        int borderAlpha = (int) (parentAlpha * 12);
        int textAlpha = (int) (parentAlpha * 255);
        int bgColor = new Color(24, 18, 24, rectAlpha).getRGB();
        int borderColor = new Color(200, 200, 200, borderAlpha).getRGB();
        int textColor = new Color(180, 180, 180, textAlpha).getRGB();

        RenderUtil.drawRoundedRect(g, x, y, width, height, 6f, bgColor); // Используем 300x24 для соответствия объявленным width/height
        RenderUtil.drawRoundedRect(g, x, y, width, height, 6f, borderColor, 1f);

        String display = text.isEmpty() && !focused ? "Поиск" : text;
        fontManager.SFPD.getSize(14f).drawString(g, display, x + 10f, y + (height / 2f) - 6f, textColor);
    }


    public boolean mouseClicked(float mouseX, float mouseY, int button) {
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            focused = true;
            return true;
        } else {
            focused = false;
        }
        return false;
    }

    public boolean mouseReleased(float mouseX, float mouseY, int button) { return false; }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!focused) return false;
        if (keyCode == 257 || keyCode == 335) {
            focused = false;
            return true;
        }
        if (keyCode == 259) {
            if (!text.isEmpty()) text = text.substring(0, text.length() - 1);
            return true;
        }
        return false;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (!focused) return false;
        if (Character.isISOControl(codePoint)) return false;
        text += codePoint;
        return true;
    }

    public String getText() { return text; }
}