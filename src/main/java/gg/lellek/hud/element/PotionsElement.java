package gg.lellek.hud.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;

import java.util.ArrayList;
import java.util.List;

/** Aktive Effekte mit Restzeit */
public class PotionsElement extends HudElement {
    public PotionsElement() { super("potions", "Effekte", TR, 4, 120, false); }
    private List<String> lines() {
        List<String> out = new ArrayList<>();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return out;
        float tps = mc.level.tickRateManager().tickrate();
        for (MobEffectInstance e : mc.player.getActiveEffects()) {
            String name = e.getEffect().value().getDisplayName().getString();
            if (e.getAmplifier() > 0) name += " " + (e.getAmplifier() + 1);
            out.add(name + " " + MobEffectUtil.formatDuration(e, 1.0f, tps).getString());
        }
        if (out.isEmpty()) out.add("Keine Effekte");
        return out;
    }
    public int getWidth() { int w = 60; for (String s : lines()) w = Math.max(w, font().width(s)); return w + PAD * 2; }
    public int getHeight() { return lines().size() * 10 + PAD * 2; }
    protected void render(GuiGraphicsExtractor g) {
        List<String> ls = lines();
        bg(g, getWidth(), getHeight());
        int y = PAD;
        for (String s : ls) { text(g, s, PAD, y, s.startsWith("Keine") ? 0xFF888888 : color); y += 10; }
    }
}
