package i.chuppachups.reek.client.ui.display;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import i.chuppachups.reek.client.font.api.Font;
import i.chuppachups.reek.client.util.impl.animation.AnimationUtil;
import i.chuppachups.reek.client.util.impl.render.RenderUtil;
import i.chuppachups.reek.client.util.type.impl.IMinecraft;
import i.chuppachups.reek.client.util.type.impl.IRender;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Potion implements IMinecraft, IRender {

    private static final List<MobEffectInstance> potions = new ArrayList<>();
    private static final List<ResourceLocation> allPotionIcons = new ArrayList<>();
    public static float posX = 50;
    public static float posY = 50;
    public static float width = 70;
    public static float height = 20;
    public static boolean dragging = false;
    private static float dragOffsetX = 0;
    private static float dragOffsetY = 0;
    private static final AnimationUtil alphaAnimation = new AnimationUtil();
    private static boolean isVisible = false;

    static {
        alphaAnimation.set(0.0);
        // Заполняем список всеми иконками зелий для предварительного просмотра
        for (MobEffect effect : BuiltInRegistries.MOB_EFFECT) {
            String id = BuiltInRegistries.MOB_EFFECT.getKey(effect).getPath();
            allPotionIcons.add(new ResourceLocation("minecraft", "textures/mob_effect/" + id + ".png"));
        }
    }

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker, int scWidth, int scHeight) {
        updatePotionList();

        // Показываем, если есть зелья, или если мы в чате и зелий нет (для превью)
        boolean isInChatScreen = mc.screen instanceof ChatScreen;
        boolean shouldBeVisible = !potions.isEmpty() || (potions.isEmpty() && isInChatScreen);

        if (shouldBeVisible != isVisible) {
            isVisible = shouldBeVisible;
            if (isVisible) {
                alphaAnimation.run(255.0, 0.3f);
            } else {
                alphaAnimation.run(0.0, 0.3f);
            }
        }

        alphaAnimation.update();
        if (alphaAnimation.get() < 1.0) return;
        int alpha = Math.clamp((int) alphaAnimation.get(), 0, 255);
        if (alpha == 0 && !isVisible) return;

        Font headerFont = fontManager.SFPD.getSize(16.f);
        Font bodyFont = fontManager.SFPD.getSize(14.f);

        float padding = 3f;
        float headerHeight = headerFont.getHeight() + padding * 2;
        int headerBgColor = new Color(0, 0, 0, (int) (170 * (alpha / 255f))).hashCode();
        int bodyBgColor = new Color(0, 0, 0, (int) (170 * (alpha / 255f))).hashCode();
        int textColor = new Color(255, 255, 255, alpha).hashCode();

        RenderUtil.Blur.drawBlur(guiGraphics, posX, posY, width, headerHeight, 5, 10, -1);
        RenderUtil.drawRoundedRect(guiGraphics, posX, posY, width, headerHeight + 1, 5, headerBgColor);

        String headerText = "Active Potions";
        float headerTextWidth = headerFont.getStringWidth(headerText);
        fontManager.ICONS.getSize(18.f).drawString(guiGraphics, "E", posX + 4, posY + padding +0.5F, Color.white.getRGB());
    //    RenderUtil.drawImage(guiGraphics, new ResourceLocation("minecraft", "heiligtum/images/potion.png"), posX + 4, posY + padding - 1f, 12f, 12, Color.white.getRGB());
        headerFont.drawString(guiGraphics, headerText, posX + 20, posY + padding + 0.5f, textColor);

        float currentY = posY + headerHeight + padding + 0.7f;
        float maxWidth = headerTextWidth + padding * 2;

        // Если зелий нет и мы в чате, показываем превью
        if (potions.isEmpty() && isInChatScreen) {
            String potionName = "Preview";
            String level = "10";
            String duration = "**:**";
            float rowHeight = bodyFont.getHeight() + padding * 2 - 1;

            RenderUtil.Blur.drawBlur(guiGraphics, posX, currentY - padding, width, rowHeight, 5, 10, -1);
            RenderUtil.drawRoundedRect(guiGraphics, posX, currentY - padding, width, rowHeight, 5, bodyBgColor);
            RenderUtil.drawRoundedRect(guiGraphics, posX + 15, currentY - padding + 1.5f, 1, 11, 1, -1);

            // Меняем иконку каждую секунду
            if (!allPotionIcons.isEmpty()) {
                long timeInSeconds = System.currentTimeMillis() / 1000;
                int iconIndex = (int) (timeInSeconds % allPotionIcons.size());
                ResourceLocation icon = allPotionIcons.get(iconIndex);
                RenderUtil.drawImage(guiGraphics, icon, posX + padding + 1, currentY - 0.5f, 9, 9, Color.white.getRGB());
            }

            float textOffsetX = posX + padding + 16;
            bodyFont.drawString(guiGraphics, potionName, textOffsetX, currentY , textColor);
            textOffsetX += bodyFont.getStringWidth(potionName) + 4;
            bodyFont.drawString(guiGraphics, level, textOffsetX , currentY , new Color(235, 85, 105).getRGB());
            float durationWidth = bodyFont.getStringWidth(duration);
            float durationX = posX + width - durationWidth - padding;
            bodyFont.drawString(guiGraphics, duration, durationX, currentY + 0.5f, textColor);

            currentY += bodyFont.getHeight() + 5;
            float lineWidth = bodyFont.getStringWidth(potionName) + bodyFont.getStringWidth(level) + bodyFont.getStringWidth(duration) + 35;
            maxWidth = Math.max(maxWidth, lineWidth);
        } else {
            // В противном случае отображаем реальные зелья
            for (MobEffectInstance potion : potions) {
                String potionName = potion.getEffect().value().getDisplayName().getString();
                int level = potion.getAmplifier() + 1;
                String duration = formatDuration(potion.getDuration() / 20);
                float rowHeight = bodyFont.getHeight() + padding * 2 - 1;

                RenderUtil.Blur.drawBlur(guiGraphics, posX, currentY - padding, width, rowHeight, 5, 10, -1);
                RenderUtil.drawRoundedRect(guiGraphics, posX, currentY - padding, width, rowHeight, 5, bodyBgColor);
                RenderUtil.drawRoundedRect(guiGraphics, posX + 15, currentY - padding + 1.5f, 1, 11, 1, -1);

                String id = potion.getEffect().value().getDescriptionId().replace("effect.minecraft.", "");
                ResourceLocation icon = new ResourceLocation("minecraft", "textures/mob_effect/" + id + ".png");
                RenderUtil.drawImage(guiGraphics, icon, posX + padding + 1, currentY -0.5f, 9, 9, Color.white.getRGB());

                float textOffsetX = posX + padding + 16;
                bodyFont.drawString(guiGraphics, potionName, textOffsetX, currentY , textColor);
                textOffsetX += bodyFont.getStringWidth(potionName) + 4; // Исправлено: используется bodyFont для расчета ширины
                bodyFont.drawString(guiGraphics, "" + level, textOffsetX , currentY , new Color(235, 85, 105).getRGB());
                float durationWidth = bodyFont.getStringWidth(duration);
                float durationX = posX + width - durationWidth - padding;
                bodyFont.drawString(guiGraphics, duration, durationX, currentY -0.2f, textColor);

                currentY += bodyFont.getHeight() + 5;
                float lineWidth = bodyFont.getStringWidth(potionName) + bodyFont.getStringWidth("" + level) + bodyFont.getStringWidth(duration) + 30;
                maxWidth = Math.max(maxWidth, lineWidth);
            }
        }

        if (isVisible) {
            width = maxWidth;
            height = currentY - posY + padding;
        }
    }

    public static void handleMouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            Font font = fontManager.SFPD.getSize(16.f);
            float headerHeight = font.getHeight() + 6;
            if (mouseX >= posX && mouseX <= posX + width &&
                    mouseY >= posY && mouseY <= posY + headerHeight) {
                dragging = true;
                dragOffsetX = (float) (mouseX - posX);
                dragOffsetY = (float) (mouseY - posY);
            }
        }
    }

    public static void handleMouseReleased(int button) {
        if (button == 0) dragging = false;
    }

    public static void handleMouseDragged(double mouseX, double mouseY, int button) {
        if (button == 0 && dragging) {
            posX = (float) (mouseX - dragOffsetX);
            posY = (float) (mouseY - dragOffsetY);
        }
    }

    public static void updatePotionList() {
        potions.clear();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        potions.addAll(mc.player.getActiveEffects());
        potions.sort(Comparator.comparing(
                p -> p.getEffect().value().getDisplayName().getString().toLowerCase()
        ));
    }

    private static String formatDuration(int seconds) {
        int m = seconds / 60;
        int s = seconds % 60;
        return String.format("%d:%02d", m, s);
    }
}