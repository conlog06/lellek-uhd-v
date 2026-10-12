package gg.lellek.hud.gui;

import gg.lellek.hud.emote.Emote;
import gg.lellek.hud.emote.EmoteState;
import gg.lellek.hud.emote.Emotes;
import gg.lellek.hud.emote.Shop;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Emote-Menü (Taste G): Kategorien links, Suche, Karten in der Mitte, rechts Live-Vorschau am eigenen Spieler.
 * Maus über Karte = Vorschau, Klick = abspielen, gesperrt = Shop.
 */
public class EmotesScreen extends Screen {
    private static final int SIDEBAR = 104, RIGHT = 150, CARD_W = 96, CARD_H = 40, GAP = 6;
    private static final int BG = 0xF0130E08, PANEL = 0xFF1F1810, CARD = 0xFF261E13, CARD_HOVER = 0xFF32271A, LINE = 0xFF3B2E1C, GOLD = 0xFFFFB400, MUTED = 0xFFB0A38E, GREEN = 0xFF7BD88F;
    private final Screen parent;
    private static int category = 0;
    private EditBox search;
    private final List<Emote> shown = new ArrayList<>();
    private float scroll, scrollTarget;
    private Emote hovered;
    private final long opened = System.currentTimeMillis();

    public EmotesScreen(Screen parent) { super(Component.literal("Emotes")); this.parent = parent; Shop.refresh(); }

