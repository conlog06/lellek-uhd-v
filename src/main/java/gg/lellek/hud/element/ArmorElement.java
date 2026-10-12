package gg.lellek.hud.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/** Rüstung + Hand-Item mit Haltbarkeit */
public class ArmorElement extends HudElement {
    private static final EquipmentSlot[] SLOTS = { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND };
    public ArmorElement() { super("armor", "Rüstung", BR, 4, 4); }
    public int getWidth() { return 62; }
    public int getHeight() { return 5 * 17 + PAD; }

    protected void render(GuiGraphicsExtractor g) {
        LocalPlayer p = Minecraft.getInstance().player;
        if (p == null) return;
        bg(g, getWidth(), getHeight());
        int y = PAD;
        for (EquipmentSlot s : SLOTS) { item(g, p.getItemBySlot(s), y); y += 17; }
    }
    private void item(GuiGraphicsExtractor g, ItemStack stack, int y) {
        if (stack.isEmpty()) return;
        g.item(stack, PAD, y);
        if (stack.isDamageableItem()) {
            int left = stack.getMaxDamage() - stack.getDamageValue();
            float pct = left / (float) stack.getMaxDamage();
            text(g, String.valueOf(left), PAD + 20, y + 4, pct > 0.5f ? 0xFF7BD88F : pct > 0.2f ? 0xFFFFB400 : 0xFFFF5F5F);
        } else if (stack.getCount() > 1) text(g, String.valueOf(stack.getCount()), PAD + 20, y + 4, WHITE);
    }
}
