package gg.lellek.hud.feature;

import gg.lellek.hud.LellekHud;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;

/** Freelook: Taste halten, Kamera dreht um den Spieler. Mausbewegung fängt der EntityMixin ab, die Kamera der CameraMixin. */
public class Perspective {
    public static boolean active; public static float camYaw, camPitch; private static CameraType saved;

    public static void tick(Minecraft mc) {
        if (mc.player == null) { active = false; return; }
        boolean want = Mods.PERSPECTIVE.enabled && LellekHud.perspectiveKey.isDown() && LellekHud.screen(mc) == null;
        if (want && !active) { camYaw = mc.player.getYRot(); camPitch = mc.player.getXRot(); saved = mc.options.getCameraType(); mc.options.setCameraType(CameraType.THIRD_PERSON_BACK); }
        if (!want && active) mc.options.setCameraType(saved);
        active = want;
    }
    /** true = Spielerdrehung schlucken */
    public static boolean onTurn(double yRot, double xRot) {
        if (!active) return false;
        camYaw += (float) (yRot * 0.15); camPitch = Math.max(-90, Math.min(90, camPitch + (float) (xRot * 0.15)));
        return true;
    }
}
