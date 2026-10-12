package gg.lellek.hud.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Anzahl des gehaltenen Items im Inventar – bei Bogen: Pfeile. */
public class ItemCountElement extends HudElement {
    public ItemCountElement() { super("itemcount", "Item-Zähler", BL, 4, 146, false); }
    private ItemStack target() { Minecraft mc = Minecraft.getInstance(); if (mc.player == null) return ItemStack.EMPTY; ItemStack h = mc.player.getMainHandItem(); return h.is(Items.BOW) || h.is(Items.CROSSBOW) ? new ItemStack(Items.ARROW) : h; }
    private int count(ItemStack t) { int n = 0; Minecraft mc = Minecraft.getInstance(); for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) { ItemStack s = mc.player.getInventory().getItem(i); if (!s.isEmpty() && s.is(t.getItem())) n += s.getCount(); } return n; }
    public int getWidth() { return 52; }
    public int getHeight() { return 20; }
    protected void render(GuiGraphicsExtractor g) { ItemStack t = target(); bg(g, getWidth(), getHeight()); if (t.isEmpty()) { text(g, "-", PAD + 22, 6, 0xFF888888); return; } g.item(t, 2, 2); text(g, "x" + count(t), 22, 6, color); }
}
