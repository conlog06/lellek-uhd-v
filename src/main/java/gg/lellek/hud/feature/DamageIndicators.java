package gg.lellek.hud.feature;

import gg.lellek.hud.LellekHud;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/** Schadenszahlen über getroffenen Entities */
public class DamageIndicators {
    private static class Ind { double x, y, z; String text; int color; long born = System.currentTimeMillis(); }
    private final Map<Integer, Float> health = new HashMap<>(); private final List<Ind> list = new ArrayList<>();
    public void install() { LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(this::render); }

    public void tick(Minecraft mc) {
        if (mc.level == null || mc.player == null) { health.clear(); list.clear(); return; }
        if (!Mods.DAMAGE.enabled) return;
        Set<Integer> seen = new HashSet<>();
        for (Entity en : mc.level.entitiesForRendering()) {
            if (!(en instanceof LivingEntity l) || en == mc.player || mc.player.distanceToSqr(en) > 32 * 32) continue;
            seen.add(en.getId()); Float prev = health.get(en.getId()); float now = l.getHealth();
            if (prev != null && Math.abs(now - prev) >= 0.5f) { Ind i = new Ind(); i.x = en.getX(); i.y = en.getY() + en.getBbHeight() + 0.3; i.z = en.getZ(); float d = now - prev; i.text = (d < 0 ? "-" : "+") + String.format("%.1f", Math.abs(d)); i.color = d < 0 ? 0xFFFF5F5F : 0xFF7BD88F; list.add(i); }
            health.put(en.getId(), now);
        }
        health.keySet().retainAll(seen);
        long now = System.currentTimeMillis(); list.removeIf(i -> now - i.born > 1200);
    }
    private boolean warned;
    private void render(LevelRenderContext ctx) {
        try { renderSafe(ctx); } catch (Throwable t) { if (!warned) { warned = true; LellekHud.log("Schadensanzeige: Rendering deaktiviert – " + t); } list.clear(); }
    }
    private void renderSafe(LevelRenderContext ctx) {
        if (!Mods.DAMAGE.enabled || list.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance(); Font font = mc.font; Vec3 cam = ctx.levelState().cameraRenderState.pos; PoseStack pose = ctx.poseStack(); net.minecraft.client.renderer.SubmitNodeCollector out = ctx.submitNodeCollector();
        for (Ind i : list) {
            float age = (System.currentTimeMillis() - i.born) / 1200f;
            pose.pushPose(); pose.translate(i.x - cam.x, i.y - cam.y + age * 0.8, i.z - cam.z); pose.mulPose(new org.joml.Matrix4f().rotation(ctx.levelState().cameraRenderState.orientation)); pose.scale(0.03f, -0.03f, 0.03f);
            int a = Math.max(20, (int) (255 * (1 - age)));
            out.submitText(pose, -font.width(i.text) / 2f, 0, net.minecraft.network.chat.Component.literal(i.text).getVisualOrderText(), true, Font.DisplayMode.SEE_THROUGH, 0xF000F0, (i.color & 0xFFFFFF) | (a << 24), 0, 0);
            pose.popPose();
        }
    }
}
