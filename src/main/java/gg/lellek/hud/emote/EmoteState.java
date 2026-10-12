package gg.lellek.hud.emote;

import gg.lellek.hud.feature.Mods;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Wer spielt gerade welches Emote (eigener Spieler + andere über den Cosmetics-Server) + Vorschau + Musik. */
public class EmoteState {
    public static class Playing {
        public final Emote emote; public final long start; int lastStep = -1;
        Playing(Emote e, long s) { emote = e; start = s; }
        public float t() { return (System.currentTimeMillis() - start) / 1000f; }
        public boolean done() { return emote.duration > 0 && t() > emote.duration; }
    }
    private static Playing local, preview;
    public static final Map<UUID, Playing> others = new ConcurrentHashMap<>();
    public static boolean changed;
    private static net.minecraft.client.CameraType savedCamera;

    public static void play(Emote e) {
        local = e == null ? null : new Playing(e, System.currentTimeMillis()); changed = true; preview = null;
        Minecraft mc = Minecraft.getInstance();
        // Eigenes Emote sieht man nur von außen: Kamera nach hinten, beim Beenden zurück
        if (local != null && savedCamera == null && mc.options.getCameraType().isFirstPerson()) { savedCamera = mc.options.getCameraType(); mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_BACK); }
        if (local == null) restoreCamera();
    }
    /** Vorschau im Emote-Menü (nur lokal, wird nicht gesendet) */
    public static void preview(Emote e) { if (e == null) preview = null; else if (preview == null || preview.emote != e) preview = new Playing(e, System.currentTimeMillis()); }
    private static void restoreCamera() { if (savedCamera != null) { Minecraft.getInstance().options.setCameraType(savedCamera); savedCamera = null; } }
    public static void stop() { if (local != null) { local = null; changed = true; } restoreCamera(); }
    public static Playing local() { if (local != null && local.done()) { local = null; changed = true; restoreCamera(); } return local; }
    public static void setOther(UUID id, Emote e, long start) { if (e == null) others.remove(id); else { Playing o = others.get(id); if (o == null || o.emote != e || o.start != start) others.put(id, new Playing(e, start)); } }
    public static Playing forUuid(UUID id) {
        Minecraft mc = Minecraft.getInstance();
        if (id == null) return null;
        if (mc.player != null && id.equals(mc.player.getUUID())) {
            if (preview != null && gg.lellek.hud.LellekHud.screen(mc) instanceof gg.lellek.hud.gui.EmotesScreen) { if (preview.done()) preview = new Playing(preview.emote, System.currentTimeMillis()); return preview; }
            return local();
        }
        Playing o = others.get(id); if (o != null && o.done()) { others.remove(id); return null; } return o;
    }

    public static void tick(Minecraft mc) {
        if (!(gg.lellek.hud.LellekHud.screen(mc) instanceof gg.lellek.hud.gui.EmotesScreen)) preview = null;
        if (local == null) restoreCamera();
        if (local != null && mc.player != null && (mc.player.zza != 0 || mc.player.xxa != 0 || mc.player.isSprinting() || mc.player.getDeltaMovement().y > 0.3)) stop();
        // Musik: eigenes Emote + Emotes anderer in Hörweite (leiser mit Entfernung)
        if (!Mods.EMOTES.boolOf("sounds")) return;
        float vol = Mods.EMOTES.intOf("volume") / 100f;
        Playing own = preview != null ? preview : local();
        if (own != null) music(own, vol);
        if (mc.level == null || mc.player == null) return;
        for (Map.Entry<UUID, Playing> en : others.entrySet()) {
            Player pl = mc.level.getPlayerByUUID(en.getKey()); if (pl == null) continue;
            double d = pl.distanceTo(mc.player); if (d > 24) continue;
            music(en.getValue(), vol * (float) (1 - d / 24) * 0.8f);
        }
    }
    private static void music(Playing p, float vol) {
        EmoteMusic m = p.emote.music; if (m == null || p.done()) return;
        int step = m.stepAt(p.t());
        if (p.lastStep < 0 || step - p.lastStep > 4) p.lastStep = step - 1;
        while (p.lastStep < step) { p.lastStep++; m.play(p.lastStep, vol * p.emote.weight(p.t())); }
    }
}
