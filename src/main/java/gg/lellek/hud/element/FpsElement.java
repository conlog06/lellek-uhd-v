package gg.lellek.hud.element;

import gg.lellek.hud.ClickTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class FpsElement extends HudElement {
    public FpsElement() { super("fps", "FPS", TL, 4, 4); }
    private String value() { return String.valueOf(Minecraft.getInstance().getFps()); }
    public int getWidth() { return labeledWidth("FPS", value()); }
    public int getHeight() { return 9 + PAD * 2; }
    protected void render(GuiGraphicsExtractor g) { labeled(g, "FPS", value()); }
}
