package gg.lellek.hud.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;

public class HeldItemElement extends HudElement {
    public HeldItemElement() { super("helditem", "Item in Hand", BC, 0, 76, false); }
    private String value() { LocalPlayer p = Minecraft.getInstance().player; if (p == null) return ""; return p.getMainHandItem().isEmpty() ? "-" : p.getMainHandItem().getHoverName().getString() + (p.getMainHandItem().isDamageableItem() ? " " + (p.getMainHandItem().getMaxDamage() - p.getMainHandItem().getDamageValue()) + "/" + p.getMainHandItem().getMaxDamage() : ""); }
    public int getWidth() { return labeledWidth("Item", value()); }
    public int getHeight() { return 9 + PAD * 2; }
    protected void render(GuiGraphicsExtractor g) { labeled(g, "Item", value()); }
}
