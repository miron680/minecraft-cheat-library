package i.chuppachups.reek.client.ui.panelgui;

import com.darkmagician6.eventapi.EventManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import i.chuppachups.reek.client.Heiligtum;
import i.chuppachups.reek.client.font.FontManager;
import i.chuppachups.reek.client.module.api.Category;
import i.chuppachups.reek.client.module.api.Module;
import i.chuppachups.reek.client.ui.panelgui.components.ModuleComponent;
import i.chuppachups.reek.client.util.impl.animation.AnimationUtil;
import i.chuppachups.reek.client.util.impl.animation.util.Easings;
import i.chuppachups.reek.client.util.impl.render.RenderUtil;

import java.awt.*;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @author chuppachups1337
 * @since 04/10/2025
 */

public class Panel extends Screen {

    private float x, y;
    private final float width = 820f;
    private final float height = 295f;
    private final Map<Category, List<ModuleComponent>> columns = new EnumMap<>(Category.class);
    private final SearchComponent search = new SearchComponent();
    private final AnimationUtil alphaAnimation = new AnimationUtil();
    private final double ANIM_DURATION_SECONDS = 0.2f;
    private final float SCROLL_SPEED = 15f;
    private final float SCROLLBAR_WIDTH = 2.0f;

    private final Map<Category, Float> targetScrollOffsets = new EnumMap<>(Category.class);
    private final Map<Category, Float> currentScrollOffsets = new EnumMap<>(Category.class);

    private final float SCROLL_LERP_FACTOR = 0.1f;

    public Panel(Component title) {
        super(title);
        EventManager.register(this);
        rebuildColumns();
        alphaAnimation.set(0.0D);
    }

    private void rebuildColumns() {
        columns.clear();
        targetScrollOffsets.clear();
        currentScrollOffsets.clear();
        List<Module> modules = Heiligtum.getInstance().getModuleManager().getModules();
        Category[] categories = {Category.COMBAT, Category.MOVEMENT, Category.VISUAL, Category.PLAYER, Category.MISC};
        for (Category c : categories) {
            List<ModuleComponent> list = modules.stream()
                    .filter(m -> m.getCategory() == c)
                    .map(ModuleComponent::new)
                    .collect(Collectors.toList());
            columns.put(c, list);
            targetScrollOffsets.put(c, 0f);
            currentScrollOffsets.put(c, 0f);
        }
    }

    @Override
    protected void init() {
        super.init();
        this.x = (minecraft.getWindow().getGuiScaledWidth() / 2f) - (width / 2f);
        this.y = (minecraft.getWindow().getGuiScaledHeight() / 2f) - (height / 2f);
        search.setX(x + (width / 2f) - 150f);
        search.setY(y + height + 8f);
        alphaAnimation.run(1.0D, ANIM_DURATION_SECONDS, Easings.CUBIC_OUT);
        for (Category c : targetScrollOffsets.keySet()) {
            targetScrollOffsets.put(c, 0f);
            currentScrollOffsets.put(c, 0f);
        }
    }

