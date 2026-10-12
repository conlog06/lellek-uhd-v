package gg.lellek.hud.feature;

import gg.lellek.hud.LellekHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/** Logik aller Features; FOV/Gamma/Mausrad greifen die Mixins ab, Chat/Angriff die Fabric-Events. */
public class Features {
    public boolean zooming;
    private double savedSensitivity = -1, savedGamma = -1, savedTilt = -1; private int savedFov = -1;
    /** Smooth Zoom: Ziel- und aktuelle Vergrößerung, interpoliert pro Frame (logarithmisch, damit jede Stufe gleich weich wirkt). */
    private double zoomTarget = 1, zoomCurrent = 1, scrollLevel = -1; private long zoomLastNanos; private long zoomShownUntil; private double lastSensZoom = 1;
    private int savedFpsLimit = -1;
    private boolean gammaApplied, tiltApplied, sneakActive, wasHurt;
    public static double lastReach = 0; public static int combo = 0;

    /** true, solange gezoomt wird oder die Rück-Animation noch läuft */
    public boolean zoomActive() { return zooming || zoomCurrent > 1.001; }
    public double zoomLevel() { return zoomCurrent; }
    public boolean showZoomIndicator() { return Mods.ZOOM.boolOf("indicator") && zooming && System.currentTimeMillis() < zoomShownUntil; }

    private void updateZoom() {
        long now = System.nanoTime();
        double dt = zoomLastNanos == 0 ? 0 : Math.min(0.1, (now - zoomLastNanos) / 1e9); zoomLastNanos = now;
        if (!Mods.ZOOM.boolOf("animate")) { zoomCurrent = zoomTarget; return; }
        double speed = 4 + Mods.ZOOM.intOf("speed") * 2.2; // 1 = gemütlich, 10 = fast sofort
        double k = 1 - Math.exp(-dt * speed);
        double lc = Math.log(zoomCurrent), lt = Math.log(zoomTarget);
        lc += (lt - lc) * k;
        if (Math.abs(lt - lc) < 0.002) lc = lt;
        zoomCurrent = Math.exp(lc);
    }
    public float modifyFov(float fov) { updateZoom(); return zoomCurrent > 1.0001 ? (float) (fov / zoomCurrent) : fov; }
    public boolean onScroll(double dy) {
        if (!zooming || dy == 0 || !Mods.ZOOM.boolOf("scroll")) return false;
        double max = Mods.ZOOM.intOf("max");
        zoomTarget = Math.max(1.5, Math.min(max, zoomTarget * Math.pow(1.25, Math.signum(dy))));
        scrollLevel = zoomTarget; zoomShownUntil = System.currentTimeMillis() + 1200;
        if (Mods.ZOOM.boolOf("remember")) ((Setting.Int) Mods.ZOOM.setting("factor")).set(String.valueOf((int) Math.round(zoomTarget)));
        return true;
    }
    /** Mausempfindlichkeit so umrechnen, dass sich die Drehung proportional zum Sichtfeld verlangsamt (MC: (s*0.6+0.2)^3). */
    private static double sensFor(double base, double zoom) { return Math.max(0, Math.min(1, ((base * 0.6 + 0.2) / Math.cbrt(zoom) - 0.2) / 0.6)); }

    /** Fenster nicht im Fokus → FPS-Limit runter (spart CPU/GPU für andere Programme), beim Zurückkommen Originalwert. */
    private void backgroundFps(Minecraft mc) {
        boolean bg = Mods.BACKGROUND_FPS.enabled && !mc.getWindow().isFocused();
        if (bg && savedFpsLimit < 0) {
            savedFpsLimit = mc.options.framerateLimit().get();
            int target = Integer.parseInt(((Setting.Choice) Mods.BACKGROUND_FPS.setting("fps")).current());
            if (savedFpsLimit > target) mc.options.framerateLimit().set(target); else savedFpsLimit = -1;
        } else if (!bg && savedFpsLimit >= 0) { mc.options.framerateLimit().set(savedFpsLimit); savedFpsLimit = -1; }
    }
    public boolean inBackground() { return savedFpsLimit >= 0; }

