package gg.lellek.hud;

import gg.lellek.hud.gui.HudEditorScreen;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Zeichnet alle aktiven Elemente über das Spiel-HUD – registriert als Lambda in LellekHud. */
public class HudRenderer {
    private static String toastText; private static int toastColor; private static long toastUntil;
    /** Kurze Einblendung über der Hotbar (eigener Ersatz für die Actionbar) */
    public static void toast(String text, int color) { toastText = text; toastColor = color; toastUntil = System.currentTimeMillis() + 4000; }
    private static void drawToast(GuiGraphicsExtractor g) {
        if (toastText == null || System.currentTimeMillis() > toastUntil) return;
        var font = Minecraft.getInstance().font; int w = font.width(toastText) + 12, x = (g.guiWidth() - w) / 2, y = g.guiHeight() - 72;
        g.fill(x, y, x + w, y + 14, 0xC0000000); g.fill(x, y, x + 2, y + 14, toastColor);
        g.text(font, toastText, x + 8, y + 3, toastColor, true);
    }
    public static void draw(GuiGraphicsExtractor g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getDebugOverlay().showDebugScreen() || LellekHud.screen(mc) instanceof HudEditorScreen || (LellekHud.screen(mc) instanceof gg.lellek.hud.gui.ModMenuScreen mm && mm.isLayoutMode())) return;
        drawToast(g);
        if (LellekHud.features != null && LellekHud.features.showZoomIndicator()) {
            String t = String.format(java.util.Locale.ROOT, "%.1fx", LellekHud.features.zoomLevel());
            var font = mc.font; int tw = font.width(t) + 10, zx = (g.guiWidth() - tw) / 2, zy = g.guiHeight() / 2 + 14;
            g.fill(zx, zy, zx + tw, zy + 13, 0xA0000000); g.text(font, t, zx + 5, zy + 3, 0xFFFFB400, true);
        }
        int w = g.guiWidth(), h = g.guiHeight();
        for (gg.lellek.hud.element.HudElement el : LellekHud.config.elements) {
            if (!el.enabled) continue;
            try { el.layout(w, h); el.draw(g); } catch (Throwable t) { el.enabled = false; LellekHud.log("Element " + el.id + " deaktiviert: " + t); }
        }
    }
}