    private void updateSmoothScrolling(float partialTicks) {
        for (Category c : columns.keySet()) {
            float target = targetScrollOffsets.getOrDefault(c, 0f);
            float current = currentScrollOffsets.getOrDefault(c, 0f);

            if (Math.abs(target - current) < 0.01f) {
                current = target;
            } else {
                current += (target - current) * SCROLL_LERP_FACTOR * (1.0f - partialTicks);
            }
            currentScrollOffsets.put(c, current);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
        updateSmoothScrolling(partialTicks);

        float currentAlpha = alphaAnimation.get();
        if (currentAlpha <= 0.01f && (alphaAnimation.getToValue() == 0.0D || !alphaAnimation.isAlive())) return;

        this.x = (minecraft.getWindow().getGuiScaledWidth() / 2f) - (width / 2f);
        this.y = (minecraft.getWindow().getGuiScaledHeight() / 2f) - (height / 2f);

        int panelAlpha = (int) (currentAlpha * 230);
        int headerAlpha = (int) (currentAlpha * 255);
        int scrollbarColor = new Color(255, 255, 255, (int)(currentAlpha * 120)).getRGB();

        Category[] cats = {Category.COMBAT, Category.MOVEMENT, Category.VISUAL, Category.PLAYER, Category.MISC};
        float gutter = 5f;
        float colWidth = (width - gutter * (cats.length - 1) - 220) / cats.length;
        float startX = x + 20f;
        float headerY = y - 10f;

        for (int i = 0; i < cats.length; i++) {
            Category c = cats[i];
            float colX = startX + i * (colWidth + gutter);
            float listY = headerY + 22f;
            float listHeight = height - 70f;
            float contentStart = listY + 20f;
            float scrollAreaX1 = colX + 60f;
            float scrollAreaY1 = listY - 6f;
            float scrollAreaX2 = colX + 60f + colWidth;
            float scrollAreaY2 = listY - 6f + listHeight + 32f;
            float visibleModuleHeight = scrollAreaY2 - contentStart;

            RenderUtil.Blur.drawBlur(g, scrollAreaX1, scrollAreaY1, colWidth, listHeight + 32f, 6f, 10, -1);
            RenderUtil.drawRoundedRect(g, scrollAreaX1, scrollAreaY1, colWidth, listHeight + 32f, 6f, new Color(20, 16, 22, panelAlpha).getRGB());
            FontManager.SFPD.getSize(23f).drawCenteredString(g, c.getName(), colX + 115f, headerY + 28, new Color(220, 220, 220, headerAlpha).getRGB());

            List<ModuleComponent> comps = columns.getOrDefault(c, Collections.emptyList());
            float contentTotalHeight = 0;
            for (ModuleComponent comp : comps) {
                contentTotalHeight += comp.getAnimatedHeight() + 2;
            }

            float scrollOffset = currentScrollOffsets.getOrDefault(c, 0f);

            g.enableScissor((int) (scrollAreaX1), (int) (contentStart - 4), (int) (scrollAreaX2), (int) (scrollAreaY2));
            float curY = contentStart + scrollOffset;
            for (ModuleComponent comp : comps) {
                comp.setX(colX + 60f);
                comp.setY(curY);
                comp.setWidth(colWidth);
                comp.draw(g, mouseX, mouseY, partialTicks, currentAlpha);
                curY += comp.getAnimatedHeight() + 2;
            }
            g.disableScissor();

            float maxScroll = Math.max(0, contentTotalHeight - visibleModuleHeight);
            if (maxScroll > 0) {
                float trackHeight = visibleModuleHeight;
                float thumbHeight = Math.max(15.0f, trackHeight * (visibleModuleHeight / contentTotalHeight));
                float scrollPercentage = scrollOffset == 0 ? 0 : -scrollOffset / maxScroll;
                float thumbY = contentStart + (trackHeight - thumbHeight) * scrollPercentage;
                float scrollX = scrollAreaX2 - SCROLLBAR_WIDTH - 2f;

                RenderUtil.drawRoundedRect(g, scrollX, thumbY, SCROLLBAR_WIDTH, thumbHeight, 1.5f, scrollbarColor);
            }
        }

        search.setX(x + (width / 2f) - (search.getWidth() / 2f));
        search.setY(y + height + 8f);
        search.draw(g, mouseX, mouseY, partialTicks, currentAlpha);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalDelta, double verticalDelta) {
        if (!isInteractable()) return false;

        float listY_start = y - 10f + 22f - 6f;
        float listY_end = listY_start + height - 70f + 32f;

        if (mouseY >= listY_start && mouseY <= listY_end) {
            Category[] cats = {Category.COMBAT, Category.MOVEMENT, Category.VISUAL, Category.PLAYER, Category.MISC};
            float gutter = 5f;
            float colWidth = (width - gutter * (cats.length - 1) - 220) / cats.length;
            float startX = x + 20f;

            for (int i = 0; i < cats.length; i++) {
                Category c = cats[i];
                float colX_start = startX + i * (colWidth + gutter) + 60f;
                float colX_end = colX_start + colWidth;

                if (mouseX >= colX_start && mouseX <= colX_end) {
                    float targetOffset = targetScrollOffsets.getOrDefault(c, 0f);
                    targetOffset += verticalDelta * SCROLL_SPEED;

                    float contentTotalHeight = 0;
                    for (ModuleComponent comp : columns.getOrDefault(c, Collections.emptyList())) {
                        contentTotalHeight += comp.getAnimatedHeight() + 2;
                    }
                    float contentStart = y - 10f + 22f + 20f;
                    float scrollAreaY2 = y - 10f + 22f - 6f + height - 70f + 32f;
                    float visibleModuleHeight = scrollAreaY2 - contentStart;
                    float maxScroll = Math.max(0, contentTotalHeight - visibleModuleHeight);

                    targetOffset = Math.max(-maxScroll, Math.min(0, targetOffset));

                    targetScrollOffsets.put(c, targetOffset);
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void tick() {
        super.tick();
        alphaAnimation.update();
        if (alphaAnimation.getToValue() == 0.0D && !alphaAnimation.isAlive()) {
            minecraft.setScreen(null);
        }
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        float currentAlpha = alphaAnimation.get();
        int bgAlpha = (int) (currentAlpha * 200);
        RenderUtil.drawRoundedRect(g, 0, 0, minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight(), 0f, new Color(0, 0, 0, bgAlpha).getRGB());
    }

    private boolean isInteractable() {
        return alphaAnimation.get() > 0.9f && !alphaAnimation.isAlive();
    }

    @Override
    public boolean mouseClicked(double mxD, double myD, int button) {
        if (!isInteractable()) return false;
        if (search.mouseClicked((float) mxD, (float) myD, button)) return true;
        for (List<ModuleComponent> list : columns.values()) {
            for (ModuleComponent comp : list) {
                if (comp.mouseClicked((float) mxD, (float) myD, button)) return true;
            }
        }
        return super.mouseClicked(mxD, myD, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (!isInteractable()) return false;
        for (List<ModuleComponent> list : columns.values()) {
            for (ModuleComponent comp : list) {
                comp.mouseDragged(mouseX, mouseY, button);
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mxD, double myD, int button) {
        if (!isInteractable()) return false;
        float mx = (float) mxD;
        float my = (float) myD;
        for (List<ModuleComponent> list : columns.values()) {
            for (ModuleComponent comp : list) {
                comp.mouseReleased(mx, my, button);
            }
        }
        search.mouseReleased(mx, my, button);
        return super.mouseReleased(mxD, myD, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.startFadeOut();
            return true;
        }
        if (!isInteractable()) return false;
        if (search.keyPressed(keyCode, scanCode, modifiers)) return true;
        for (List<ModuleComponent> list : columns.values()) {
            for (ModuleComponent comp : list) {
                if (comp.keyPressed(keyCode, scanCode, modifiers)) return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!isInteractable()) return false;
        if (search.charTyped(codePoint, modifiers)) return true;
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void onClose() {
        startFadeOut();
    }

    public void startFadeOut() {
        alphaAnimation.run(0.0D, ANIM_DURATION_SECONDS, Easings.CUBIC_IN);
        EventManager.unregister(this);
    }

    public void shutDown() {
        startFadeOut();
    }
}