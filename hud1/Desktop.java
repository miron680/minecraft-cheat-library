package i.chuppachups.reek.client.ui.display;

import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import i.chuppachups.reek.client.Heiligtum;
import i.chuppachups.reek.client.font.api.Font;
import i.chuppachups.reek.client.util.impl.move.MoveUtil;
import i.chuppachups.reek.client.util.impl.player.PlayerUtil;
import i.chuppachups.reek.client.util.impl.render.RenderUtil;
import i.chuppachups.reek.client.util.type.impl.IRender;
import i.chuppachups.reek.client.util.type.impl.IMinecraft;
import i.chuppachups.reek.client.user.UserData;

import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Desktop implements IMinecraft, IRender {

    private static final AnimatedValue fpsAnim   = new AnimatedValue(0, 0.02f);
    private static final AnimatedValue xAnim     = new AnimatedValue(0, 0.02f);
    private static final AnimatedValue yAnim     = new AnimatedValue(0, 0.02f);
    private static final AnimatedValue zAnim     = new AnimatedValue(0, 0.02f);
    private static final AnimatedValue speedAnim = new AnimatedValue(0, 0.02f);
    private static final AnimatedValue pingAnim  = new AnimatedValue(0, 0.02f);;

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker, int scWidth, int scHeight) {

        Font font = fontManager.BENZIN.getSize(18.f);
        String text = "RK";

        RenderUtil.Blur.drawBlur(guiGraphics, 5, 3, 22 , 15, 5, 10, -1);
        RenderUtil.drawRoundedRect(guiGraphics, 5, 3, 22 , 15, 5, new Color(0, 0, 0, 170).getRGB());

        RenderUtil.Blur.drawBlur(guiGraphics, 28, 3, 70 , 15, 5, 10, -1);
        RenderUtil.drawRoundedRect(guiGraphics, 28, 3, 70 , 15, 5, new Color(0, 0, 0, 170).getRGB());

        RenderUtil.Blur.drawBlur(guiGraphics, 99, 3 , 50 , 15, 5, 10, -1);
        RenderUtil.drawRoundedRect(guiGraphics, 99, 3 , 50 , 15, 5, new Color(0, 0, 0, 170).getRGB());

        RenderUtil.Blur.drawBlur(guiGraphics, 5, 19 , 60 , 15, 5, 10, -1);
        RenderUtil.drawRoundedRect(guiGraphics, 5, 19 , 60 , 15, 5, new Color(0, 0, 0, 170).getRGB());

        fontManager.SFPD.getSize(18.f).drawString(guiGraphics, text, 10, 5.5f, -1);
        fontManager.SFPD.getSize(16.f).drawString(guiGraphics, "сhuppachups", 43, 5.5f, -1);

        fpsAnim.setTarget(mc.getFps());
        xAnim.setTarget((float) mc.player.getX());
        yAnim.setTarget((float) mc.player.getY());
        zAnim.setTarget((float) mc.player.getZ());
        speedAnim.setTarget((float) (MoveUtil.getSpeed() * 20));
        pingAnim.setTarget(PlayerUtil.getPing(mc.player));


        fontManager.ICONS.getSize(18.f).drawString(guiGraphics, "X", 103,6 ,-1);
        fontManager.SFPD.getSize(16.f).drawString(guiGraphics, fpsAnim.getInt() + " fps", 101 + font.getStringWidth(text), 5.5f , -1);

        LocalTime now = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String timeText = now.format(formatter);

        fontManager.ICONS.getSize(18.f).drawString(guiGraphics, timeText + "V",8,22f,-1);
        fontManager.SFPD.getSize(16.f).drawString(guiGraphics, timeText + " time", 20, 22f , -1);

        fontManager.ICONS.getSize(18.f).drawString(guiGraphics, "W" ,32,6f,-1);

        float offset = (float) ((mc.gui.getAnimation().get() / 2) * window.getGuiScale());

        font = fontManager.SFPD.getSize(16.f);

        String xyzText = "xyz: " + ChatFormatting.GRAY + String.format("%.1f, %.1f, %.1f", xAnim.get(), yAnim.get(), zAnim.get());
        font.drawString(guiGraphics, xyzText, 1, scHeight - 11.f - offset, -1);
        offset += 8.f;

        String speedText = "b/s: " + ChatFormatting.GRAY + String.format("%.1f", speedAnim.get());
        font.drawString(guiGraphics, speedText, 1, scHeight - 11.f - offset, -1);

        String pingText = "ping: " + ChatFormatting.GRAY + pingAnim.getInt();
        font.drawString(guiGraphics, pingText, scWidth - font.getStringWidth(pingText) - 3.f, scHeight - 11.f - offset, -1);
    }

    private static class AnimatedValue {
        private float value;
        private float target;
        private final float speed;

        public AnimatedValue(float start, float speed) {
            this.value = start;
            this.target = start;
            this.speed = speed;
        }

        public void setTarget(float target) {
            this.target = target;
        }

        public float get() {
            value += (target - value) * speed;
            return value;
        }

        public int getInt() {
            return Math.round(get());
        }
    }
}
