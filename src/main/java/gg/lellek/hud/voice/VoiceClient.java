package gg.lellek.hud.voice;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import gg.lellek.hud.LellekHud;
import gg.lellek.hud.feature.Mods;
import net.minecraft.client.Minecraft;

import javax.sound.sampled.*;
import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Proximity-Voice: Push-to-Talk (Taste B), Mikrofon → µ-law → UDP → Server → Mitspieler in Hörweite.
 * Empfang: pro Sprecher ein kleiner Puffer, Mixer summiert alle 20 ms mit Lautstärke nach Entfernung.
 */
public class VoiceClient {
    public static final int RATE = 16000, FRAME = 320; // 20 ms
    private static final AudioFormat FORMAT = new AudioFormat(RATE, 16, 1, true, false);

    private static VoiceClient inst;
    public static VoiceClient get() { if (inst == null) inst = new VoiceClient(); return inst; }

    private DatagramSocket socket; private InetAddress host; private int port; private byte[] token;
    private float range = 48; private int seq, failures; private boolean connected, connecting;
    public volatile boolean talking; private int tick;
    private TargetDataLine mic; private SourceDataLine out;
    private final Map<Integer, Speaker> speakers = new ConcurrentHashMap<Integer, Speaker>();
    private final Map<Integer, String> names = new ConcurrentHashMap<Integer, String>();

    static class Speaker { final Deque<short[]> frames = new ArrayDeque<short[]>(); volatile float distance; volatile long last; }

    public boolean isConnected() { return connected; }
    /** Namen der Spieler, die gerade sprechen (letzte 300 ms) */
    public List<String> speaking() { List<String> l = new ArrayList<String>(); long now = System.currentTimeMillis(); for (Map.Entry<Integer, Speaker> e : speakers.entrySet()) if (now - e.getValue().last < 300) { String n = names.get(e.getKey()); l.add(n == null ? "?" : n); } return l; }
    public int nearby() { return names.size(); }

    private static String base() {
        String s = Mods.VOICE.textOf("server").trim();
        if (s.equals("http://127.0.0.1:8766")) { try { File f = new File(Minecraft.getInstance().gameDirectory, "lellek/voice-server.txt"); if (f.exists()) { String t = new Scanner(f, "UTF-8").useDelimiter("\\A").next().trim(); if (!t.isEmpty()) s = t; } } catch (Exception ignored) {} }
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }

    // ---------- Verbindung ----------
    private void connect() {
        if (connecting || connected) return; connecting = true;
        new Thread(new Runnable() { public void run() {
            try {
                Minecraft mc = Minecraft.getInstance();
                String serverId = new BigInteger(130, new SecureRandom()).toString(16);
                mc.services().sessionService().joinServer(mc.getUser().getProfileId(), mc.getUser().getAccessToken(), serverId);
                HttpURLConnection c = (HttpURLConnection) new URL(base() + "/v1/auth").openConnection();
                c.setRequestMethod("POST"); c.setDoOutput(true); c.setConnectTimeout(5000); c.setReadTimeout(8000); c.setRequestProperty("Content-Type", "application/json");
                c.getOutputStream().write(("{\"name\":\"" + mc.getUser().getName() + "\",\"serverId\":\"" + serverId + "\"}").getBytes(StandardCharsets.UTF_8));
                InputStream in = c.getResponseCode() >= 400 ? c.getErrorStream() : c.getInputStream();
                JsonObject j = new JsonParser().parse(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
                if (j.has("error")) throw new IOException(j.get("error").getAsString());
                token = hex(j.get("token").getAsString()); port = j.get("udpPort").getAsInt(); range = j.has("range") ? j.get("range").getAsFloat() : 48;
                URL u = new URL(base()); host = InetAddress.getByName(u.getHost());
                socket = new DatagramSocket(); socket.setSoTimeout(1000);
                openAudio();
                connected = true;
                Thread r = new Thread(new Runnable() { public void run() { receiveLoop(); } }, "LellekHUD-VoiceRx"); r.setDaemon(true); r.start();
                Thread m = new Thread(new Runnable() { public void run() { mixLoop(); } }, "LellekHUD-VoiceMix"); m.setDaemon(true); m.start();
                Thread t = new Thread(new Runnable() { public void run() { micLoop(); } }, "LellekHUD-VoiceMic"); t.setDaemon(true); t.start();
                LellekHud.log("Voice verbunden mit " + base());
            } catch (Exception e) { if (++failures <= 2 || failures % 20 == 0) LellekHud.log("Voice: " + e.getMessage() + (failures == 2 ? " – weitere Versuche werden nicht mehr geloggt" : "")); }
            finally { connecting = false; }
        } }, "LellekHUD-VoiceConnect").start();
    }
    public void disconnect() {
        if (socket != null && token != null) { try { byte[] b = new byte[9]; b[0] = 3; System.arraycopy(token, 0, b, 1, 8); socket.send(new DatagramPacket(b, 9, host, port)); } catch (Exception ignored) {} }
        connected = false; if (socket != null) socket.close(); socket = null;
        if (mic != null) { mic.close(); mic = null; } if (out != null) { out.close(); out = null; }
        speakers.clear(); names.clear();
    }
    private static byte[] hex(String s) { byte[] b = new byte[s.length() / 2]; for (int i = 0; i < b.length; i++) b[i] = (byte) Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16); return b; }

