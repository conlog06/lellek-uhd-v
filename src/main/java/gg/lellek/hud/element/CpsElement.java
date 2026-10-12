package gg.lellek.hud.element;

import gg.lellek.hud.ClickTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class CpsElement extends HudElement {
    public CpsElement() { super("cps", "CPS", TL, 4, 18); }
    private String value() { return ClickTracker.leftCps() + " | " + ClickTracker.rightCps(); }
    public int getWidth() { return labeledWidth("CPS", value()); }
    public int getHeight() { return 9 + PAD * 2; }
    protected void render(GuiGraphicsExtractor g) { labeled(g, "CPS", value()); }
}
