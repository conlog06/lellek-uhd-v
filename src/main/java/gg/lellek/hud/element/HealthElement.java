package gg.lellek.hud.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;

public class HealthElement extends HudElement {
    public HealthElement() { super("health", "Leben & Hunger", TL, 4, 130, false); }
    private String value() { LocalPlayer p = Minecraft.getInstance().player; if (p == null) return ""; return String.format("%.0f", (double) p.getHealth()) + " HP \u00b7 " + p.getFoodData().getFoodLevel() + " Hunger"; }
    public int getWidth() { return labeledWidth("HP", value()); }
    public int getHeight() { return 9 + PAD * 2; }
    protected void render(GuiGraphicsExtractor g) { labeled(g, "HP", value()); }
}
