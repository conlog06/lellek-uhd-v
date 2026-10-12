package gg.lellek.hud.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;

public class LightElement extends HudElement {
    public LightElement() { super("light", "Lichtlevel", TL, 4, 172, false); }
    private String value() { Minecraft mc = Minecraft.getInstance(); LocalPlayer p = mc.player; if (p == null || mc.level == null) return ""; return mc.level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, p.blockPosition()) + " (Sonne " + mc.level.getBrightness(net.minecraft.world.level.LightLayer.SKY, p.blockPosition()) + ")"; }
    public int getWidth() { return labeledWidth("Licht", value()); }
    public int getHeight() { return 9 + PAD * 2; }
    protected void render(GuiGraphicsExtractor g) { labeled(g, "Licht", value()); }
}
