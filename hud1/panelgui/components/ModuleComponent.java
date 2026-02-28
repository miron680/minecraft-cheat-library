package i.chuppachups.reek.client.ui.panelgui.components;

import i.chuppachups.reek.client.module.setting.impl.*;
import i.chuppachups.reek.client.ui.panelgui.components.core.*;
import net.minecraft.client.gui.GuiGraphics;
import i.chuppachups.reek.client.Heiligtum;
import i.chuppachups.reek.client.module.api.Module;
import i.chuppachups.reek.client.module.setting.api.Setting;
import i.chuppachups.reek.client.util.impl.render.RenderUtil;
import i.chuppachups.reek.client.util.type.impl.IMinecraft;
import i.chuppachups.reek.client.util.type.impl.IRender;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chuppachups1337
 * @since 04/10/2025
 */


public class ModuleComponent implements IMinecraft, IRender {

    private final Module module;
    private final List<SettingComponent> settingComponents = new ArrayList<>();
    private float x, y, width;
    private final float height = 18f;
    private boolean binding = false;

    private boolean expanded = false;
    private float animatedHeight = height;

    public ModuleComponent(Module m) {
        this.module = m;
        List<Setting> settings = Heiligtum.getInstance().getSettingManager().getSettingsByModule(m);
        for (Setting setting : settings) {
            if (setting instanceof BooleanSetting) {
                settingComponents.add(new BooleanComponent((BooleanSetting) setting));
            } else if (setting instanceof NumberSetting) {
                settingComponents.add(new NumberComponent((NumberSetting) setting));
            } else if (setting instanceof ModeSetting) {
                settingComponents.add(new ModeComponent((ModeSetting) setting));
            } else if (setting instanceof ModeListSetting) {
                settingComponents.add(new ModeListComponent((ModeListSetting) setting));
            }
        }
    }

    /**
     * Отрисовка компонента. Принимает родительский alpha для анимации.
     * @param parentAlpha Текущее значение alpha (прозрачности) от родительской панели (0.0f до 1.0f).
     */

    public void draw(GuiGraphics g, float mouseX, float mouseY, float partialTicks, float parentAlpha) {
        float targetHeight = getTargetHeight();
        animatedHeight += (targetHeight - animatedHeight) * 0.1f * (1.0f - partialTicks);
        if (Math.abs(targetHeight - animatedHeight) < 0.1f) {
            animatedHeight = targetHeight;
        }

        int rectAlpha = (int) (parentAlpha * 170);
        int borderAlpha = (int) (parentAlpha * 45);
        int textAlpha = (int) (parentAlpha * 255);

        float animFactor = Math.min(1.0f, (animatedHeight - height) / (targetHeight - height));
        if (targetHeight == height) {
            animFactor = 0.0f;
        }

        float settingsAlpha = parentAlpha * animFactor;

        int baseBg = new Color(28, 22, 28, rectAlpha).getRGB();
        int toggledBg = new Color(60, 36, 60, rectAlpha).getRGB();
        int borderColor = new Color(255, 255, 255, borderAlpha).getRGB();
        int textColor = new Color(220, 220, 220, textAlpha).getRGB();
        int dotsColor = new Color(180, 180, 180, textAlpha).getRGB();
        int bg = module.isToggled() ? toggledBg : baseBg;

        RenderUtil.drawRoundedRectOutline(g, x + 6, y, width - 12, animatedHeight, 4f, 0.1f, borderColor, 1);
        RenderUtil.Blur.drawBlur(g, x + 6, y, width - 12, animatedHeight, 4f, 10, -1);
        RenderUtil.drawRoundedRect(g, x + 6, y, width - 12, animatedHeight, 4f, bg);

        var font = fontManager.SFPD.getSize(16f);
        font.drawString(g, module.getName(), x + 10f, y + 4f, textColor);

        if (!settingComponents.isEmpty()) {
            float nameWidth = font.getStringWidth(module.getName());
            float dotsX = x + 10f + nameWidth + 2f;
            fontManager.SFPD.getSize(19f).drawCenteredString(g, "...", x + 100, y + 6f, dotsColor);
        }

        if (animatedHeight > height + 1) {
            float settingY = y + height + -2;
            for (SettingComponent comp : settingComponents) {
                comp.setX(x);
                comp.setY(settingY);
                comp.setWidth(width);
                comp.draw(g, mouseX, mouseY, partialTicks, settingsAlpha);
                settingY += comp.getHeight();
            }
        }

        if (binding) {
            int overlayAlpha = (int) (parentAlpha * 120);
            RenderUtil.drawRoundedRect(g, 0, 0, mc.getWindow().getWidth(), mc.getWindow().getHeight(), 0f, new Color(0, 0, 0, overlayAlpha).getRGB());
            String text = "Press any key to bind...";
            var bindFont = fontManager.SFPD.getSize(25f);
            float textWidth = bindFont.getStringWidth(text);
            float centerX = (mc.getWindow().getGuiScaledWidth() - textWidth) / 2f;
            float centerY = (mc.getWindow().getGuiScaledHeight() - bindFont.getHeight()) / 2f;

            int bindTextColor = new Color(255, 255, 255, textAlpha).getRGB();
            bindFont.drawString(g, text, centerX, centerY, bindTextColor);
        }
    }

    public boolean mouseClicked(float mouseX, float mouseY, int button) {

        boolean hovered = isHovered(mouseX, mouseY, x, y, width, height);

        if (binding) return true;

        if (hovered) {
            if (button == 0) {
                module.toggle();
                return true;
            } else if (button == 1) {
                if (!settingComponents.isEmpty()) {
                    expanded = !expanded;
                }
                return true;
            } else if (button == 2) {
                binding = true;
                return true;
            }
        }

        if (expanded) {
            for (SettingComponent comp : settingComponents) {
                if (comp.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void mouseDragged(double mouseX, double mouseY, int button) {
        if (expanded) {
            for (SettingComponent comp : settingComponents) {
                comp.mouseDragged(mouseX, mouseY, button);
            }
        }
    }

    public boolean mouseReleased(float mouseX, float mouseY, int button) {
        if (expanded) {
            for (SettingComponent comp : settingComponents) {
                comp.mouseReleased(mouseX, mouseY, button);
            }
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (binding) {
            module.setBind(keyCode);
            binding = false;
            return true;
        }
        return false;
    }

    private float getTargetHeight() {
        float totalHeight = height;
        if (expanded) {
            for (SettingComponent comp : settingComponents) {
                totalHeight += comp.getHeight();
            }
        }
        return totalHeight;
    }

    private boolean isHovered(float mouseX, float mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public Module getModule() {
        return module;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setWidth(float w) {
        this.width = w;
    }

    public float getAnimatedHeight() {
        return animatedHeight;
    }
}