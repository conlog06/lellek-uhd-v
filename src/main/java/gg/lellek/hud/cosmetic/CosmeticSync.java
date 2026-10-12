package gg.lellek.hud.cosmetic;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import gg.lellek.hud.LellekHud;
import gg.lellek.hud.feature.Mods;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;

/** Abgleich mit dem Cosmetics-Server (siehe 1.8.9-Version für Details). */
public class CosmeticSync {
    private static String token, pushed = null; private static int tick; private static boolean busy;
    private static final java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newSingleThreadExecutor(r -> { Thread t = new Thread(r, "LellekHUD-Net"); t.setDaemon(true); return t; });
    public interface Callback { void done(JsonObject json, String error); }
    private static long downUntil; private static boolean downLogged;
    public static boolean serverDown() { return System.currentTimeMillis() < downUntil; }
    public static void request(String method, String path, String body, boolean auth, Callback cb) {
        if (serverDown()) { cb.done(null, "Cosmetics-Server nicht erreichbar"); return; }
        pool.submit(() -> {
            try {
                if (auth && token == null) login();
                JsonObject j;
                try { j = sendJson(open(method, path, auth), body); }
                catch (IOException e) { if (auth && e.getMessage() != null && e.getMessage().contains("eingeloggt")) { token = null; login(); j = sendJson(open(method, path, auth), body); } else throw e; }
                cb.done(j, null);
            } catch (java.net.ConnectException | java.net.UnknownHostException | java.net.SocketTimeoutException e) { downUntil = System.currentTimeMillis() + 5 * 60 * 1000; if (!downLogged) { downLogged = true; LellekHud.log("Cosmetics-Server nicht erreichbar (" + base() + ") – Cosmetics laufen lokal weiter, nächster Versuch in 5 min"); } cb.done(null, "Cosmetics-Server nicht erreichbar"); }
            catch (Exception e) { cb.done(null, e.getMessage() == null ? e.toString() : e.getMessage()); }
        });
    }
    private static final Map<UUID, Identifier> capes = new HashMap<>(); private static final Set<UUID> capeRequested = new HashSet<>();

    private static String base() {
        String s = Mods.COSMETIC_SYNC.textOf("server").trim();
        if (s.equals("http://127.0.0.1:8765")) {
            try { java.nio.file.Path f = Minecraft.getInstance().gameDirectory.toPath().resolve("lellek/cosmetics-server.txt"); if (java.nio.file.Files.exists(f)) { String t = java.nio.file.Files.readString(f).trim(); if (!t.isEmpty()) s = t; } } catch (Exception ignored) {}
        }
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }
    public static Identifier capeTexture(UUID uuid) { return capes.get(uuid); }

