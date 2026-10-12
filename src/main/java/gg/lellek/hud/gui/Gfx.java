package gg.lellek.hud.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Kleine Brücke zu den umbenannten Zeichenmethoden von 26.x. */
public final class Gfx {
    public static void text(GuiGraphicsExtractor g, Font f, String s, int x, int y, int color) { g.text(f, s, x, y, color, true); }
    public static void text(GuiGraphicsExtractor g, Font f, String s, int x, int y, int color, boolean shadow) { g.text(f, s, x, y, color, shadow); }
    public static void text(GuiGraphicsExtractor g, Font f, net.minecraft.util.FormattedCharSequence s, int x, int y, int color) { g.text(f, s, x, y, color, true); }
    public static void centered(GuiGraphicsExtractor g, Font f, String s, int cx, int y, int color) { g.text(f, s, cx - f.width(s) / 2, y, color, true); }
}
