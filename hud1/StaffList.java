package i.chuppachups.reek.client.ui.display;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen; // <-- ДОБАВЛЕН ИМПОРТ
import net.minecraft.resources.ResourceLocation;
import i.chuppachups.reek.client.Client;
import i.chuppachups.reek.client.font.api.Font;
import i.chuppachups.reek.client.util.impl.render.RenderUtil;
import i.chuppachups.reek.client.util.impl.animation.AnimationUtil;
import i.chuppachups.reek.client.util.type.impl.IMinecraft;
import i.chuppachups.reek.client.util.type.impl.IRender;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class StaffList implements IMinecraft, IRender {

    public static final List<Staff> staffPlayers = new ArrayList<>();
    public static final Pattern namePattern = Pattern.compile("^\\w{3,16}$");
    public static final Pattern prefixMatches = Pattern.compile(".*(mod|der|adm|help|wne|хелп|адм|поддержка|кура|own|staf|curat|dev|supp|yt|гл.мод|мл.мод|мл.сотруд|ст.сотруд|стажёр|стажер|сотруд).*");

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
    }

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker, int scWidth, int scHeight) {
        updateStaffList();

        boolean shouldBeVisible = !staffPlayers.isEmpty() || (mc.screen instanceof ChatScreen);

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

        Font font = fontManager.SFPD.getSize(16.f);
        float padding = 3f;
        float circleRadius = 2f;
        float headerHeight = font.getHeight() + padding * 2;
        int headerBgColor = new Color(0, 0, 0, (int) (170 * (alpha / 255f))).hashCode();
        int bodyBgColor = new Color(0, 0, 0, (int) (170 * (alpha / 255f))).hashCode();
        int textColor = new Color(255, 255, 255, alpha).hashCode();

        RenderUtil.Blur.drawBlur(guiGraphics, posX, posY, width, headerHeight, 5, 10, -1);
        RenderUtil.drawRoundedRect(guiGraphics, posX, posY, width, headerHeight, 5, headerBgColor);

        String headerText = "StaffList";
        float headerTextWidth = font.getStringWidth(headerText);
        RenderUtil.drawImage(guiGraphics, new ResourceLocation(Client.NAMESPACE + "/images/overlay/staff/staff.png"), posX + 4, posY + padding - 1f, 12f, 12, Color.white.getRGB());
        font.drawString(guiGraphics, headerText, posX + width / 2 - headerTextWidth / 2f - 1, posY + padding - 0.1f, textColor);

        float currentY = posY + headerHeight + padding;
        float maxWidth = headerTextWidth + padding * 2;

        if (staffPlayers.isEmpty() && mc.screen instanceof ChatScreen) {
            String prefixText = "§cADMIN";
            String nameText = "chuppachups";
            float rowHeight = font.getHeight() + padding * 2 - 2;
            RenderUtil.Blur.drawBlur(guiGraphics, posX, currentY - padding, width, rowHeight, 5, 10, -1);
            RenderUtil.drawRoundedRect(guiGraphics, posX, currentY - padding, width, rowHeight, 5, bodyBgColor);

            long timeInSeconds = System.currentTimeMillis() / 800;
            int baseCircleColor = (timeInSeconds % 2 == 0) ? Color.GREEN.getRGB() : Color.RED.getRGB(); // Зеленый/Красный
            int circleColor = new Color((baseCircleColor >> 16) & 0xFF, (baseCircleColor >> 8) & 0xFF, baseCircleColor & 0xFF, alpha).hashCode();
            RenderUtil.drawRoundedRect(guiGraphics, posX + padding + 1, currentY + font.getHeight() / 2f - circleRadius - 1.7f, 6, 6, circleRadius, circleColor);

            float textOffsetX = posX + padding + circleRadius * 2 + 4;
            fontManager.SFPD.getSize(14.f).drawString(guiGraphics, prefixText, textOffsetX, currentY - 0.2F, -1);
            textOffsetX += fontManager.SFPD.getSize(14.f).getStringWidth(prefixText) + 2;
            fontManager.SFPD.getSize(14.f).drawString(guiGraphics, nameText, textOffsetX, currentY - 0.2F, textColor);

            currentY += font.getHeight() + 3.8f;
            float nameWidth = font.getStringWidth(prefixText) + 2 + font.getStringWidth(nameText) + padding * 2 + circleRadius * 2 + 2;
            maxWidth = Math.max(maxWidth, nameWidth);

        } else {
            for (Staff staff : staffPlayers) {
                String nameText = staff.name;
                String prefixText = staff.prefix;
                float rowHeight = font.getHeight() + padding * 2 - 2;

                // Фон строки
                RenderUtil.Blur.drawBlur(guiGraphics, posX, currentY - padding, width, rowHeight, 5, 10, -1);
                RenderUtil.drawRoundedRect(guiGraphics, posX, currentY - padding, width, rowHeight, 5, bodyBgColor);

                // Цветной кружок (спец/не спец)
                int baseCircleColor = staff.isSpec ? Color.RED.getRGB() : Color.GREEN.getRGB();
                int circleColor = new Color((baseCircleColor >> 16) & 0xFF,
                        (baseCircleColor >> 8) & 0xFF,
                        baseCircleColor & 0xFF,
                        alpha).hashCode();
                RenderUtil.drawRoundedRect(guiGraphics, posX + padding + 1, currentY + font.getHeight() / 2f - circleRadius - 1.7f, 6, 6, circleRadius, circleColor);

                float textOffsetX = posX + padding + circleRadius * 2 + 4;


                if (!prefixText.isEmpty()) {
                    // Берем первый символ префикса как цвет (например, 'c' -> красный)
                    char colorChar = prefixText.charAt(0);
                    String coloredPrefix = "§" + colorChar + prefixText.substring(1);

                    // Рендерим префикс
                    fontManager.SFPD.getSize(14.f).drawStringWithDefaultRender(guiGraphics, coloredPrefix, textOffsetX, currentY - 0.2f, -1);

                    // Смещаем X, убираем цветовые коды из ширины
                    textOffsetX += fontManager.SFPD.getSize(14.f).getStringWidth(coloredPrefix.replaceAll("", "")) + 2;
                }


                // Рендер имени
                fontManager.SFPD.getSize(14.f).drawString(guiGraphics, nameText, textOffsetX, currentY - 0.2F, textColor);

                currentY += font.getHeight() + 3.8f;

                float nameWidth = (prefixText.isEmpty() ? 0 : font.getStringWidth(prefixText) + 2) +
                        font.getStringWidth(nameText) + padding * 2 + circleRadius * 2 + 2;
                maxWidth = Math.max(maxWidth, nameWidth);
            }

        }

        if (isVisible) {
            width = maxWidth;
            height = currentY - posY;
        }
    }
    private static int getMinecraftColor(String text, int alpha) {
        if (text == null || text.isEmpty()) return new Color(255, 255, 255, alpha).hashCode();

        int index = text.indexOf('§');
        if (index != -1 && index + 1 < text.length()) {
            char colorChar = text.charAt(index + 1);
            return switch (colorChar) {
                case '0' -> new Color(0, 0, 0, alpha).hashCode();
                case '1' -> new Color(0, 0, 170, alpha).hashCode();
                case '2' -> new Color(0, 170, 0, alpha).hashCode();
                case '3' -> new Color(0, 170, 170, alpha).hashCode();
                case '4' -> new Color(170, 0, 0, alpha).hashCode();
                case '5' -> new Color(170, 0, 170, alpha).hashCode();
                case '6' -> new Color(255, 170, 0, alpha).hashCode();
                case '7' -> new Color(170, 170, 170, alpha).hashCode();
                case '8' -> new Color(85, 85, 85, alpha).hashCode();
                case '9' -> new Color(85, 85, 255, alpha).hashCode();
                case 'a' -> new Color(85, 255, 85, alpha).hashCode();
                case 'b' -> new Color(85, 255, 255, alpha).hashCode();
                case 'c' -> new Color(255, 85, 85, alpha).hashCode();
                case 'd' -> new Color(255, 85, 255, alpha).hashCode();
                case 'e' -> new Color(255, 255, 85, alpha).hashCode();
                case 'f' -> new Color(255, 255, 255, alpha).hashCode();
                default -> new Color(255, 255, 255, alpha).hashCode();
            };
        }
        return new Color(255, 255, 255, alpha).hashCode();
    }


    public static void handleMouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // ЛКМ
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

    public static void updateStaffList() {
        staffPlayers.clear();
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.level == null) return;

        for (var team : mc.level.getScoreboard().getPlayerTeams()) {
            for (String member : team.getPlayers()) {
                String name = member;
                String prefix = team.getPlayerPrefix().getString();


                boolean vanish = true;
                for (var playerInfo : mc.getConnection().getOnlinePlayers()) {
                    if (playerInfo.getProfile().getName().equals(name)) {
                        vanish = false;
                        break;
                    }
                }

                if (namePattern.matcher(name).matches() && !name.equals(mc.player.getName().getString())) {
                    if (!vanish) {
                        if (prefixMatches.matcher(prefix.toLowerCase(Locale.ROOT)).matches()) {
                            staffPlayers.add(new Staff(prefix, name, false, Status.NONE));
                        }
                    }
                    if (vanish && !prefix.isEmpty()) {
                        staffPlayers.add(new Staff(prefix, name, true, Status.VANISHED));
                    }
                }
            }
        }

        staffPlayers.sort(Comparator.comparing(staff -> staff.name.toLowerCase(Locale.ROOT)));
    }


    private static class Staff {
        String prefix; // префикс с §
        String name;
        boolean isSpec;
        Status status;

        public Staff(String prefix, String name, boolean isSpec, Status status) {
            this.prefix = prefix;
            this.name = name;
            this.isSpec = isSpec;
            this.status = status;
        }
    }

    public enum Status {
        NONE,
        VANISHED
    }

}