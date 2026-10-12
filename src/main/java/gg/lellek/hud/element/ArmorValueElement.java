package gg.lellek.hud.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;

public class ArmorValueElement extends HudElement {
    public ArmorValueElement() { super("armorvalue", "Rüstungswert", TL, 4, 144, false); }
    private String value() { LocalPlayer p = Minecraft.getInstance().player; if (p == null) return ""; return String.valueOf(p.getArmorValue()); }
    public int getWidth() { return labeledWidth("Rüstung", value()); }
    public int getHeight() { return 9 + PAD * 2; }
    protected void render(GuiGraphicsExtractor g) { labeled(g, "Rüstung", value()); }
}
