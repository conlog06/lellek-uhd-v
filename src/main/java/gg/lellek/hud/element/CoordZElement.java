package gg.lellek.hud.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;

public class CoordZElement extends HudElement {
    public CoordZElement() { super("coordz", "Koordinate Z", TL, 60, 88, false); }
    private String value() { LocalPlayer p = Minecraft.getInstance().player; return p == null ? "0" : String.valueOf(p.getBlockZ()); }
    public int getWidth() { return labeledWidth("Z", value()); }
    public int getHeight() { return 9 + PAD * 2; }
    protected void render(GuiGraphicsExtractor g) { labeled(g, "Z", value()); }
}
