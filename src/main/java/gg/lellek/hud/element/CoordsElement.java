package gg.lellek.hud.element;

import gg.lellek.hud.feature.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;

public class CoordsElement extends HudElement {
    public CoordsElement() { super("coords", "Koordinaten (XYZ)", TL, 4, 46); add(new Setting.Choice("layout", "Anordnung", 0, "Eine Zeile", "Drei Zeilen")); add(new Setting.Bool("facing", "Blickrichtung anzeigen", true)); }
    public static String facing(LocalPlayer p) { return switch (p.getDirection()) { case NORTH -> "N"; case SOUTH -> "S"; case WEST -> "W"; case EAST -> "O"; default -> ""; }; }
    private String[] lines() {
        LocalPlayer p = Minecraft.getInstance().player; int x = p == null ? 0 : p.getBlockX(), y = p == null ? 0 : p.getBlockY(), z = p == null ? 0 : p.getBlockZ();
        String f = p != null && boolOf("facing") ? " " + facing(p) : "";
        if (choiceOf("layout") == 0) return new String[] { "XYZ|" + x + " " + y + " " + z + f };
        return new String[] { "X|" + x, "Y|" + y, "Z|" + z + f };
    }
    public int getWidth() { int w = 0; for (String l : lines()) { String[] p = l.split("\\|"); w = Math.max(w, labeledWidth(p[0], p[1])); } return w; }
    public int getHeight() { return lines().length * 10 + PAD * 2 - 1; }
    protected void render(GuiGraphicsExtractor g) { String[] ls = lines(); bg(g, getWidth(), getHeight()); int y = PAD; for (String l : ls) { String[] p = l.split("\\|"); text(g, p[0], PAD, y, color); text(g, p[1], PAD + font().width("XYZ "), y, WHITE); y += 10; } }
}
