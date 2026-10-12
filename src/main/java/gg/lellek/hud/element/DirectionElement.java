package gg.lellek.hud.element;

import gg.lellek.hud.feature.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class DirectionElement extends HudElement {
    private static final String[] NAMES = { "S", "SW", "W", "NW", "N", "NO", "O", "SO" };
    public DirectionElement() { super("direction", "Richtung", TC, 0, 4); add(new Setting.Choice("style", "Stil", 0, "Leiste", "Kompakt")); add(new Setting.Bool("degrees", "Grad anzeigen", true)); add(new Setting.Int("width", "Breite", 140, 80, 300, "px")); }
    private float yaw() { Minecraft mc = Minecraft.getInstance(); return mc.player == null ? 0 : ((mc.player.getYRot() % 360) + 360) % 360; }
    private String compact() { float y = yaw(); String n = NAMES[Math.round(y / 45f) % 8]; return boolOf("degrees") ? n + " " + (int) y + "\u00b0" : n; }
    public int getWidth() { return choiceOf("style") == 1 ? font().width(compact()) + PAD * 2 : intOf("width"); }
    public int getHeight() { return choiceOf("style") == 1 ? 9 + PAD * 2 : 16; }
    protected void render(GuiGraphicsExtractor g) {
        if (choiceOf("style") == 1) { bg(g, getWidth(), getHeight()); text(g, compact(), PAD, PAD, color); return; }
        int W = intOf("width"), H = 16; bg(g, W, H); float yaw = yaw();
        for (int i = 0; i < 8; i++) { float diff = ((i * 45f - yaw + 540) % 360) - 180; if (Math.abs(diff) > 90) continue; int x = W / 2 + (int) (diff / 90f * (W / 2f - 8)); boolean major = i % 2 == 0; text(g, NAMES[i], x - font().width(NAMES[i]) / 2, major ? 2 : 4, major ? color : 0xFFAAAAAA); }
        g.fill(W / 2, 11, W / 2 + 1, H - 1, WHITE);
        if (boolOf("degrees")) { String deg = (int) yaw + "\u00b0"; text(g, deg, W - font().width(deg) - 2, H - 9, 0xFFAAAAAA); }
    }
}
