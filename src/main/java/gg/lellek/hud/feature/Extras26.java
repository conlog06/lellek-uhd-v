package gg.lellek.hud.feature;

import gg.lellek.hud.LellekHud;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/** Chat-Erwähnung, Auto-Respawn, Haltbarkeits-Warnung – rein über Fabric-Events. */
public class Extras26 {
    private static long respawnAt = -1, lastWarn;
    public static void install() {
        ClientReceiveMessageEvents.GAME.register((msg, overlay) -> {
            if (overlay || !Mods.CHAT_MENTION.enabled) return;
            Minecraft mc = Minecraft.getInstance(); if (mc.player == null) return;
            if (msg.getString().toLowerCase().contains(mc.player.getName().getString().toLowerCase())) mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING, 1.6f));
        });
        ClientReceiveMessageEvents.MODIFY_GAME.register((msg, overlay) -> {
            if (overlay || !Mods.CHAT_MENTION.enabled) return msg;
            Minecraft mc = Minecraft.getInstance(); if (mc.player == null) return msg;
            String name = mc.player.getName().getString();
            return msg.getString().toLowerCase().contains(name.toLowerCase()) ? Component.literal("\u25b6 ").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Mods.CHAT_MENTION.colorOf("color")))).append(msg) : msg;
        });
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> { if (screen instanceof DeathScreen && Mods.AUTO_RESPAWN.enabled) respawnAt = System.currentTimeMillis() + Mods.AUTO_RESPAWN.intOf("delay") * 1000L; });
    }
    public static void tick(Minecraft mc) {
        if (respawnAt > 0 && System.currentTimeMillis() >= respawnAt) { respawnAt = -1; if (mc.player != null && LellekHud.screen(mc) instanceof DeathScreen) { mc.player.respawn(); mc.gui.setScreen(null); } }
        if (Mods.DURABILITY.enabled && mc.player != null && System.currentTimeMillis() - lastWarn > 10000) {
            for (EquipmentSlot s : EquipmentSlot.values()) {
                ItemStack st = mc.player.getItemBySlot(s); if (st.isEmpty() || !st.isDamageableItem()) continue;
                int left = st.getMaxDamage() - st.getDamageValue();
                if (left * 100 / st.getMaxDamage() < Mods.DURABILITY.intOf("percent")) { lastWarn = System.currentTimeMillis(); gg.lellek.hud.HudRenderer.toast("\u26a0 " + st.getHoverName().getString() + ": nur noch " + left + " Haltbarkeit", 0xFFFF5F5F); mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BASS, 0.8f)); break; }
            }
        }
    }
}
