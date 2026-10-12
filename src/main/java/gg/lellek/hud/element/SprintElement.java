package gg.lellek.hud.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class SprintElement extends HudElement {
    public SprintElement() { super("sprint", "Sprint/Sneak", BL, 4, 130, false); }
    private String value() { Minecraft mc = Minecraft.getInstance(); if (mc.player == null) return ""; return mc.player.isShiftKeyDown() ? "Schleicht" : mc.player.isSprinting() ? "Sprintet" : mc.player.zza != 0 || mc.player.xxa != 0 ? "Geht" : "Steht"; }
    public int getWidth() { return font().width("Schleicht") + PAD * 2; }
    public int getHeight() { return 9 + PAD * 2; }
    protected void render(GuiGraphicsExtractor g) { bg(g, getWidth(), getHeight()); text(g, value(), PAD, PAD, color); }
}
