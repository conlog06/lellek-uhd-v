package gg.lellek.hud.waypoint;

import com.mojang.blaze3d.vertex.PoseStack;
import gg.lellek.hud.feature.Mods;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

/** Wegpunkte in der Welt: Lichtstrahl + Schild – über den SubmitNodeCollector (26.x). */
public class WaypointRenderer {
    public static void install() { LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(WaypointRenderer::render); }

    public static void render(LevelRenderContext ctx) {
        try { renderSafe(ctx); } catch (Throwable t) { if (!warned) { warned = true; gg.lellek.hud.LellekHud.log("Wegpunkte: Rendering deaktiviert – " + t); } }
    }
    private static boolean warned;
    private static void renderSafe(LevelRenderContext ctx) {
        if (!Mods.WAYPOINTS.enabled) return;
        Minecraft mc = Minecraft.getInstance(); if (mc.player == null) return;
        Vec3 cam = ctx.levelState().cameraRenderState.pos;
        SubmitNodeCollector out = ctx.submitNodeCollector();
        boolean beams = Mods.WAYPOINTS.boolOf("beam"); int maxDist = Mods.WAYPOINTS.intOf("maxdist");
        PoseStack pose = ctx.poseStack();
        for (Waypoint w : Waypoints.here()) {
            if (!w.visible) continue;
            double dx = w.x + 0.5 - cam.x, dy = w.y + 0.5 - cam.y, dz = w.z + 0.5 - cam.z, dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (maxDist > 0 && dist > maxDist) continue;
            if (beams) WorldLines.line(pose, out, dx, -cam.y, dz, dx, -cam.y + 320, dz, (w.color & 0xFFFFFF) | 0x8C000000);
            double clamp = Math.min(dist, 48), f = dist > 0 ? clamp / dist : 1;
            label(ctx, out, pose, dx * f, dy * f, dz * f, w.name + " (" + (int) dist + "m)", w.color, (float) clamp);
        }
    }
    static void label(LevelRenderContext ctx, SubmitNodeCollector out, PoseStack pose, double x, double y, double z, String text, int color, float dist) {
        Font font = Minecraft.getInstance().font;
        float scale = 0.016f * Math.max(1f, dist / 8f);
        pose.pushPose();
        pose.translate(x, y + 0.6, z);
        pose.mulPose(new org.joml.Matrix4f().rotation(ctx.levelState().cameraRenderState.orientation));
        pose.scale(scale, -scale, scale);
        float w = font.width(text) / 2f;
        out.submitText(pose, -w, -3, Component.literal(text).getVisualOrderText(), true, Font.DisplayMode.SEE_THROUGH, 0xF000F0, 0xFFFFFFFF, (color & 0xFFFFFF) | 0x60000000, 0);
        pose.popPose();
    }
}
