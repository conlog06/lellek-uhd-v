package gg.lellek.hud.gui;

import gg.lellek.hud.LellekHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;

/** Hintergrund und Logo des Hauptmenüs (von den Mixins aufgerufen). */
public class LellekMenuBackground {
    private static final long START = System.currentTimeMillis();

    public static void draw(GuiGraphicsExtractor g) {
        int w = g.guiWidth(), h = g.guiHeight();
        g.fillGradient(0, 0, w, h, 0xFF120D07, 0xFF2E1B08);
        float t = (System.currentTimeMillis() - START) / 1000f;
        int horizon = h * 55 / 100;
        for (int i = 0; i < 9; i++) {
            float f = ((i + t * 0.6f) % 9) / 9f;
            int y = horizon + (int) (f * f * (h - horizon));
            g.fill(0, y, w, y + 1, ((int) (f * 90) << 24) | 0xFFAA00);
        }
        for (int i = -6; i <= 6; i++) {
            int xTop = w / 2 + i * 26, xBottom = w / 2 + i * (w / 4);
            int steps = h - horizon;
            for (int s = 0; s <= steps; s += 3) { int px = xTop + (xBottom - xTop) * s / Math.max(1, steps); g.fill(px, horizon + s, px + 1, horizon + s + 1, 0x40FFAA00); }
        }
        g.fillGradient(0, horizon - 40, w, horizon + 30, 0x00120D07, 0xFF120D07);
        g.fillGradient(0, horizon + 30, w, horizon + 60, 0xFF120D07, 0x00120D07);
        String s = LellekHud.CLIENT_NAME + " " + LellekHud.MC_VERSION + " \u00b7 HUD " + LellekHud.VERSION;
        gg.lellek.hud.gui.Gfx.text(g, Minecraft.getInstance().font, s, 2, h - 20, 0xFFFFB400, true);
    }

    public static void drawLogo(GuiGraphicsExtractor g, int screenWidth) {
        float t = (System.currentTimeMillis() - START) / 1000f;
        int size = Math.min(150, g.guiHeight() * 2 / 5);
        int x = screenWidth / 2 - size / 2, y = Math.max(6, g.guiHeight() / 4 - size / 2 - 6) + (int) (Math.sin(t * 1.2) * 3);
        g.blit(RenderPipelines.GUI_TEXTURED, LellekHud.LOGO, x, y, 0, 0, size, size, size, size);
    }
}
