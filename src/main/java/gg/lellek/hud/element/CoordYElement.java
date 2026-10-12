package gg.lellek.hud.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;

public class CoordYElement extends HudElement {
    public CoordYElement() { super("coordy", "Koordinate Y", TL, 60, 74, false); }
    private String value() { LocalPlayer p = Minecraft.getInstance().player; return p == null ? "0" : String.valueOf(p.getBlockY()); }
    public int getWidth() { return labeledWidth("Y", value()); }
    public int getHeight() { return 9 + PAD * 2; }
    protected void render(GuiGraphicsExtractor g) { labeled(g, "Y", value()); }
}
