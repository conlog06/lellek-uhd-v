package gg.lellek.hud.gui;

import gg.lellek.hud.emote.Emote;
import gg.lellek.hud.emote.EmoteState;
import gg.lellek.hud.emote.Emotes;
import gg.lellek.hud.emote.Shop;
import gg.lellek.hud.LellekHud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Emote-Auswahl (Taste G): Raster aller Emotes, gekaufte sind aktiv, Klick spielt ab. */
public class EmotesScreen extends Screen {
    private static final int CARD_W = 78, CARD_H = 34, GAP = 6;
    private static final int PANEL = 0xF0130E08, CARD = 0xFF261E13, CARD_HOVER = 0xFF32271A, LINE = 0xFF3B2E1C, GOLD = 0xFFFFB400, MUTED = 0xFFB0A38E;
    private final Screen parent;
    public EmotesScreen(Screen parent) { super(Component.literal("Emotes")); this.parent = parent; Shop.refresh(); }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.literal("Shop"), b -> minecraft.gui.setScreen(new ShopScreen(this))).bounds(width / 2 - 155, height - 26, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Emote beenden"), b -> { EmoteState.stop(); minecraft.gui.setScreen(null); }).bounds(width / 2 - 50, height - 26, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Zurück"), b -> onClose()).bounds(width / 2 + 55, height - 26, 100, 20).build());
    }
    private int cols() { return Math.max(1, (width - 40 + GAP) / (CARD_W + GAP)); }
    private int left() { return (width - (cols() * (CARD_W + GAP) - GAP)) / 2; }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float pt) {
        super.extractRenderState(g, mouseX, mouseY, pt);
        g.fill(0, 0, width, height, PANEL);
        Gfx.centered(g, font, "Emotes", width / 2, 10, GOLD);
        Gfx.centered(g, font, Shop.offline() ? "Offline-Modus: alle Emotes frei (nur du siehst sie)" : Shop.coins + " Coins  \u00b7  " + Shop.owned.size() + " gekauft", width / 2, 22, MUTED);
        int n = cols(), x0 = left();
        for (int i = 0; i < Emotes.ALL.size(); i++) {
            Emote e = Emotes.ALL.get(i); int x = x0 + (i % n) * (CARD_W + GAP), y = 40 + (i / n) * (CARD_H + GAP);
            boolean owned = Shop.owns(e.id), hov = mouseX >= x && mouseX < x + CARD_W && mouseY >= y && mouseY < y + CARD_H;
            EmoteState.Playing pl = EmoteState.local(); boolean active = pl != null && pl.emote == e;
            g.fill(x, y, x + CARD_W, y + CARD_H, hov ? CARD_HOVER : CARD);
            g.fill(x, y, x + 2, y + CARD_H, active ? GOLD : owned ? 0xFF7BD88F : LINE);
            Gfx.text(g, font, e.name, x + 7, y + 7, owned ? 0xFFFFFFFF : 0xFF777777);
            Gfx.text(g, font, owned ? (e.duration > 0 ? (int) e.duration + " s" : "Dauer") : e.price + " Coins", x + 7, y + 19, owned ? MUTED : 0xFFFFB400);
            if (!owned) Gfx.text(g, font, "\ud83d\udd12", x + CARD_W - 14, y + 7, MUTED);
        }
    }
    @Override
    public boolean mouseClicked(MouseButtonEvent e0, boolean dbl) {
        if (super.mouseClicked(e0, dbl)) return true;
        int mouseX = (int) e0.x(), mouseY = (int) e0.y();
        int n = cols(), x0 = left();
        for (int i = 0; i < Emotes.ALL.size(); i++) {
            int x = x0 + (i % n) * (CARD_W + GAP), y = 40 + (i / n) * (CARD_H + GAP);
            if (mouseX < x || mouseX >= x + CARD_W || mouseY < y || mouseY >= y + CARD_H) continue;
            Emote e = Emotes.ALL.get(i);
            if (!Shop.owns(e.id)) { minecraft.gui.setScreen(new ShopScreen(this)); return true; }
            EmoteState.play(e); minecraft.gui.setScreen(null); return true;
        }
        return false;
    }
    @Override
    public void onClose() { minecraft.gui.setScreen(parent); }
    @Override
    public boolean isPauseScreen() { return false; }
}
