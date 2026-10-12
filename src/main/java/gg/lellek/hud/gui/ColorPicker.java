package gg.lellek.hud.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import java.awt.Color;

/** Farbwähler: Sättigung/Helligkeit-Feld links, Farbton-Leiste rechts. */
public class ColorPicker {
    public int x, y, size = 96;
    public float hue, sat = 1f, val = 1f;
    private boolean dragSV, dragHue;

    public ColorPicker(int x, int y, int argb) { this.x = x; this.y = y; setColor(argb); }
    public void setColor(int argb) { float[] h = Color.RGBtoHSB((argb >> 16) & 255, (argb >> 8) & 255, argb & 255, null); hue = h[0]; sat = h[1]; val = h[2]; }
    public int getColor() { return 0xFF000000 | (Color.HSBtoRGB(hue, sat, val) & 0xFFFFFF); }
    private int hueX() { return x + size + 8; }

    public void draw(GuiGraphicsExtractor g) {
        int pure = Color.HSBtoRGB(hue, 1f, 1f) & 0xFFFFFF;
        for (int i = 0; i < size; i++) { // Spalte: weiß→Farbton, darüber schwarz nach unten
            float f = i / (float) (size - 1);
            int r = (int) (255 + (((pure >> 16) & 255) - 255) * f), gr = (int) (255 + (((pure >> 8) & 255) - 255) * f), b = (int) (255 + ((pure & 255) - 255) * f);
            g.fillGradient(x + i, y, x + i + 1, y + size, 0xFF000000 | (r << 16) | (gr << 8) | b, 0xFF000000);
        }
        for (int i = 0; i < 6; i++) {
            int c1 = Color.HSBtoRGB(i / 6f, 1f, 1f) | 0xFF000000, c2 = Color.HSBtoRGB((i + 1) / 6f, 1f, 1f) | 0xFF000000;
            g.fillGradient(hueX(), y + i * size / 6, hueX() + 12, y + (i + 1) * size / 6, c1, c2);
        }
        int mx = x + Math.round(sat * (size - 1)), my = y + Math.round((1 - val) * (size - 1));
        g.fill(mx - 2, my - 2, mx + 3, my + 3, 0xFF000000); g.fill(mx - 1, my - 1, mx + 2, my + 2, 0xFFFFFFFF);
        int hy = y + Math.round(hue * (size - 1));
        g.fill(hueX() - 2, hy - 1, hueX() + 14, hy + 2, 0xFFFFFFFF);
        g.fill(x, y + size + 6, x + size + 20, y + size + 22, getColor());
    }

    public boolean mouseClicked(double mx, double my) {
        dragSV = mx >= x && mx < x + size && my >= y && my < y + size;
        dragHue = mx >= hueX() - 2 && mx < hueX() + 14 && my >= y && my < y + size;
        mouseDragged(mx, my);
        return dragSV || dragHue;
    }
    public void mouseDragged(double mx, double my) {
        if (dragSV) { sat = clamp((float) (mx - x) / (size - 1)); val = 1f - clamp((float) (my - y) / (size - 1)); }
        if (dragHue) hue = clamp((float) (my - y) / (size - 1));
    }
    public void mouseReleased() { dragSV = dragHue = false; }
    private static float clamp(float f) { return Math.max(0f, Math.min(1f, f)); }
}
