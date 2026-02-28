package im.expensive.ui.display.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import im.expensive.Expensive;
import im.expensive.events.EventDisplay;
import im.expensive.ui.display.ElementRenderer;
import im.expensive.ui.styles.Style;
import im.expensive.utils.client.PingUtil;
import im.expensive.utils.client.ServerTPS;
import im.expensive.utils.render.ColorUtils;
import im.expensive.utils.render.DisplayUtils;
import im.expensive.utils.render.font.Fonts;
import im.expensive.utils.text.GradientUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class WatermarkRenderer implements ElementRenderer {

final Minecraft mc = Minecraft.getInstance();
final ResourceLocation logo = new ResourceLocation("expensive/images/hud/logo.png");
private final ResourceLocation user = new ResourceLocation("expensive/images/hud/user.png");
private final ResourceLocation nurr = new ResourceLocation("expensive/images/nurr.png");


override
public void render(EventDisplay eventDisplay) {
MatrixStack ms = eventDisplay.getMatrixStack();
float posX = 4;
float posY = 4;
float padding = 2f;
float fontSize = 6.5f;
float iconSize = 10;
Style style = Expensive.getInstance().getStyleManager().getCurrentStyle();

//drawStyledRect(posX, posY, iconSize + padding * 2, iconSize + padding * 2, 4);
// DisplayUtils.drawImage(logo, posX + padding, posY + padding, iconSize, iconSize, ColorUtils.rgb(255, 255, 255));

int fps = mc.getDebugFPS();
int ping = PingUtil.calculatePing();
String.valueOf(ServerTPS.getTPS());

ITextComponent text = GradientUtil.gradient("Celestial" );//+ fps + " Пинг " + ping + "мс");
ITextComponent text2 = GradientUtil.white(fps+"Fps");//+ fps + " Пинг " + ping + "мс");
ITextComponent text3 = GradientUtil.white("ping " +ping );//+ fps + " Пинг " + ping + "мс");
ITextComponent text4 = GradientUtil.white("tps " + (ServerTPS.getTPS()));//+ fps + " Пинг " + ping + "мс");



float textWidth = Fonts.sfui.getWidth(text, fontSize);

float localPosX = posX + iconSize + padding * 3;

drawStyledRect(posX, posY, iconSize + padding * 20.5f + textWidth, iconSize + padding * 2, 4);
DisplayUtils.drawImage(user, posX + padding + 1.5f, posY + padding, iconSize, iconSize, ColorUtils.rgb(255, 255, 255));
DisplayUtils.drawImage(nurr, posX + padding +46, posY + padding, iconSize, iconSize, ColorUtils.rgb(255, 255, 255));
DisplayUtils.drawImage(nurr, posX + padding +100, posY + padding, iconSize, iconSize, ColorUtils.rgb(49, 49, 49));


// Fonts.sfui.drawText(ms, text, posX + iconSize + padding * 2.5f - 1, posY + iconSize - 5.5f, fontSize, 255);


drawStyledRect(posX, posY, iconSize + padding * 60.5f + textWidth, iconSize + padding * 2, 5);
DisplayUtils.drawImage(user, posX + padding + 1.5f, posY + padding, iconSize, iconSize, ColorUtils.rgb(255, 255, 255));
DisplayUtils.drawImage(nurr, posX + padding +46, posY + padding, iconSize, iconSize, ColorUtils.rgb(49, 49, 49));
DisplayUtils.drawImage(nurr, posX + padding +86, posY + padding, iconSize, iconSize, ColorUtils.rgb(49, 49, 49));
DisplayUtils.drawImage(nurr, posX + padding +119, posY + padding +0.3f, iconSize, iconSize, ColorUtils.rgb(49, 49, 49));

Fonts.sfui.drawText(ms, text, posX + iconSize + padding * 2.5f - 1, posY + iconSize - 5.5f, fontSize, 255);
Fonts.sfui.drawText(ms, text2, posX + iconSize + padding * 24.5f - 1, posY + iconSize - 6.3f, fontSize, 255);
Fonts.sfui.drawText(ms, text3, posX + iconSize + padding * 44.5f - 1, posY + iconSize - 6.3f, fontSize, 255);
Fonts.sfui.drawText(ms, text4, posX + iconSize + padding * 60.5f - 1, posY + iconSize - 6.3f, fontSize, 255);

}

private void drawStyledRect(float x,
float y,
float width,
float height,
float radius) {

DisplayUtils.drawRoundedRect(x - 0.5f, y - 0.5f, width + 1, height + 1, radius + 0.5f, ColorUtils.getColor(0)); // outline
DisplayUtils.drawRoundedRect(x, y, width, height, radius, ColorUtils.rgba(21, 21, 21, 255));
}
}
