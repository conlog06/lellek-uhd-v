package gg.lellek.hud.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Kleine Brücke zu den umbenannten Zeichenmethoden von 26.x. */
public final class Gfx {
    public static void text(GuiGraphicsExtractor g, Font f, String s, int x, int y, int color) { g.text(f, s, x, y, color, true); }
    public static void text(GuiGraphicsExtractor g, Font f, String s, int x, int y, int color, boolean shadow) { g.text(f, s, x, y, color, shadow); }
    public static void text(GuiGraphicsExtractor g, Font f, net.minecraft.util.FormattedCharSequence s, int x, int y, int color) { g.text(f, s, x, y, color, true); }
    public static void centered(GuiGraphicsExtractor g, Font f, String s, int cx, int y, int color) { g.text(f, s, cx - f.width(s) / 2, y, color, true); }

    private static java.lang.reflect.Method previewMethod; private static boolean previewSearched;
    /** Eigenen Spieler in einem Rechteck zeichnen (InventoryScreen-Methode per Reflection, Name variiert je Version) */
    public static boolean playerPreview(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, int mx, int my) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player == null) return false;
        if (!previewSearched) {
            previewSearched = true;
            for (java.lang.reflect.Method m : net.minecraft.client.gui.screens.inventory.InventoryScreen.class.getMethods())
                if (java.lang.reflect.Modifier.isStatic(m.getModifiers()) && m.getName().toLowerCase().contains("entity") && m.getParameterCount() >= 6 && m.getParameterTypes()[0] == GuiGraphicsExtractor.class) { previewMethod = m; break; }
        }
        if (previewMethod == null) return false;
        try {
            Class<?>[] pt = previewMethod.getParameterTypes(); Object[] args = new Object[pt.length];
            int size = Math.max(30, Math.min(x2 - x1, y2 - y1) / 2 - 10);
            int[] ints = { x1, y1, x2, y2, size }; float[] floats = { 0.0625f, mx, my }; int ii = 0, fi = 0;
            for (int i = 0; i < pt.length; i++) {
                if (pt[i] == GuiGraphicsExtractor.class) args[i] = g;
                else if (pt[i] == int.class) args[i] = ii < ints.length ? ints[ii++] : 0;
                else if (pt[i] == float.class) args[i] = fi < floats.length ? floats[fi++] : 0f;
                else if (pt[i].isInstance(mc.player)) args[i] = mc.player;
                else return false;
            }
            previewMethod.invoke(null, args); return true;
        } catch (Throwable t) { previewMethod = null; return false; }
    }
}
