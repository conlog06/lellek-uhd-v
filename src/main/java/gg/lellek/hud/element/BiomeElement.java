package gg.lellek.hud.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;

public class BiomeElement extends HudElement {
    public BiomeElement() { super("biome", "Biom", TL, 4, 158, false); }
    private String value() { Minecraft mc = Minecraft.getInstance(); LocalPlayer p = mc.player; if (p == null || mc.level == null) return ""; return mc.level.getBiome(p.blockPosition()).unwrapKey().map(k -> k.identifier().getPath().replace("_", " ")).orElse("?"); }
    public int getWidth() { return labeledWidth("Biom", value()); }
    public int getHeight() { return 9 + PAD * 2; }
    protected void render(GuiGraphicsExtractor g) { labeled(g, "Biom", value()); }
}
