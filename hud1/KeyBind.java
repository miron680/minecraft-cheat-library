package i.chuppachups.reek.client.ui.display;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import i.chuppachups.reek.client.Heiligtum;
import i.chuppachups.reek.client.font.api.Font;
import i.chuppachups.reek.client.module.api.Module; // Импорт класса Module
import i.chuppachups.reek.client.util.impl.animation.AnimationUtil;
import i.chuppachups.reek.client.util.impl.render.RenderUtil;
import i.chuppachups.reek.client.util.type.impl.IMinecraft;
import i.chuppachups.reek.client.util.type.impl.IRender;
import org.lwjgl.glfw.GLFW; // Для преобразования int bind в название клавиши

import java.awt.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KeyBind implements IMinecraft, IRender {

    public static float posX = 50;
    public static float posY = 50;
    public static float width = 100; // Увеличена ширина по умолчанию
    public static float height = 20;
    public static boolean dragging = false;
    private static float dragOffsetX = 0;
    private static float dragOffsetY = 0;
    private static final AnimationUtil alphaAnimation = new AnimationUtil();
    private static boolean isVisible = false;

    static {
        alphaAnimation.set(0.0);
    }

    // --- Карта для явного сопоставления GLFW кодов и англ. названий ---
    private static final Map<Integer, String> KEY_NAME_MAP = new HashMap<>();

    static {
        // Буквы A-Z
        for (int i = 0; i < 26; i++) {
            int key = GLFW.GLFW_KEY_A + i;
            char letter = (char) ('A' + i);
            KEY_NAME_MAP.put(key, String.valueOf(letter));
        }

        // Цифры 0-9
        for (int i = 0; i <= 9; i++) {
            KEY_NAME_MAP.put(GLFW.GLFW_KEY_0 + i, String.valueOf(i));
        }

        // Numpad 0-9
        for (int i = 0; i <= 9; i++) {
            KEY_NAME_MAP.put(GLFW.GLFW_KEY_KP_0 + i, "NUMPAD_" + i);
        }

        // Функциональные клавиши
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_F1, "F1");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_F2, "F2");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_F3, "F3");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_F4, "F4");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_F5, "F5");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_F6, "F6");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_F7, "F7");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_F8, "F8");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_F9, "F9");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_F10, "F10");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_F11, "F11");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_F12, "F12");

        // Специальные клавиши
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_ESCAPE, "ESC");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_TAB, "TAB");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_LEFT_CONTROL, "LCTRL");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_RIGHT_CONTROL, "RCTRL");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_LEFT_SHIFT, "LSHIFT");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_RIGHT_SHIFT, "RSHIFT");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_LEFT_ALT, "LALT");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_RIGHT_ALT, "RALT");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_SPACE, "SPACE");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_ENTER, "ENTER");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_BACKSPACE, "BACKSPACE");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_DELETE, "DEL");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_INSERT, "INSERT");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_HOME, "HOME");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_END, "END");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_PAGE_UP, "PGUP");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_PAGE_DOWN, "PGDN");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_UP, "UP");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_DOWN, "DOWN");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_LEFT, "LEFT");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_RIGHT, "RIGHT");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_CAPS_LOCK, "CAPS");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_SEMICOLON, ";");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_EQUAL, "=");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_COMMA, ",");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_MINUS, "-");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_PERIOD, ".");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_SLASH, "/");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_GRAVE_ACCENT, "`");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_LEFT_BRACKET, "[");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_BACKSLASH, "\\");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_RIGHT_BRACKET, "]");
        KEY_NAME_MAP.put(GLFW.GLFW_KEY_APOSTROPHE, "'");
    }

    // --- Новый метод для получения списка включенных модулей ---
    private static List<Module> getToggledModules() {
        // Предполагается, что в Heiligtum есть ModuleManager
        List<Module> toggledModules = Heiligtum.getInstance().getModuleManager().getModules().stream()
                .filter(Module::isToggled)
                // ДОБАВЛЕНО: Фильтрация, исключающая модули с bind == -1
                .filter(module -> module.getBind() != -1)
                .sorted(Comparator.comparing(Module::getName)) // Сортировка по имени
                .collect(Collectors.toList());
        return toggledModules;
    }

    // --- ИЗМЕНЕННЫЙ метод для преобразования int бинда в строку (АНГЛ.) ---
    private static String getKeyName(int key) {
        if (key == 0) return "NONE";

        // 1. Поиск в явной карте (для букв и общих клавиш)
        if (KEY_NAME_MAP.containsKey(key)) {
            return KEY_NAME_MAP.get(key);
        }

        // 2. Обработка кнопок мыши (Minecraft/GLFW Mouse Buttons)
        // Minecraft uses GLFW key codes for mouse buttons (0-7 for buttons 1-8).
        if (key >= 0 && key <= 7) {
            return "MOUSE_" + (key + 1); // GLFW uses 0 for MOUSE_BUTTON_1, 1 for MOUSE_BUTTON_2, etc.
        }

        // 3. Попытка использовать GLFW (для менее распространенных клавиш)
        // ВАЖНО: Это все еще может дать локализованное название для некоторых клавиш!
        String name = GLFW.glfwGetKeyName(key, 0);
        if (name != null) {
            return name.toUpperCase();
        }

        // 4. Если ничего не помогло, возвращаем код
        return "KEY_" + key;
    }


    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker, int scWidth, int scHeight) {
        List<Module> toggledModules = getToggledModules();

        boolean isInChatScreen = mc.screen instanceof ChatScreen;
        boolean shouldBeVisible = !toggledModules.isEmpty() || (toggledModules.isEmpty() && isInChatScreen);

        // Логика анимации и видимости (как в Potion)
        if (shouldBeVisible != isVisible) {
            isVisible = shouldBeVisible;
            if (isVisible) {
                alphaAnimation.run(255.0, 0.3f);
            } else {
                alphaAnimation.run(0.0, 0.3f);
            }
        }

        alphaAnimation.update();
        if (alphaAnimation.get() < 1.0 && !isVisible) return;
        int alpha = Math.clamp((int) alphaAnimation.get(), 0, 255);
        if (alpha == 0 && !isVisible) return;

        Font headerFont = fontManager.SFPD.getSize(16.f);
        Font bodyFont = fontManager.SFPD.getSize(14.f);

        float padding = 3f;
        float headerHeight = headerFont.getHeight() + padding * 2;
        int headerBgColor = new Color(0, 0, 0, (int) (170 * (alpha / 255f))).hashCode();
        int bodyBgColor = new Color(0, 0, 0, (int) (170 * (alpha / 255f))).hashCode();
        int textColor = new Color(255, 255, 255, alpha).hashCode();
        int bindColor = new Color(235, 85, 105).getRGB(); // Светло-голубой для бинда

        // Рисование заголовка
        RenderUtil.Blur.drawBlur(guiGraphics, posX, posY, width, headerHeight, 5, 10, -1);
        RenderUtil.drawRoundedRect(guiGraphics, posX, posY, width, headerHeight , 5, headerBgColor);

        String headerText = "KeyBinds";
        float headerTextWidth = headerFont.getStringWidth(headerText);
        // Замените иконку зелья на свою, или уберите
        fontManager.ICONS.getSize(22.f).drawString(guiGraphics, "C",posX + 5, posY + padding - 0.5f, Color.white.getRGB());
        headerFont.drawString(guiGraphics, headerText, posX + padding + 20, posY + padding -0.2f, textColor);

        float currentY = posY + headerHeight + padding + 0.1f;
        float maxWidth = headerTextWidth + padding * 2;

        // Если модулей нет и мы в чате, показываем превью
        if (toggledModules.isEmpty() && isInChatScreen) {
            String moduleName = "AttackAura";
            String bind = getKeyName(GLFW.GLFW_KEY_R); // Пример бинда
            float rowHeight = bodyFont.getHeight() + padding * 2 - 1;

            RenderUtil.Blur.drawBlur(guiGraphics, posX, currentY - padding, width, rowHeight, 5, 10, -1);
            RenderUtil.drawRoundedRect(guiGraphics, posX, currentY - padding, width, rowHeight, 5, bodyBgColor);

            // Название Модуля
            bodyFont.drawString(guiGraphics, moduleName, posX + padding +2, currentY -0.3f, textColor);

            // Бинд
            String bindText =  bind ;
            float bindWidth = bodyFont.getStringWidth(bindText);
            float bindX = posX + width - bindWidth - padding;
            RenderUtil.drawRoundedRect(guiGraphics, posX + 61, currentY - padding + 1.5f, 1, 11, 1, -1);
            fontManager.SFPD.getSize(16.f).drawString(guiGraphics, bindText, bindX- 2, currentY -0.7f, bindColor);

            currentY += bodyFont.getHeight() + 4;
            float lineWidth = bodyFont.getStringWidth(moduleName) + bindWidth + padding * 10;
            maxWidth = Math.max(maxWidth, lineWidth);
        } else {
            // Отображаем реальные включенные модули
            for (Module module : toggledModules) {
                String moduleName = module.getName();
                String bind = getKeyName(module.getBind());
                float rowHeight = bodyFont.getHeight() + padding * 2 - 1;

                RenderUtil.Blur.drawBlur(guiGraphics, posX, currentY - padding, width, rowHeight, 5, 10, -1);
                RenderUtil.drawRoundedRect(guiGraphics, posX, currentY - padding, width, rowHeight, 5, bodyBgColor);

                // Название Модуля
                RenderUtil.drawRoundedRect(guiGraphics, posX + 60, currentY - padding + 1.5f, 1, 11, 1, -1);
                bodyFont.drawString(guiGraphics, moduleName, posX + padding+ 2, currentY- 0.3f , textColor);

                // Бинд
                String bindText =  bind ;
                float bindWidth = bodyFont.getStringWidth(bindText);
                float bindX = posX + width - bindWidth - padding;
                fontManager.SFPD.getSize(16.f).drawString(guiGraphics, bindText, bindX-4, currentY -0.7f, bindColor);

                currentY += bodyFont.getHeight() + 5;
                float lineWidth = bodyFont.getStringWidth(moduleName) + fontManager.SFPD.getSize(16.f).getStringWidth(bindText) + padding * 10;
                maxWidth = Math.max(maxWidth, lineWidth);
            }
        }

        // Обновление размеров рамки
        if (isVisible) {
            width = maxWidth + padding * 2;
            height = currentY - posY + padding;
        }
    }

    // --- Методы для перетаскивания (как в Potion) ---

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
}