package rich.modules.impl.render;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import rich.IMinecraft;
import rich.events.api.EventHandler;
import rich.events.impl.JumpEvent;
import rich.events.impl.WorldRenderEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.ColorSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.ColorUtil;
import rich.util.render.Render3D;

import java.util.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class JumpEffect extends ModuleStructure implements IMinecraft {

    final List<WaveEffect> waveEffects = Collections.synchronizedList(new ArrayList<>());

    final SliderSettings radius = new SliderSettings("Радиус", "Radius of effect")
            .setValue(4f)
            .range(2.0f, 8.0f);

    final SliderSettings speed = new SliderSettings("Скорость", "Animation speed")
            .setValue(800f)
            .range(300f, 2000f);

    final ColorSetting color1 = new ColorSetting("Цвет 1", "First color")
            .value(ColorUtil.getColor(100, 200, 255, 255));

    final ColorSetting color2 = new ColorSetting("Цвет 2", "Second color")
            .value(ColorUtil.getColor(255, 100, 200, 255));

    public JumpEffect() {
        super("JumpEffect", "Jump Effect", ModuleCategory.RENDER);
        settings(radius, speed, color1, color2);
    }

    @EventHandler
    public void onJump(JumpEvent event) {
        if (!isState()) return;
        if (mc.player == null || event.getPlayer() != mc.player) return;

        BlockPos pos = mc.player.getBlockPos().down();
        waveEffects.add(new WaveEffect(pos, System.currentTimeMillis()));
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent e) {
        if (!isState()) return;
        if (waveEffects.isEmpty() || mc.world == null) return;

        Iterator<WaveEffect> iterator = waveEffects.iterator();
        while (iterator.hasNext()) {
            WaveEffect wave = iterator.next();
            if (wave.isExpired()) {
                iterator.remove();
                continue;
            }
            wave.render();
        }
    }

    private class WaveEffect {
        private final BlockPos centerPos;
        private final long startTime;
        private final long duration;
        private final int maxRadius;

        public WaveEffect(BlockPos centerPos, long startTime) {
            this.centerPos = centerPos;
            this.startTime = startTime;
            this.duration = (long) speed.getValue();
            this.maxRadius = (int) Math.ceil(radius.getValue());
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - startTime > duration;
        }

        public void render() {
            if (mc.world == null) return;

            long elapsed = System.currentTimeMillis() - startTime;
            float progress = (float) elapsed / duration;
            
            float currentRadius = easeOutCubic(progress) * maxRadius;
            
            float fadeInDuration = 0.15f;
            float fadeOutStart = 0.75f;
            float globalAlpha;

            if (progress < fadeInDuration) {
                globalAlpha = progress / fadeInDuration;
            } else if (progress >= fadeOutStart) {
                float fadeOutProgress = (progress - fadeOutStart) / (1f - fadeOutStart);
                globalAlpha = 1f - easeInCubic(fadeOutProgress);
            } else {
                globalAlpha = 1f;
            }

            int rendered = 0;
            int maxPerFrame = 400;

            for (int x = -maxRadius; x <= maxRadius; x++) {
                for (int z = -maxRadius; z <= maxRadius; z++) {
                    if (rendered >= maxPerFrame) break;

                    BlockPos blockPos = centerPos.add(x, 0, z);
                    
                    double distanceFromCenter = Math.sqrt(x * x + z * z);
                    
                    if (distanceFromCenter > currentRadius + 0.5f) continue;
                    if (distanceFromCenter < currentRadius - 2.5f) continue;

                    BlockState state = mc.world.getBlockState(blockPos);
                    if (state.isAir()) continue;

                    VoxelShape shape = state.getOutlineShape(mc.world, blockPos);
                    if (shape.isEmpty()) continue;

                    rendered++;

                    float waveProgress = (float) (distanceFromCenter / maxRadius);
                    
                    float localAlpha = 1.0f - Math.abs((float)distanceFromCenter - currentRadius) / 2.5f;
                    localAlpha = Math.max(0, Math.min(1, localAlpha));
                    
                    float pulseOffset = waveProgress * 2f;
                    float pulse = (float) Math.sin((progress * Math.PI * 4) - pulseOffset);
                    pulse = (pulse + 1f) / 2f;
                    localAlpha *= (0.5f + pulse * 0.5f);
                    
                    localAlpha *= globalAlpha;

                    if (localAlpha > 0.02f) {
                        int gradientColor = getGradientColor(color1.getColor(), color2.getColor(), waveProgress);
                        int finalColor = ColorUtil.setAlpha(gradientColor, (int) (localAlpha * 180));

                        try {
                            Render3D.drawShapeAlternative(
                                    blockPos,
                                    shape,
                                    finalColor,
                                    1.5f,
                                    true,
                                    true
                            );
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }

        private int getGradientColor(int c1, int c2, float t) {
            float smoothT = (float) (Math.sin((t - 0.5f) * Math.PI) * 0.5f + 0.5f);
            return ColorUtil.lerpColor(c1, c2, smoothT);
        }

        private float easeOutCubic(float x) {
            return 1f - (float) Math.pow(1f - x, 3);
        }

        private float easeInCubic(float x) {
            return x * x * x;
        }
    }
}
