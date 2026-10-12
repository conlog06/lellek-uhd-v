package gg.lellek.hud.element;

import gg.lellek.hud.feature.Features;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class MemoryElement extends HudElement {
    public MemoryElement() { super("memory", "Speicher", TL, 4, 116, false); }
    private String value() { return ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) >> 20) + " / " + (Runtime.getRuntime().maxMemory() >> 20) + " MB"; }
    public int getWidth() { return labeledWidth("RAM", value()); }
    public int getHeight() { return 9 + PAD * 2; }
    protected void render(GuiGraphicsExtractor g) { labeled(g, "RAM", value()); }
}
