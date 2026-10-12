package gg.lellek.hud.element;

import gg.lellek.hud.spotify.SpotifyClient;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class SpotifyElement extends HudElement {
    private static final int W = 150, H = 26;
    public SpotifyElement() { super("spotify", "Spotify", BC, 0, 46); }
    public int getWidth() { return W; }
    public int getHeight() { return H; }
    private static String time(int ms) { int s = ms / 1000; return (s / 60) + ":" + String.format("%02d", s % 60); }

    protected void render(GuiGraphicsExtractor g) {
        SpotifyClient sp = SpotifyClient.get();
        bg(g, W, H);
        if (!sp.connected) { text(g, "Spotify", PAD, PAD, color); text(g, "nicht verbunden", PAD + 40, PAD, 0xFF888888); return; }
        if (sp.title.isEmpty()) { text(g, "Spotify", PAD, PAD, color); text(g, "nichts läuft", PAD + 40, PAD, 0xFF888888); return; }
        String line = (sp.playing ? "> " : "|| ") + sp.title + " - " + sp.artist;
        text(g, font().plainSubstrByWidth(line, W - PAD * 2), PAD, PAD, WHITE);
        int p = sp.liveProgress();
        String t = time(p) + " / " + time(sp.durationMs);
        text(g, t, W - PAD - font().width(t), 14, 0xFFAAAAAA);
        int barW = W - PAD * 2 - font().width(t) - 4;
        int fill = sp.durationMs > 0 ? (int) ((long) barW * p / sp.durationMs) : 0;
        g.fill(PAD, 17, PAD + barW, 20, 0x60FFFFFF);
        g.fill(PAD, 17, PAD + fill, 20, color);
    }
}
