package gg.lellek.hud.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;

public class PingElement extends HudElement {
    public PingElement() { super("ping", "Ping", TL, 4, 32); }
    private String value() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null || mc.isLocalServer()) return "-";
        PlayerInfo info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
        return info == null ? "-" : info.getLatency() + " ms";
    }
    public int getWidth() { return labeledWidth("Ping", value()); }
    public int getHeight() { return 9 + PAD * 2; }
    protected void render(GuiGraphicsExtractor g) { labeled(g, "Ping", value()); }
}
