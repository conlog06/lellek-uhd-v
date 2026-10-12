package gg.lellek.hud.element;

import gg.lellek.hud.ClickTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class ClockElement extends HudElement {
    public ClockElement() { super("clock", "Uhrzeit", TL, 4, 60, false); }
    private String value() { return new java.text.SimpleDateFormat("HH:mm").format(new java.util.Date()); }
    public int getWidth() { return labeledWidth("Uhr", value()); }
    public int getHeight() { return 9 + PAD * 2; }
    protected void render(GuiGraphicsExtractor g) { labeled(g, "Uhr", value()); }
}
