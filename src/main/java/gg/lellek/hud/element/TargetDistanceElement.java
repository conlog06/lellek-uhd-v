package gg.lellek.hud.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;

public class TargetDistanceElement extends HudElement {
    public TargetDistanceElement() { super("targetdist", "Entfernung zum Ziel", TL, 4, 186, false); }
    private String value() { Minecraft mc = Minecraft.getInstance(); LocalPlayer p = mc.player; if (p == null) return ""; return mc.hitResult == null || mc.hitResult.getType() == net.minecraft.world.phys.HitResult.Type.MISS ? "-" : String.format("%.1f m", mc.hitResult.getLocation().distanceTo(p.getEyePosition())); }
    public int getWidth() { return labeledWidth("Ziel", value()); }
    public int getHeight() { return 9 + PAD * 2; }
    protected void render(GuiGraphicsExtractor g) { labeled(g, "Ziel", value()); }
}
