package gg.lellek.hud.gui;

import gg.lellek.hud.LellekHud;
import gg.lellek.hud.cosmetic.Cosmetics;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Auswahlrad auf X: Maus in Richtung des Cosmetics, klicken oder Taste loslassen. */
public class CosmeticWheelScreen extends Screen {
    private static final int INNER = 28, OUTER = 78;
    private final long opened = System.currentTimeMillis();
    private int hovered = -1;

    public CosmeticWheelScreen() { super(Component.literal("Cosmetics")); }
    private int slots() { return Cosmetics.ALL.size() + 1; }
    private String idOf(int i) { return i == 0 ? "" : Cosmetics.ALL.get(i - 1).id; }
    private String nameOf(int i) { return i == 0 ? "Keins" : Cosmetics.ALL.get(i - 1).name; }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float pt) {
        super.extractRenderState(g, mouseX, mouseY, pt);
        int cx = width / 2, cy = height / 2, n = slots();
        float grow = Math.min(1f, (System.currentTimeMillis() - opened) / 160f); grow = 1 - (1 - grow) * (1 - grow);
        int inner = (int) (INNER * grow), outer = (int) (OUTER * grow);
        double dx = mouseX - cx, dy = mouseY - cy;
        hovered = -1;
        if (dx * dx + dy * dy > inner * inner) hovered = (int) (((Math.toDegrees(Math.atan2(dy, dx)) + 90 + 360 + 180f / n) % 360) / (360f / n));
        // Ring über Scanlines (pro Zeile zwei Füllungen), Sektorfarbe nach Winkel
        for (int yy = -outer; yy <= outer; yy++) {
            int half = (int) Math.sqrt(outer * outer - yy * yy);
            int ih = Math.abs(yy) < inner ? (int) Math.sqrt(inner * inner - yy * yy) : 0;
            for (int xx = -half; xx <= half; xx += 2) {
                if (Math.abs(xx) < ih) continue;
                int slot = (int) (((Math.toDegrees(Math.atan2(yy, xx)) + 90 + 360 + 180f / n) % 360) / (360f / n));
                boolean sel = slot == hovered, active = idOf(slot).equals(LellekHud.config.cosmetic);
                g.fill(cx + xx, cy + yy, cx + xx + 2, cy + yy + 1, sel ? 0xC0FFB400 : active ? 0xA0FF8F1C : 0xA0140F08);
            }
        }
        for (int i = 0; i < n && grow > 0.9f; i++) {
            double mid = Math.toRadians(-90 + i * 360.0 / n);
            int tx = (int) (cx + Math.cos(mid) * (inner + outer) / 2), ty = (int) (cy + Math.sin(mid) * (inner + outer) / 2);
            gg.lellek.hud.gui.Gfx.centered(g, font, nameOf(i), tx, ty - 4, i == hovered ? 0xFF1A1000 : 0xFFFFFFFF);
        }
        gg.lellek.hud.gui.Gfx.centered(g, font, "Cosmetics", cx, cy - 8, 0xFFFFB400);
        gg.lellek.hud.gui.Gfx.centered(g, font, hovered >= 0 ? nameOf(hovered) : "", cx, cy + 4, 0xFFAAAAAA);
    }
    private void choose() { if (hovered >= 0) { LellekHud.config.cosmetic = idOf(hovered); LellekHud.config.save(); } minecraft.gui.setScreen(null); }
    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean dbl) { choose(); return true; }
    @Override
    public void tick() { if (!LellekHud.cosmeticKey.isDown() && System.currentTimeMillis() - opened > 250) choose(); }
    @Override
    public boolean isPauseScreen() { return false; }
}
