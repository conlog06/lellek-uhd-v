package gg.lellek.hud.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;

public class CoordXElement extends HudElement {
    public CoordXElement() { super("coordx", "Koordinate X", TL, 60, 60, false); }
    private String value() { LocalPlayer p = Minecraft.getInstance().player; return p == null ? "0" : String.valueOf(p.getBlockX()); }
    public int getWidth() { return labeledWidth("X", value()); }
    public int getHeight() { return 9 + PAD * 2; }
    protected void render(GuiGraphicsExtractor g) { labeled(g, "X", value()); }
}