    public static void tick(Minecraft mc) {
        if (!Mods.COSMETIC_SYNC.enabled || mc.player == null || mc.getConnection() == null) return;
        tick++;
        if (tick % 40 == 0 && !LellekHud.config.cosmetic.equals(pushed)) push(LellekHud.config.cosmetic);
        if (tick % 600 == 1) pull();
        if (tick % 10 == 0 && gg.lellek.hud.emote.EmoteState.changed) { gg.lellek.hud.emote.EmoteState.changed = false; gg.lellek.hud.emote.EmoteState.Playing pl = gg.lellek.hud.emote.EmoteState.local(); request("PUT", "/v1/emote/me", pl == null ? "{\"emote\":\"\"}" : "{\"emote\":\"" + pl.emote.id + "\",\"start\":" + pl.start + "}", true, (j, err) -> { if (err != null) LellekHud.log("Emote-Sync: " + err); }); }
        if (tick % 40 == 5) pullEmotes(mc);
        if (tick % 1200 == 100) gg.lellek.hud.emote.Shop.heartbeat();
        if (tick == 20) gg.lellek.hud.emote.Shop.refresh();
    }
    private static void pullEmotes(Minecraft mc) {
        List<UUID> ids = new ArrayList<>();
        for (PlayerInfo i : mc.getConnection().getOnlinePlayers()) if (!i.getProfile().id().equals(mc.player.getUUID())) ids.add(i.getProfile().id());
        if (ids.isEmpty()) return;
        StringBuilder b = new StringBuilder("{\"uuids\":[");
        for (int k = 0; k < ids.size(); k++) { if (k > 0) b.append(','); b.append('"').append(ids.get(k).toString().replace("-", "")).append('"'); }
        request("POST", "/v1/emotes/batch", b.append("]}").toString(), false, (j, err) -> { if (err != null) return; mc.execute(() -> { for (UUID id : ids) { String key = id.toString().replace("-", ""); if (!j.has(key)) { gg.lellek.hud.emote.EmoteState.setOther(id, null, 0); continue; } JsonObject e = j.getAsJsonObject(key); gg.lellek.hud.emote.EmoteState.setOther(id, gg.lellek.hud.emote.Emotes.byId(e.get("emote").getAsString()), e.get("start").getAsLong()); } }); });
    }
    private static void async(Runnable r) { if (busy || serverDown()) return; busy = true; Thread t = new Thread(() -> { try { r.run(); } catch (Throwable e) { LellekHud.log("Cosmetics-Sync: " + e.getMessage()); } finally { busy = false; } }, "LellekHUD-CosmeticSync"); t.setDaemon(true); t.start(); }
    private static HttpURLConnection open(String method, String path, boolean auth) throws IOException {
        HttpURLConnection c = (HttpURLConnection) new URL(base() + path).openConnection();
        c.setRequestMethod(method); c.setConnectTimeout(5000); c.setReadTimeout(8000); c.setRequestProperty("User-Agent", "LellekHUD/" + LellekHud.VERSION);
        if (auth && token != null) c.setRequestProperty("Authorization", "Bearer " + token);
        return c;
    }
    private static JsonObject sendJson(HttpURLConnection c, String body) throws IOException {
        if (body != null) { c.setDoOutput(true); c.setRequestProperty("Content-Type", "application/json"); c.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8)); }
        int code = c.getResponseCode(); InputStream in = code >= 400 ? c.getErrorStream() : c.getInputStream();
        String text = in == null ? "{}" : new String(in.readAllBytes(), StandardCharsets.UTF_8);
        JsonObject j = JsonParser.parseString(text).getAsJsonObject();
        if (code >= 400) throw new IOException(j.has("error") ? j.get("error").getAsString() : "HTTP " + code);
        return j;
    }
    private static void login() throws Exception {
        Minecraft mc = Minecraft.getInstance();
        String serverId = new BigInteger(130, new SecureRandom()).toString(16);
        mc.services().sessionService().joinServer(mc.getUser().getProfileId(), mc.getUser().getAccessToken(), serverId);
        JsonObject j = sendJson(open("POST", "/v1/auth", false), "{\"name\":\"" + mc.getUser().getName() + "\",\"serverId\":\"" + serverId + "\"}");
        token = j.get("token").getAsString();
    }
    private static void push(String cosmetic) {
        async(() -> {
            try {
                if (token == null) login();
                try { sendJson(open("PUT", "/v1/cosmetics/me", true), "{\"cosmetic\":\"" + cosmetic + "\"}"); }
                catch (IOException e) { if (e.getMessage().contains("eingeloggt")) { token = null; login(); sendJson(open("PUT", "/v1/cosmetics/me", true), "{\"cosmetic\":\"" + cosmetic + "\"}"); } else throw e; }
                pushed = cosmetic;
            } catch (java.net.ConnectException | java.net.UnknownHostException ex) { downUntil = System.currentTimeMillis() + 5 * 60 * 1000; pushed = cosmetic; } catch (Exception ex) { LellekHud.log("Cosmetics-Sync: " + ex.getMessage()); pushed = cosmetic; }
        });
    }
    private static void loadCape(Minecraft mc, UUID id) throws IOException {
        try (InputStream in = open("GET", "/v1/capes/" + id.toString().replace("-", "") + ".png", false).getInputStream()) {
            NativeImage img = NativeImage.read(in);
            Identifier loc = Identifier.fromNamespaceAndPath("lellekhud", "cape_" + id.toString().replace("-", ""));
            mc.execute(() -> { mc.getTextureManager().register(loc, new DynamicTexture(() -> "lellek_cape", img)); capes.put(id, loc); });
        }
    }
    private static void pull() {
        Minecraft mc = Minecraft.getInstance(); List<UUID> ids = new ArrayList<>();
        for (PlayerInfo i : mc.getConnection().getOnlinePlayers()) if (!i.getProfile().id().equals(mc.player.getUUID())) ids.add(i.getProfile().id());
        if (ids.isEmpty()) return;
        async(() -> {
            try {
                StringBuilder b = new StringBuilder("{\"uuids\":[");
                for (int k = 0; k < ids.size(); k++) { if (k > 0) b.append(','); b.append('"').append(ids.get(k).toString().replace("-", "")).append('"'); }
                JsonObject j = sendJson(open("POST", "/v1/cosmetics/batch", false), b.append("]}").toString());
                Map<UUID, String> found = new HashMap<>();
                for (UUID id : ids) {
                    String key = id.toString().replace("-", ""); if (!j.has(key)) continue;
                    JsonObject e = j.getAsJsonObject(key); found.put(id, e.get("cosmetic").getAsString());
                    if (e.get("cape").getAsBoolean() && !capes.containsKey(id) && !capeRequested.contains(id)) { capeRequested.add(id); loadCape(mc, id); }
                }
                mc.execute(() -> { for (UUID id : ids) CosmeticLayer.others.remove(id); for (var en : found.entrySet()) if (!en.getValue().isEmpty()) CosmeticLayer.others.put(en.getKey(), en.getValue()); });
            } catch (java.net.ConnectException | java.net.UnknownHostException ex) { downUntil = System.currentTimeMillis() + 5 * 60 * 1000; } catch (Exception ex) { LellekHud.log("Cosmetics-Sync: " + ex.getMessage()); }
        });
    }
    public static void requestOwnCape() {
        Minecraft mc = Minecraft.getInstance(); UUID id = mc.getUser().getProfileId(); capes.remove(id); capeRequested.remove(id);
        async(() -> { try { loadCape(mc, id); } catch (Exception ex) { LellekHud.log("Kein eigenes Cape auf dem Server: " + ex.getMessage()); } });
    }
}
