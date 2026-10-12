package gg.lellek.hud.waypoint;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import gg.lellek.hud.LellekHud;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Linien in der Welt – ausschließlich über Minecrafts eigene Linien-Routine (ShapeRenderer.renderLineBox),
 * die das Vertex-Format der jeweiligen Version garantiert richtig füllt. Eigene Vertex-Aufrufe haben in 26.2
 * zu "Missing elements in vertex" geführt, und die Ausnahme fällt erst in der Render-Pipeline, wo kein try/catch greift.
 */
public final class WorldLines {
    private static Method lineBox; private static boolean searched, poseStackFirst;

    private static boolean ready() {
        if (searched) return lineBox != null;
        searched = true;
        try {
            Class<?> sr = Class.forName("net.minecraft.client.renderer.ShapeRenderer");
            for (Method m : sr.getMethods()) {
                if (!Modifier.isStatic(m.getModifiers()) || !m.getName().equals("renderLineBox")) continue;
                Class<?>[] p = m.getParameterTypes();
                if (p.length == 12 && p[1] == VertexConsumer.class && p[2] == double.class && p[8] == float.class) { lineBox = m; poseStackFirst = p[0] == PoseStack.class; break; }
            }
        } catch (Throwable ignored) {}
        if (lineBox == null) LellekHud.log("Welt-Linien: ShapeRenderer.renderLineBox nicht gefunden – Lichtstrahl und Block-Umrandung bleiben aus");
        else LellekHud.log("Welt-Linien: nutze ShapeRenderer.renderLineBox(" + (poseStackFirst ? "PoseStack" : "Pose") + ", …)");
        return lineBox != null;
    }

    /** Quader-Umriss, Koordinaten kameraRelativ */
    public static void box(PoseStack pose, SubmitNodeCollector out, double x1, double y1, double z1, double x2, double y2, double z2, int color) {
        if (!ready()) return;
        float r = ((color >> 16) & 255) / 255f, g = ((color >> 8) & 255) / 255f, b = (color & 255) / 255f, a = ((color >> 24) & 255) / 255f;
        out.submitCustomGeometry(pose, RenderTypes.lines(), (p, vc) -> {
            try {
                Object first = poseStackFirst ? fromPose(p) : p;
                lineBox.invoke(null, first, vc, Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2), Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2), r, g, b, a);
            } catch (Throwable t) { lineBox = null; }
        });
    }
    /** Senkrechter Strahl als sehr dünner Quader */
    public static void line(PoseStack pose, SubmitNodeCollector out, double x1, double y1, double z1, double x2, double y2, double z2, int color) {
        box(pose, out, x1 - 0.02, y1, z1 - 0.02, x2 + 0.02, y2, z2 + 0.02, color);
    }
    private static PoseStack fromPose(PoseStack.Pose p) { PoseStack ps = new PoseStack(); ps.mulPose(p.pose()); return ps; }
}
