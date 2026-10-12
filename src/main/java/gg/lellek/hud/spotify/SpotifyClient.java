package gg.lellek.hud.spotify;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Liest die vom LellekClient-Launcher abgelegte Datei lellek/spotify.json
 * (clientId, accessToken, refreshToken, expiresAt) und fragt alle 3 Sekunden
 * den aktuellen Titel ab. Token-Refresh läuft ohne Client-Secret (PKCE).
 */
public class SpotifyClient {
    private static final SpotifyClient INSTANCE = new SpotifyClient();
    public static SpotifyClient get() { return INSTANCE; }

    private File file;
    private String clientId, accessToken, refreshToken;
    private long expiresAt;
    private long lastPoll;

    public volatile boolean connected, playing;
    public volatile String title = "", artist = "";
    public volatile int progressMs, durationMs;
    private volatile long progressAt;

    public void start() {
        file = new File(Minecraft.getInstance().gameDirectory, "lellek/spotify.json");
        Thread t = new Thread(new Runnable() { public void run() { loop(); } }, "LellekHUD-Spotify");
        t.setDaemon(true);
        t.start();
    }

    private void loop() {
        while (true) {
            try {
                if (!load()) { connected = false; Thread.sleep(5000); continue; }
                poll();
                Thread.sleep(3000);
            } catch (InterruptedException e) { return; }
            catch (Exception e) { connected = false; try { Thread.sleep(5000); } catch (InterruptedException ignored) { return; } }
        }
    }

    private boolean load() throws IOException {
        if (!file.exists()) return false;
        if (file.lastModified() <= lastPoll && accessToken != null) return true;
        lastPoll = file.lastModified();
        JsonObject j = new JsonParser().parse(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)).getAsJsonObject();
        clientId = j.get("clientId").getAsString();
        accessToken = j.get("accessToken").getAsString();
        refreshToken = j.get("refreshToken").getAsString();
        expiresAt = j.get("expiresAt").getAsLong();
        return true;
    }

    private void save() throws IOException {
        JsonObject j = new JsonObject();
        j.addProperty("clientId", clientId);
        j.addProperty("accessToken", accessToken);
        j.addProperty("refreshToken", refreshToken);
        j.addProperty("expiresAt", expiresAt);
        Writer w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
        w.write(j.toString()); w.close();
        lastPoll = file.lastModified();
    }

    private void refresh() throws IOException {
        String body = "grant_type=refresh_token&refresh_token=" + URLEncoder.encode(refreshToken, "UTF-8") + "&client_id=" + URLEncoder.encode(clientId, "UTF-8");
        HttpURLConnection c = (HttpURLConnection) new URL("https://accounts.spotify.com/api/token").openConnection();
        c.setRequestMethod("POST");
        c.setDoOutput(true);
        c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        c.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
        if (c.getResponseCode() != 200) throw new IOException("Refresh " + c.getResponseCode());
        JsonObject j = new JsonParser().parse(new InputStreamReader(c.getInputStream(), StandardCharsets.UTF_8)).getAsJsonObject();
        accessToken = j.get("access_token").getAsString();
        if (j.has("refresh_token")) refreshToken = j.get("refresh_token").getAsString();
        expiresAt = System.currentTimeMillis() + j.get("expires_in").getAsLong() * 1000L - 30000L;
        save();
    }

    private HttpURLConnection request(String method, String path) throws IOException {
        if (System.currentTimeMillis() > expiresAt) refresh();
        HttpURLConnection c = (HttpURLConnection) new URL("https://api.spotify.com/v1" + path).openConnection();
        c.setRequestMethod(method);
        c.setRequestProperty("Authorization", "Bearer " + accessToken);
        c.setConnectTimeout(4000); c.setReadTimeout(4000);
        if (!method.equals("GET")) { c.setDoOutput(true); c.setFixedLengthStreamingMode(0); c.getOutputStream().close(); }
        return c;
    }

    private void poll() throws IOException {
        HttpURLConnection c = request("GET", "/me/player/currently-playing");
        int code = c.getResponseCode();
        if (code == 401) { refresh(); return; }
        connected = true;
        if (code == 204) { playing = false; title = ""; return; }
        if (code != 200) return;
        JsonObject j = new JsonParser().parse(new InputStreamReader(c.getInputStream(), StandardCharsets.UTF_8)).getAsJsonObject();
        playing = j.has("is_playing") && j.get("is_playing").getAsBoolean();
        progressMs = j.has("progress_ms") && !j.get("progress_ms").isJsonNull() ? j.get("progress_ms").getAsInt() : 0;
        progressAt = System.currentTimeMillis();
        if (j.has("item") && !j.get("item").isJsonNull()) {
            JsonObject item = j.getAsJsonObject("item");
            title = item.get("name").getAsString();
            durationMs = item.get("duration_ms").getAsInt();
            StringBuilder a = new StringBuilder();
            if (item.has("artists")) for (com.google.gson.JsonElement e : item.getAsJsonArray("artists")) {
                if (a.length() > 0) a.append(", ");
                a.append(e.getAsJsonObject().get("name").getAsString());
            }
            artist = a.toString();
        }
    }

    /** Fortschritt inkl. der seit dem letzten Abruf vergangenen Zeit */
    public int liveProgress() {
        if (!playing) return progressMs;
        return (int) Math.min(durationMs, progressMs + (System.currentTimeMillis() - progressAt));
    }

    private void control(final String method, final String path) {
        if (!connected) return;
        new Thread(new Runnable() { public void run() {
            try { request(method, path).getResponseCode(); Thread.sleep(300); poll(); } catch (Exception ignored) {}
        } }).start();
    }
    public void togglePlay() { control("PUT", playing ? "/me/player/pause" : "/me/player/play"); }
    public void next() { control("POST", "/me/player/next"); }
    public void previous() { control("POST", "/me/player/previous"); }
}