    private void openAudio() throws LineUnavailableException {
        mic = AudioSystem.getTargetDataLine(FORMAT); mic.open(FORMAT, FRAME * 8); mic.start();
        out = AudioSystem.getSourceDataLine(FORMAT); out.open(FORMAT, FRAME * 16); out.start();
    }

    // ---------- Tick: an/aus, Position ----------
    public void tick(Minecraft mc) {
        boolean want = Mods.VOICE.enabled && mc.player != null && mc.level != null;
        if (!want) { if (connected) disconnect(); talking = false; return; }
        if (!connected) { if (++tick % (failures > 2 ? 1200 : 100) == 1) connect(); return; }
        talking = LellekHud.voiceKey.isDown() && LellekHud.screen(mc) == null;
        if (++tick % 10 == 0) { // alle 500 ms Position
            ByteBuffer b = ByteBuffer.allocate(25).order(ByteOrder.LITTLE_ENDIAN);
            b.put((byte) 1).put(token).putFloat((float) mc.player.getX()).putFloat((float) mc.player.getY()).putFloat((float) mc.player.getZ()).putInt(worldHash());
            send(b.array());
        }
    }
    private static int worldHash() { return gg.lellek.hud.waypoint.Waypoints.currentWorld().hashCode(); }
    private void send(byte[] data) { try { if (socket != null) socket.send(new DatagramPacket(data, data.length, host, port)); } catch (IOException ignored) {} }

    // ---------- Mikrofon → Server ----------
    private void micLoop() {
        byte[] pcm = new byte[FRAME * 2]; byte[] pkt = new byte[13 + FRAME];
        while (connected && mic != null) {
            int n = mic.read(pcm, 0, pcm.length); if (n <= 0) continue;
            if (!talking) continue;
            float gain = Mods.VOICE.intOf("micgain") / 100f;
            ByteBuffer b = ByteBuffer.wrap(pkt).order(ByteOrder.LITTLE_ENDIAN);
            b.put((byte) 2).put(token).putInt(seq++);
            for (int i = 0; i < FRAME; i++) { int s = (short) ((pcm[i * 2] & 0xFF) | (pcm[i * 2 + 1] << 8)); s = Math.max(-32768, Math.min(32767, (int) (s * gain))); pkt[13 + i] = ULaw.encode((short) s); }
            send(pkt);
        }
    }

    // ---------- Server → Puffer ----------
    private void receiveLoop() {
        byte[] buf = new byte[2048];
        while (connected && socket != null) {
            try {
                DatagramPacket p = new DatagramPacket(buf, buf.length); socket.receive(p);
                ByteBuffer b = ByteBuffer.wrap(buf, 0, p.getLength()).order(ByteOrder.LITTLE_ENDIAN);
                int type = b.get() & 0xFF;
                if (type == 0x12) {
                    int id = b.getInt(); b.getInt(); float dist = b.getFloat();
                    short[] frame = new short[b.remaining()]; for (int i = 0; i < frame.length; i++) frame[i] = ULaw.decode(b.get());
                    Speaker s = speakers.get(id); if (s == null) speakers.put(id, s = new Speaker());
                    s.distance = dist; s.last = System.currentTimeMillis();
                    synchronized (s.frames) { if (s.frames.size() > 8) s.frames.pollFirst(); s.frames.addLast(frame); }
                } else if (type == 0x13) {
                    int n = b.get() & 0xFF; Map<Integer, String> fresh = new HashMap<Integer, String>();
                    for (int i = 0; i < n; i++) { int id = b.getInt(); int len = b.get() & 0xFF; byte[] nb = new byte[len]; b.get(nb); fresh.put(id, new String(nb, StandardCharsets.UTF_8)); }
                    names.clear(); names.putAll(fresh);
                }
            } catch (SocketTimeoutException ignored) {} catch (Exception e) { if (connected) LellekHud.log("Voice-Empfang: " + e.getMessage()); }
        }
    }

    // ---------- Mixer → Lautsprecher ----------
    private void mixLoop() {
        int[] mix = new int[FRAME]; byte[] outBuf = new byte[FRAME * 2];
        while (connected && out != null) {
            Arrays.fill(mix, 0); boolean any = false;
            float vol = Mods.VOICE.intOf("volume") / 100f;
            for (Speaker s : speakers.values()) {
                short[] f; synchronized (s.frames) { f = s.frames.pollFirst(); }
                if (f == null) continue; any = true;
                float g = vol * Math.max(0f, 1f - s.distance / range);
                for (int i = 0; i < Math.min(FRAME, f.length); i++) mix[i] += (int) (f[i] * g);
            }
            if (!any) { try { Thread.sleep(10); } catch (InterruptedException e) { return; } continue; }
            for (int i = 0; i < FRAME; i++) { int s = Math.max(-32768, Math.min(32767, mix[i])); outBuf[i * 2] = (byte) s; outBuf[i * 2 + 1] = (byte) (s >> 8); }
            out.write(outBuf, 0, outBuf.length);
        }
    }
}
