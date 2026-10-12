package gg.lellek.hud.emote;

import net.minecraft.client.Minecraft;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Wer spielt gerade welches Emote (eigener Spieler + andere über den Cosmetics-Server). */
public class EmoteState {
    public static class Playing { public final Emote emote; public final long start; Playing(Emote e, long s) { emote = e; start = s; } public float t() { return (System.currentTimeMillis() - start) / 1000f; } public boolean done() { return emote.duration > 0 && t() > emote.duration; } }
    private static Playing local;
    public static final Map<UUID, Playing> others = new ConcurrentHashMap<>();
    public static boolean changed;
    private static net.minecraft.client.CameraType savedCamera;
    public static void play(Emote e) {
        local = e == null ? null : new Playing(e, System.currentTimeMillis()); changed = true;
        Minecraft mc = Minecraft.getInstance();
        // Eigenes Emote sieht man nur von außen: Kamera nach hinten, beim Beenden zurück
        if (local != null && savedCamera == null && mc.options.getCameraType().isFirstPerson()) { savedCamera = mc.options.getCameraType(); mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_BACK); }
        if (local == null) restoreCamera();
    }
    private static void restoreCamera() { if (savedCamera != null) { Minecraft.getInstance().options.setCameraType(savedCamera); savedCamera = null; } }
    public static void stop() { if (local != null) { local = null; changed = true; } restoreCamera(); }
    public static Playing local() { if (local != null && local.done()) { local = null; changed = true; restoreCamera(); } return local; }
    public static void setOther(UUID id, Emote e, long start) { if (e == null) others.remove(id); else others.put(id, new Playing(e, start)); }
    public static Playing forUuid(UUID id) {
        Minecraft mc = Minecraft.getInstance();
        if (id == null) return null;
        if (mc.player != null && id.equals(mc.player.getUUID())) return local();
        Playing o = others.get(id); if (o != null && o.done()) { others.remove(id); return null; } return o;
    }
    public static void tick(Minecraft mc) {
        if (local == null || mc.player == null) { if (local == null) restoreCamera(); return; }
        if (mc.player.zza != 0 || mc.player.xxa != 0 || mc.player.isSprinting() || mc.player.getDeltaMovement().y > 0.3) stop();
    }
}
