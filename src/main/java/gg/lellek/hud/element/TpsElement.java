package gg.lellek.hud.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Geschätzte Server-TPS aus dem Fortschritt der Spielzeit. */
public class TpsElement extends HudElement {
    private long lastGameTime = -1, lastReal; private double tps = 20;
    public TpsElement() { super("tps", "Server-TPS", TL, 4, 214, false); }
    private String value() {
        Minecraft mc = Minecraft.getInstance(); if (mc.level == null) return "-";
        long gt = mc.level.getGameTime(), now = System.currentTimeMillis();
        if (lastGameTime >= 0 && now - lastReal >= 2000) { tps = Math.min(20, (gt - lastGameTime) * 1000.0 / (now - lastReal)); lastGameTime = gt; lastReal = now; }
        if (lastGameTime < 0) { lastGameTime = gt; lastReal = now; }
        return String.format("%.1f", tps);
    }
    public int getWidth() { return labeledWidth("TPS", value()); }
    public int getHeight() { return 9 + PAD * 2; }
    protected void render(GuiGraphicsExtractor g) { String v = value(); bg(g, getWidth(), getHeight()); text(g, "TPS", PAD, PAD, color); text(g, v, PAD + font().width("TPS "), PAD, tps >= 19 ? 0xFF7BD88F : tps >= 15 ? 0xFFFFB400 : 0xFFFF5F5F); }
}
