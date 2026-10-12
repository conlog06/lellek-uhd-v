package gg.lellek.hud.gui;

import gg.lellek.hud.LellekHud;
import gg.lellek.hud.cosmetic.Cosmetic;
import gg.lellek.hud.cosmetic.Cosmetics;
import gg.lellek.hud.element.HudElement;
import gg.lellek.hud.feature.Feature;
import gg.lellek.hud.feature.Mods;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Modernes Mod-Menü: Seitenleiste, Suche, Karten mit animierten Schaltern. Klick = an/aus, Zahnrad = Einstellungen. */
public class ModMenuScreen extends Screen {
    private static final int SIDEBAR = 96, CARD_H = 54, GAP = 8;
    private static final int BG = 0xF0130E08, PANEL = 0xFF1F1810, CARD = 0xFF261E13, CARD_HOVER = 0xFF32271A, LINE = 0xFF3B2E1C, GOLD = 0xFFFFB400, MUTED = 0xFFB0A38E;

    private final Screen parent;
    private final String[] categories;
    private int category = 0;
    private EditBox search;
    private float scroll, scrollTarget;
    private final long opened = System.currentTimeMillis();
    private final Map<String, Float> toggleAnim = new HashMap<>();

    private static class Card { String id, name, desc; boolean on, hasSettings, radio, locked; Feature feature; HudElement element; Cosmetic cosmetic; }
    private final List<Card> cards = new ArrayList<>();

    public ModMenuScreen(Screen parent) {
        super(Component.literal("LellekClient Mods"));
        this.parent = parent;
        List<String> c = new ArrayList<>(); c.add("Alle"); c.addAll(List.of(Mods.CATEGORIES)); c.add("HUD-Elemente");
        categories = c.toArray(new String[0]);
    }

    @Override
    protected void init() {
        search = new EditBox(font, SIDEBAR + 16, 13, width - SIDEBAR - 32, 20, Component.literal("Suchen"));
        search.setMaxLength(30); search.setBordered(false); search.setHint(Component.literal("Suchen\u2026"));
        search.setResponder(t -> rebuild());
        addRenderableWidget(search);
        rebuild();
    }

    private void rebuild() {
        cards.clear();
        String cat = categories[category], q = search == null ? "" : search.getValue().toLowerCase();
        if (!cat.equals("HUD-Elemente"))
            for (Feature f : Mods.ALL) {
                if (!cat.equals("Alle") && !f.category.equals(cat)) continue;
                if (!q.isEmpty() && !f.name.toLowerCase().contains(q) && !f.desc.toLowerCase().contains(q)) continue;
                Card c = new Card(); c.id = f.id; c.name = f.name; c.desc = f.unavailable != null ? f.unavailable : f.desc; c.on = f.enabled; c.hasSettings = !f.settings.isEmpty() && f.unavailable == null; c.feature = f; c.locked = f.unavailable != null; cards.add(c);
            }
        if (cat.equals("HUD-Elemente") || (cat.equals("Alle") && !q.isEmpty()))
            for (HudElement el : LellekHud.config.elements) {
                if (!q.isEmpty() && !el.name.toLowerCase().contains(q)) continue;
                Card c = new Card(); c.id = "el." + el.id; c.name = el.name; c.desc = "HUD-Element · Position im Editor (rechte Shift)"; c.on = el.enabled; c.hasSettings = true; c.element = el; cards.add(c);
            }
        scroll = scrollTarget = 0;
    }

