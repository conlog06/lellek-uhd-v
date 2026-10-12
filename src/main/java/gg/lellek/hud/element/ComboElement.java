package gg.lellek.hud.element;

import gg.lellek.hud.feature.Features;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class ComboElement extends HudElement {
    public ComboElement() { super("combo", "Combo", TL, 4, 88, false); }
    private String value() { return Features.combo + " Hits"; }
    public int getWidth() { return labeledWidth("Combo", value()); }
    public int getHeight() { return 9 + PAD * 2; }
    protected void render(GuiGraphicsExtractor g) { labeled(g, "Combo", value()); }
}