    @Override
    protected void init() {
        search = new EditBox(font, SIDEBAR + 16, 13, width - SIDEBAR - RIGHT - 32, 20, Component.literal("Suchen"));
        search.setMaxLength(30); search.setBordered(false); search.setResponder(t -> rebuild());
        addRenderableWidget(search);
        rebuild();
    }
    private void rebuild() {
        shown.clear(); String q = search == null ? "" : search.getValue().toLowerCase(); String cat = Emotes.CATEGORIES[category];
        for (Emote e : Emotes.ALL) {
            if (!cat.equals("Alle") && !e.category.equals(cat)) continue;
            if (!q.isEmpty() && !e.name.toLowerCase().contains(q)) continue;
            shown.add(e);
        }
        // Freie/gekaufte zuerst
        shown.sort((a, b) -> Boolean.compare(!Shop.owns(a.id), !Shop.owns(b.id)));
        scroll = scrollTarget = 0;
    }
    private int cols() { return Math.max(1, (width - SIDEBAR - RIGHT - 32 + GAP) / (CARD_W + GAP)); }
    private int top() { return 44; }
    private int cardX(int i) { return SIDEBAR + 16 + (i % cols()) * (CARD_W + GAP); }
    private int cardY(int i) { return top() + (i / cols()) * (CARD_H + GAP) - (int) scroll; }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float pt) {}

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float pt) {
        scroll += (scrollTarget - scroll) * 0.3f;
        float in = Math.min(1f, (System.currentTimeMillis() - opened) / 180f); in = 1 - (1 - in) * (1 - in);
        g.fill(0, 0, width - RIGHT, height, BG);
        // Seitenleiste
        g.fill(0, 0, SIDEBAR, height, PANEL); g.fill(SIDEBAR, 0, SIDEBAR + 1, height, LINE);
        Gfx.centered(g, font, "Emotes", SIDEBAR / 2, 14, GOLD);
        for (int i = 0; i < Emotes.CATEGORIES.length; i++) {
            int y = 34 + i * 20; boolean sel = i == category, hov = mouseX < SIDEBAR && mouseY >= y && mouseY < y + 18;
            if (sel) { g.fill(6, y, SIDEBAR - 6, y + 18, 0x30FFB400); g.fill(6, y + 3, 8, y + 15, GOLD); } else if (hov) g.fill(6, y, SIDEBAR - 6, y + 18, 0x18FFFFFF);
            Gfx.text(g, font, font.plainSubstrByWidth(Emotes.CATEGORIES[i], SIDEBAR - 22), 14, y + 5, sel ? GOLD : 0xFFDDDDDD);
        }
        int by = height - 70;
        button(g, 6, by, SIDEBAR - 12, "⭐ Shop", mouseX, mouseY);
        button(g, 6, by + 22, SIDEBAR - 12, "■ Emote stoppen", mouseX, mouseY);
        Gfx.text(g, font, Shop.offline() ? "Offline: alles frei" : Shop.coins + " Coins", 8, height - 18, MUTED);
        // Suche
        g.fill(SIDEBAR + 12, 10, width - RIGHT - 12, 36, CARD);
        String v = search.getValue();
        Gfx.text(g, font, v.isEmpty() && !search.isFocused() ? "Suchen… (" + Emotes.ALL.size() + " Emotes)" : v + (search.isFocused() && (System.currentTimeMillis() / 500) % 2 == 0 ? "_" : ""), search.getX() + 4, search.getY() + 6, v.isEmpty() ? MUTED : 0xFFFFFFFF);
        // Karten
        hovered = null;
        EmoteState.Playing pl = EmoteState.local();
        g.enableScissor(SIDEBAR + 1, top() - 2, width - RIGHT, height);
        for (int i = 0; i < shown.size(); i++) {
            Emote e = shown.get(i); int x = cardX(i), y = cardY(i) + (int) ((1 - in) * 16 * (i % cols() + 1));
            if (y + CARD_H < top() || y > height) continue;
            boolean owned = Shop.owns(e.id), hov = mouseX >= x && mouseX < x + CARD_W && mouseY >= y && mouseY < y + CARD_H && mouseY >= top() && mouseX < width - RIGHT;
            boolean active = pl != null && pl.emote == e;
            if (hov) hovered = e;
            g.fill(x, y, x + CARD_W, y + CARD_H, hov ? CARD_HOVER : CARD);
            g.fill(x, y, x + 2, y + CARD_H, active ? GOLD : owned ? GREEN : LINE);
            if (active) g.outline(x, y, CARD_W, CARD_H, GOLD);
            Gfx.text(g, font, font.plainSubstrByWidth(e.name, CARD_W - 14), x + 8, y + 7, owned ? 0xFFFFFFFF : 0xFF8A8A8A);
            String info = owned ? (e.duration > 0 ? String.format("%.1f s", e.duration) : "Schleife") : Shop.price(e) + " ⭐";
            Gfx.text(g, font, info, x + 8, y + 22, owned ? MUTED : GOLD);
            if (e.music != null) Gfx.text(g, font, "♫", x + CARD_W - 12, y + 22, owned ? GOLD : 0xFF777777);
            if (!owned) Gfx.text(g, font, "🔒", x + CARD_W - 14, y + 6, MUTED);
        }
        g.disableScissor();
        if (shown.isEmpty()) Gfx.centered(g, font, "Nichts gefunden", SIDEBAR + (width - SIDEBAR - RIGHT) / 2, height / 2, MUTED);
        // Vorschau
        EmoteState.preview(hovered);
        int rx = width - RIGHT;
        g.fill(rx, 0, width, height, PANEL); g.fill(rx, 0, rx + 1, height, LINE);
        Gfx.centered(g, font, "Vorschau", rx + RIGHT / 2, 14, GOLD);
        if (!Gfx.playerPreview(g, rx + 8, 30, width - 8, height - 60, rx + RIGHT / 2, height / 3)) Gfx.centered(g, font, "Vorschau: 3rd-Person", rx + RIGHT / 2, height / 2, MUTED);
        Emote info = hovered != null ? hovered : pl != null ? pl.emote : null;
        if (info != null) {
            Gfx.centered(g, font, info.name, rx + RIGHT / 2, height - 50, 0xFFFFFFFF);
            Gfx.centered(g, font, info.category + (info.music != null ? " · ♫ Musik" : ""), rx + RIGHT / 2, height - 38, MUTED);
        }
        Gfx.centered(g, font, "Klick = abspielen · Bewegen = stoppen", rx + RIGHT / 2, height - 14, 0xFF666666);
        super.extractRenderState(g, mouseX, mouseY, pt);
    }
    private void button(GuiGraphicsExtractor g, int x, int y, int w, String label, int mx, int my) {
        boolean hov = mx >= x && mx < x + w && my >= y && my < y + 18;
        g.fill(x, y, x + w, y + 18, hov ? 0x40FFB400 : 0x25FFB400);
        Gfx.centered(g, font, label, x + w / 2, y + 5, GOLD);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent ev, boolean dbl) {
        if (super.mouseClicked(ev, dbl)) return true;
        int mx = (int) ev.x(), my = (int) ev.y();
        if (mx < SIDEBAR) {
            for (int i = 0; i < Emotes.CATEGORIES.length; i++) { int y = 34 + i * 20; if (my >= y && my < y + 18) { category = i; rebuild(); return true; } }
            int by = height - 70;
            if (my >= by && my < by + 18) { minecraft.gui.setScreen(new ShopScreen(this)); return true; }
            if (my >= by + 22 && my < by + 40) { EmoteState.stop(); return true; }
            return false;
        }
        if (my < top() || mx >= width - RIGHT) return false;
        for (int i = 0; i < shown.size(); i++) {
            int x = cardX(i), y = cardY(i);
            if (mx < x || mx >= x + CARD_W || my < y || my >= y + CARD_H) continue;
            Emote e = shown.get(i);
            if (!Shop.owns(e.id)) { minecraft.gui.setScreen(new ShopScreen(this)); return true; }
            EmoteState.play(e); minecraft.gui.setScreen(null); return true;
        }
        return false;
    }
    @Override
    public boolean mouseScrolled(double mx, double my, double dx, double dy) {
        int rows = (shown.size() + cols() - 1) / cols();
        int max = Math.max(0, rows * (CARD_H + GAP) - (height - top() - 10));
        scrollTarget = (float) Math.max(0, Math.min(max, scrollTarget - Math.signum(dy) * 30));
        return true;
    }
    @Override
    public void onClose() { EmoteState.preview(null); minecraft.gui.setScreen(parent); }
    @Override
    public boolean isPauseScreen() { return false; }
}
