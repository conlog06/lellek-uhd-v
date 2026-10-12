package gg.lellek.hud.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;

public class SpeedElement extends HudElement {
    public SpeedElement() { super("speed", "Geschwindigkeit", TL, 4, 200, false); }
    private String value() { LocalPlayer p = Minecraft.getInstance().player; if (p == null) return ""; return String.format("%.1f Bl/s", p.getDeltaMovement().horizontalDistance() * 20); }
    public int getWidth() { return labeledWidth("Speed", value()); }
    public int getHeight() { return 9 + PAD * 2; }
    protected void render(GuiGraphicsExtractor g) { labeled(g, "Speed", value()); }
}
