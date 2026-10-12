package gg.lellek.hud.element;

import gg.lellek.hud.feature.Features;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class ReachElement extends HudElement {
    public ReachElement() { super("reach", "Reichweite", TL, 4, 74, false); }
    private String value() { return String.format("%.2f", Features.lastReach); }
    public int getWidth() { return labeledWidth("Reach", value()); }
    public int getHeight() { return 9 + PAD * 2; }
    protected void render(GuiGraphicsExtractor g) { labeled(g, "Reach", value()); }
}
