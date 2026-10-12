package gg.lellek.hud.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Sitzung: Spielzeit seit Start, Tode. */
public class SessionElement extends HudElement {
    private static final long START = System.currentTimeMillis(); public static int deaths; private static boolean wasDead;
    public SessionElement() { super("session", "Sitzung", TL, 4, 228, false); }
    public static void tick(Minecraft mc) { if (mc.player == null) return; boolean dead = mc.player.isDeadOrDying(); if (dead && !wasDead) deaths++; wasDead = dead; }
    private String value() { long m = (System.currentTimeMillis() - START) / 60000; return (m / 60 > 0 ? m / 60 + "h " : "") + m % 60 + "min \u00b7 " + deaths + " Tode"; }
    public int getWidth() { return labeledWidth("Sitzung", value()); }
    public int getHeight() { return 9 + PAD * 2; }
    protected void render(GuiGraphicsExtractor g) { labeled(g, "Sitzung", value()); }
}