    public void onAttack() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.hitResult == null) return;
        lastReach = mc.hitResult.getLocation().distanceTo(mc.player.getEyePosition());
        combo++;
    }

    public Component onChat(Component msg) {
        if (!Mods.CHAT_TIME.enabled) return msg;
        String fmt = Mods.CHAT_TIME.boolOf("seconds") ? "HH:mm:ss" : "HH:mm";
        return Component.literal("[" + new SimpleDateFormat(fmt).format(new Date()) + "] ").withStyle(net.minecraft.network.chat.Style.EMPTY.withColor(net.minecraft.network.chat.TextColor.fromRgb(0xAAAAAA))).append(msg);
    }

    public static void drawCrosshair(GuiGraphicsExtractor g, int cx, int cy) {
        Minecraft mc = Minecraft.getInstance();
        int style = Mods.CROSSHAIR.choiceOf("style"), s = Mods.CROSSHAIR.intOf("size"), t = Mods.CROSSHAIR.intOf("thickness"), c = Mods.CROSSHAIR.colorOf("color");
        if (Mods.CROSSHAIR.boolOf("target") && mc.hitResult instanceof net.minecraft.world.phys.EntityHitResult) c = Mods.CROSSHAIR.colorOf("targetcolor");
        if (Mods.CROSSHAIR.boolOf("dynamic") && mc.player != null) { double v = mc.player.getDeltaMovement().horizontalDistance(); s += (int) Math.min(6, v * 25); }
        int h = (t + 1) / 2;
        switch (style) {
            case 0 -> { g.fill(cx - s, cy - h, cx + s + 1, cy + t - h, c); g.fill(cx - h, cy - s, cx + t - h, cy + s + 1, c); }
            case 1 -> g.fill(cx - h, cy - h, cx + t, cy + t, c);
            case 2 -> { for (int a = 0; a < 360; a += 10) { int px = cx + (int) Math.round(Math.cos(Math.toRadians(a)) * s), py = cy + (int) Math.round(Math.sin(Math.toRadians(a)) * s); g.fill(px, py, px + t, py + t, c); } }
            default -> { int gap = 2; g.fill(cx - s, cy - h, cx - gap, cy + t - h, c); g.fill(cx + gap + 1, cy - h, cx + s + 1, cy + t - h, c); g.fill(cx - h, cy - s, cx + t - h, cy - gap, c); g.fill(cx - h, cy + gap + 1, cx + t - h, cy + s + 1, c); }
        }
    }

    public void tick(Minecraft mc) {
        backgroundFps(mc);
        if (mc.player == null) { if (savedSensitivity >= 0) { mc.options.sensitivity().set(savedSensitivity); savedSensitivity = -1; } zooming = false; zoomTarget = zoomCurrent = 1; combo = 0; return; }

        boolean want = Mods.ZOOM.enabled && LellekHud.zoomKey.isDown() && LellekHud.screen(mc) == null;
        if (want && !zooming) {
            zoomTarget = Mods.ZOOM.boolOf("remember") && scrollLevel > 0 ? scrollLevel : Mods.ZOOM.intOf("factor");
            zoomShownUntil = System.currentTimeMillis() + 1200; zoomLastNanos = 0;
            if (savedSensitivity < 0) savedSensitivity = mc.options.sensitivity().get();
            mc.options.smoothCamera = Mods.ZOOM.boolOf("smooth");
            if (!gg.lellek.hud.Diag.fov) { savedFov = mc.options.fov().get(); mc.options.fov().set(Math.max(30, (int) (savedFov / zoomTarget))); } // Fallback ohne FOV-Mixin: Option direkt
        }
        if (!want && zooming) {
            zoomTarget = 1; mc.options.smoothCamera = false;
            if (savedFov >= 0) { mc.options.fov().set(savedFov); savedFov = -1; }
        }
        zooming = want;
        // Empfindlichkeit folgt der aktuellen (animierten) Zoom-Stufe; nach dem Rauszoomen wieder Original
        if (savedSensitivity >= 0) {
            boolean active = zoomActive() && Mods.ZOOM.boolOf("sens");
            double z = active ? zoomCurrent : 1;
            if (Math.abs(z - lastSensZoom) > 0.01 || !active) {
                mc.options.sensitivity().set(active ? sensFor(savedSensitivity, z) : savedSensitivity); lastSensZoom = z;
            }
            if (!zoomActive()) { mc.options.sensitivity().set(savedSensitivity); savedSensitivity = -1; lastSensZoom = 1; }
        }

        if (Mods.TOGGLE_SPRINT.enabled && mc.player.zza > 0 && !mc.player.isShiftKeyDown() && !mc.player.horizontalCollision
                && mc.player.getFoodData().getFoodLevel() > 6 && !mc.player.isSprinting()) mc.player.setSprinting(true);

        while (LellekHud.sneakKey.consumeClick()) if (Mods.TOGGLE_SNEAK.enabled) sneakActive = !sneakActive;
        if (!Mods.TOGGLE_SNEAK.enabled) sneakActive = false;
        if (sneakActive && LellekHud.screen(mc) == null) mc.options.keyShift.setDown(true);

        if (Mods.FULLBRIGHT.enabled) { if (!gammaApplied) { savedGamma = mc.options.gamma().get(); gammaApplied = true; } mc.options.gamma().set(10.0); }
        else if (gammaApplied) { mc.options.gamma().set(savedGamma); gammaApplied = false; }

        if (Mods.NO_HURT_CAM.enabled) { if (!tiltApplied) { savedTilt = mc.options.damageTiltStrength().get(); tiltApplied = true; } mc.options.damageTiltStrength().set(0.0); }
        else if (tiltApplied) { mc.options.damageTiltStrength().set(savedTilt); tiltApplied = false; }


        Features26.tick(mc);
        if (mc.level != null) {
            if (Mods.WEATHER.enabled) { mc.level.setRainLevel(0f); mc.level.setThunderLevel(0f); }
        }

        boolean hurt = mc.player.hurtTime > 0;
        if (hurt && !wasHurt) combo = 0;
        wasHurt = hurt;
    }
}
