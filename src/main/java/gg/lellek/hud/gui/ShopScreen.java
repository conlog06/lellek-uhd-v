package gg.lellek.hud.gui;

import gg.lellek.hud.emote.Emote;
import gg.lellek.hud.emote.Emotes;
import gg.lellek.hud.emote.Shop;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Shop: Emotes mit Coins kaufen. Coins gibt es fürs Spielen (10 pro Minute). */
public class ShopScreen extends Screen {
    private static final int ROW = 26;
    private static final int PANEL = 0xF0130E08, CARD = 0xFF261E13, CARD_HOVER = 0xFF32271A, GOLD = 0xFFFFB400, MUTED = 0xFFB0A38E;
    private final Screen parent; private int scroll; private String pending;
    public ShopScreen(Screen parent) { super(Component.literal("Shop")); this.parent = parent; Shop.refresh(); }

    @Override
    protected void init() { addRenderableWidget(Button.builder(Component.literal("Zurück"), b -> onClose()).bounds(width / 2 - 50, height - 26, 100, 20).build()); }
    private int left() { return width / 2 - 160; }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float pt) {
        super.extractRenderState(g, mouseX, mouseY, pt);
        g.fill(0, 0, width, height, PANEL);
        Gfx.centered(g, font, "Shop", width / 2, 10, GOLD);
        String coins = Shop.coins < 0 ? "…" : String.valueOf(Shop.coins);
        Gfx.text(g, font, "\u2b50 " + coins + " Coins", width - 90, 10, GOLD);
        Gfx.centered(g, font, "10 Coins pro gespielter Minute", width / 2, 22, MUTED);
        if (Shop.lastError != null) Gfx.centered(g, font, Shop.lastError, width / 2, 34, 0xFFF07C7C);
        int x = left();
        for (int i = 0; i < Emotes.shop().size(); i++) {
            Emote e = Emotes.shop().get(i); int y = 46 - scroll + i * ROW; if (y < 40 || y > height - 50) continue;
            boolean owned = Shop.owns(e.id), hov = mouseX >= x && mouseX < x + 320 && mouseY >= y && mouseY < y + ROW - 2;
            g.fill(x, y, x + 320, y + ROW - 2, hov ? CARD_HOVER : CARD);
            Gfx.text(g, font, e.name, x + 10, y + 8, 0xFFFFFFFF);
            Gfx.text(g, font, e.duration > 0 ? (int) e.duration + " s" : "Dauerpose", x + 120, y + 8, MUTED);
            int bx = x + 230; boolean bh = mouseX >= bx && mouseX < bx + 80 && mouseY >= y + 3 && mouseY < y + 21;
            if (owned) { g.fill(bx, y + 3, bx + 80, y + 21, 0xFF1F3A26); Gfx.centered(g, font, "Gekauft", bx + 40, y + 8, 0xFF7BD88F); }
            else { boolean can = Shop.coins >= gg.lellek.hud.emote.Shop.price(e); g.fill(bx, y + 3, bx + 80, y + 21, can ? (bh ? 0xFFFFC533 : GOLD) : 0xFF3B2E1C); Gfx.centered(g, font, e.id.equals(pending) ? "…" : gg.lellek.hud.emote.Shop.price(e) + " \u2b50", bx + 40, y + 8, can ? 0xFF2A1400 : MUTED); }
        }
    }
    @Override
    public boolean mouseClicked(MouseButtonEvent e0, boolean dbl) {
        if (super.mouseClicked(e0, dbl)) return true;
        int mouseX = (int) e0.x(), mouseY = (int) e0.y();
        int x = left();
        for (int i = 0; i < Emotes.shop().size(); i++) {
            Emote e = Emotes.shop().get(i); int y = 46 - scroll + i * ROW, bx = x + 230;
            if (mouseX >= bx && mouseX < bx + 80 && mouseY >= y + 3 && mouseY < y + 21 && !Shop.owns(e.id) && Shop.coins >= gg.lellek.hud.emote.Shop.price(e)) { pending = e.id; Shop.buy(e.id); return true; }
        }
        return false;
    }
    @Override
    public boolean mouseScrolled(double mx, double my, double dx, double dy) { scroll = Math.max(0, Math.min(Math.max(0, Emotes.shop().size() * ROW - (height - 100)), scroll - (int) Math.signum(dy) * 26)); return true; }
    @Override
    public void onClose() { minecraft.gui.setScreen(parent); }
    @Override
    public boolean isPauseScreen() { return false; }
}