    private int columns() { return Math.max(1, (width - SIDEBAR - 32 + GAP) / (200 + GAP)); }
    private int cardW() { return (width - SIDEBAR - 32 - (columns() - 1) * GAP) / columns(); }
    private int contentTop() { return 44; }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float pt) {
        float in = Math.min(1f, (System.currentTimeMillis() - opened) / 180f); in = 1 - (1 - in) * (1 - in);
        scroll += (scrollTarget - scroll) * 0.3f;
        super.extractRenderState(g, mouseX, mouseY, pt);
        g.fill(0, 0, width, height, BG);
        g.fill(0, 0, SIDEBAR, height, PANEL); g.fill(SIDEBAR, 0, SIDEBAR + 1, height, LINE);
        g.blit(RenderPipelines.GUI_TEXTURED, LellekHud.LOGO, SIDEBAR / 2 - 18, 10, 0, 0, 36, 36, 36, 36);
        gg.lellek.hud.gui.Gfx.centered(g, font, "Mods", SIDEBAR / 2, 50, GOLD);
        for (int i = 0; i < categories.length; i++) {
            int y = 70 + i * 22;
            boolean sel = i == category, hov = mouseX < SIDEBAR && mouseY >= y && mouseY < y + 20;
            if (sel) g.fill(6, y, SIDEBAR - 6, y + 20, 0x30FFB400); else if (hov) g.fill(6, y, SIDEBAR - 6, y + 20, 0x18FFFFFF);
            if (sel) g.fill(6, y + 3, 8, y + 17, GOLD);
            gg.lellek.hud.gui.Gfx.text(g, font, categories[i], 14, y + 6, sel ? GOLD : 0xFFDDDDDD);
        }
        int cy = 70 + categories.length * 22 + 10;
        boolean chov = mouseX < SIDEBAR && mouseY >= cy && mouseY < cy + 22;
        g.fill(6, cy, SIDEBAR - 6, cy + 22, chov ? 0x40FFB400 : 0x25FFB400);
        gg.lellek.hud.gui.Gfx.centered(g, font, "\u2726 Cosmetics", SIDEBAR / 2, cy + 7, GOLD);
        gg.lellek.hud.gui.Gfx.text(g, font, gg.lellek.hud.Diag.summary(), SIDEBAR + 16, height - 22, 0xFF666666);
        gg.lellek.hud.gui.Gfx.text(g, font, LellekHud.CLIENT_NAME, 8, height - 22, MUTED);
        gg.lellek.hud.gui.Gfx.text(g, font, LellekHud.MC_VERSION + " \u00b7 HUD " + LellekHud.VERSION, 8, height - 12, MUTED);

        int cols = columns(), cw = cardW(), top = contentTop();
        g.enableScissor(SIDEBAR + 1, top, width, height);
        for (int i = 0; i < cards.size(); i++) {
            Card c = cards.get(i);
            int x = SIDEBAR + 16 + (i % cols) * (cw + GAP), y = top + (i / cols) * (CARD_H + GAP) - (int) scroll + (int) ((1 - in) * 20 * (i % cols + 1));
            if (y + CARD_H < top || y > height) continue;
            boolean hov = mouseX >= x && mouseX < x + cw && mouseY >= y && mouseY < y + CARD_H && mouseY >= top;
            g.fill(x, y, x + cw, y + CARD_H, hov ? CARD_HOVER : CARD);
            g.fill(x, y, x + 2, y + CARD_H, c.on ? GOLD : LINE);
            gg.lellek.hud.gui.Gfx.text(g, font, c.name, x + 10, y + 9, c.locked ? 0xFF777777 : c.on ? 0xFFFFFFFF : 0xFFCCCCCC);
            List<FormattedCharSequence> lines = font.split(Component.literal(c.desc), cw - 70);
            for (int l = 0; l < Math.min(2, lines.size()); l++) gg.lellek.hud.gui.Gfx.text(g, font, lines.get(l), x + 10, y + 24 + l * 10, MUTED);
            if (c.locked) gg.lellek.hud.gui.Gfx.text(g, font, "folgt", x + cw - 40, y + 12, 0xFF777777); else drawToggle(g, c, x + cw - 44, y + 10);
            if (c.hasSettings) gg.lellek.hud.gui.Gfx.text(g, font, "\u2699", x + cw - 62, y + 36, hov ? GOLD : MUTED);
        }
        g.disableScissor();
        g.fill(SIDEBAR + 12, 10, width - 12, 36, CARD);
        drawSearch(g);
        if (cards.isEmpty()) gg.lellek.hud.gui.Gfx.centered(g, font, "Nichts gefunden", SIDEBAR + (width - SIDEBAR) / 2, height / 2, MUTED);
        gg.lellek.hud.gui.Gfx.centered(g, font, "ESC = zurück", SIDEBAR + (width - SIDEBAR) / 2, height - 12, 0xFF555555);
    }

    private void drawToggle(GuiGraphicsExtractor g, Card c, int x, int y) {
        float a = toggleAnim.getOrDefault(c.id, c.on ? 1f : 0f);
        a += ((c.on ? 1f : 0f) - a) * 0.25f; toggleAnim.put(c.id, a);
        int w = 28, h = 14, bg = blend(0xFF3B2E1C, GOLD, a);
        g.fill(x, y + 2, x + w, y + h - 2, bg); g.fill(x + 2, y, x + w - 2, y + h, bg);
        int kx = x + 2 + (int) ((w - 12) * a);
        g.fill(kx, y + 2, kx + 10, y + h - 2, 0xFFFFFFFF);
    }
    private static int blend(int c1, int c2, float t) {
        int r = (int) (((c1 >> 16) & 255) * (1 - t) + ((c2 >> 16) & 255) * t), gr = (int) (((c1 >> 8) & 255) * (1 - t) + ((c2 >> 8) & 255) * t), b = (int) ((c1 & 255) * (1 - t) + (c2 & 255) * t);
        return 0xFF000000 | (r << 16) | (gr << 8) | b;
    }

    private void drawSearch(GuiGraphicsExtractor g) {
        String v = search.getValue();
        Gfx.text(g, font, v.isEmpty() && !search.isFocused() ? "Suchen\u2026" : v + (search.isFocused() && (System.currentTimeMillis() / 500) % 2 == 0 ? "_" : ""), search.getX() + 4, search.getY() + 6, v.isEmpty() ? MUTED : 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean dbl) {
        if (super.mouseClicked(e, dbl)) return true;
        int mouseX = (int) e.x(), mouseY = (int) e.y();
        if (mouseX < SIDEBAR) {
            for (int i = 0; i < categories.length; i++) { int y = 70 + i * 22; if (mouseY >= y && mouseY < y + 20) { category = i; rebuild(); return true; } }
            int cy = 70 + categories.length * 22 + 10;
            if (mouseY >= cy && mouseY < cy + 22) { minecraft.gui.setScreen(new CosmeticsScreen(this)); return true; }
            return false;
        }
        if (mouseY < contentTop()) return false;
        int cols = columns(), cw = cardW(), top = contentTop();
        for (int i = 0; i < cards.size(); i++) {
            Card c = cards.get(i);
            int x = SIDEBAR + 16 + (i % cols) * (cw + GAP), y = top + (i / cols) * (CARD_H + GAP) - (int) scroll;
            if (mouseX < x || mouseX >= x + cw || mouseY < y || mouseY >= y + CARD_H) continue;
            if (c.hasSettings && mouseX >= x + cw - 70 && mouseY >= y + 30) {
                if (c.feature != null) minecraft.gui.setScreen(new FeatureSettingsScreen(this, c.feature));
                if (c.element != null) minecraft.gui.setScreen(new ElementSettingsScreen(this, c.element));
                return true;
            }
            if (c.locked) return true;
            if (c.feature != null) { c.feature.enabled = !c.feature.enabled; c.on = c.feature.enabled; }
            else if (c.element != null) { c.element.enabled = !c.element.enabled; c.on = c.element.enabled; }
            else if (c.radio) { LellekHud.config.cosmetic = c.cosmetic == null ? "" : c.cosmetic.id; for (Card o : cards) o.on = o.id.equals(c.id); }
            LellekHud.config.save();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double dx, double dy) {
        int rows = (cards.size() + columns() - 1) / columns();
        int max = Math.max(0, rows * (CARD_H + GAP) - (height - contentTop() - 16));
        scrollTarget = (float) Math.max(0, Math.min(max, scrollTarget - Math.signum(dy) * 30));
        return true;
    }
    @Override
    public void onClose() { LellekHud.config.save(); minecraft.gui.setScreen(parent); }
    @Override
    public boolean isPauseScreen() { return false; }
}
