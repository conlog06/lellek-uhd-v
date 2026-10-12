package gg.lellek.hud.element;

import gg.lellek.hud.feature.Features;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class ServerElement extends HudElement {
    public ServerElement() { super("server", "Server", TL, 4, 102, false); }
    private String value() { return Minecraft.getInstance().isLocalServer() || Minecraft.getInstance().getCurrentServer() == null ? "Einzelspieler" : Minecraft.getInstance().getCurrentServer().ip; }
    public int getWidth() { return labeledWidth("Server", value()); }
    public int getHeight() { return 9 + PAD * 2; }
    protected void render(GuiGraphicsExtractor g) { labeled(g, "Server", value()); }
}
