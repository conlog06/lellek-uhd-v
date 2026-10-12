package gg.lellek.hud.element;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Stoppuhr: Taste (Standard: Ende) startet/stoppt, langer Druck (im Mod-Menü) setzt zurück. */
public class StopwatchElement extends HudElement {
    public static long start, accumulated; public static boolean running;
    public static void toggle() { if (running) { accumulated += System.currentTimeMillis() - start; running = false; } else { start = System.currentTimeMillis(); running = true; } }
    public static void reset() { running = false; accumulated = 0; }
    public StopwatchElement() { super("stopwatch", "Stoppuhr", TC, 0, 24, false); }
    private String value() { long ms = accumulated + (running ? System.currentTimeMillis() - start : 0); return String.format("%02d:%02d.%d", ms / 60000, (ms / 1000) % 60, (ms / 100) % 10); }
    public int getWidth() { return labeledWidth("\u23f1", value()); }
    public int getHeight() { return 9 + PAD * 2; }
    protected void render(GuiGraphicsExtractor g) { bg(g, getWidth(), getHeight()); text(g, "\u23f1", PAD, PAD, running ? 0xFF7BD88F : color); text(g, value(), PAD + font().width("\u23f1 "), PAD, WHITE); }
}
