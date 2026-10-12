package gg.lellek.hud.feature;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;

/** Auto-GG */
public class Extras {
    private static final String[][] TRIGGERS = { { "won the game", "gewonnen", "winner", "victory", "1st place" }, { "game over", "spiel vorbei", "du bist gestorben" } };
    private long ggAt = -1, lastGg;
    public void install() {
        ClientReceiveMessageEvents.GAME.register((msg, overlay) -> {
            if (overlay || !Mods.AUTO_GG.enabled) return;
            String t = msg.getString().toLowerCase();
            for (String s : TRIGGERS[Mods.AUTO_GG.choiceOf("trigger")]) if (t.contains(s) && System.currentTimeMillis() - lastGg > 30000) { ggAt = System.currentTimeMillis() + Mods.AUTO_GG.intOf("delay") * 1000L; return; }
        });
    }
    public void tick(Minecraft mc) {
        if (ggAt > 0 && System.currentTimeMillis() >= ggAt) { ggAt = -1; lastGg = System.currentTimeMillis(); if (mc.getConnection() != null) mc.getConnection().sendChat(new String[] { "gg", "GG!", "gg wp" }[Mods.AUTO_GG.choiceOf("text")]); }
    }
}
