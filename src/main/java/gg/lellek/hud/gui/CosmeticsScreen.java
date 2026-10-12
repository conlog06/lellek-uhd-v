package gg.lellek.hud.gui;

import gg.lellek.hud.LellekHud;
import gg.lellek.hud.cosmetic.Cosmetic;
import gg.lellek.hud.cosmetic.Cosmetics;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/** Garderobe: Kategorien links, Karten mit Vorschau, rechts der eigene Spieler live mit Cosmetic. */
public class CosmeticsScreen extends Screen {
    private static final int SIDEBAR = 96, RIGHT = 170, CARD_W = 120, CARD_H = 140, GAP = 10;
    private static final int BG = 0xF0130E08, PANEL = 0xFF1F1810, CARD = 0xFF261E13, CARD_HOVER = 0xFF32271A, LINE = 0xFF3B2E1C, GOLD = 0xFFFFB400, MUTED = 0xFFB0A38E;
    private final Screen parent;
    private int category = 0;
    private EditBox search;
    private final List<Cosmetic> shown = new ArrayList<>();
    private final long opened = System.currentTimeMillis();

    public CosmeticsScreen(Screen parent) { super(Component.literal("Cosmetics")); this.parent = parent; }

    @Override
    protected void init() {
        search = new EditBox(font, SIDEBAR + 16, 13, width - SIDEBAR - RIGHT - 32, 20, Component.literal("Suchen"));
        search.setBordered(false); search.setHint(Component.literal("Suchen\u2026")); search.setResponder(t -> rebuild());
        addRenderableWidget(search);
        rebuild();
    }
    private void rebuild() {
        shown.clear();
        String cat = Cosmetics.CATEGORIES[category], q = search == null ? "" : search.getValue().toLowerCase();
        for (Cosmetic c : Cosmetics.ALL) { if (!cat.equals("Alle") && !c.category.equals(cat)) continue; if (!q.isEmpty() && !c.name.toLowerCase().contains(q)) continue; shown.add(c); }
    }
    private int cols() { return Math.max(1, (width - SIDEBAR - RIGHT - 32 + GAP) / (CARD_W + GAP)); }
    private List<Cosmetic> all() { List<Cosmetic> a = new ArrayList<>(); a.add(null); a.addAll(shown); return a; }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float pt) {
        float in = Math.min(1f, (System.currentTimeMillis() - opened) / 180f); in = 1 - (1 - in) * (1 - in);
        super.extractRenderState(g, mouseX, mouseY, pt);
        g.fill(0, 0, width, height, BG);
        g.fill(0, 0, SIDEBAR, height, PANEL); g.fill(SIDEBAR, 0, SIDEBAR + 1, height, LINE);
        gg.lellek.hud.gui.Gfx.centered(g, font, "Cosmetics", SIDEBAR / 2, 16, GOLD);
        for (int i = 0; i < Cosmetics.CATEGORIES.length; i++) {
            int y = 40 + i * 22; boolean sel = i == category;
            if (sel) { g.fill(6, y, SIDEBAR - 6, y + 20, 0x30FFB400); g.fill(6, y + 3, 8, y + 17, GOLD); }
            gg.lellek.hud.gui.Gfx.text(g, font, Cosmetics.CATEGORIES[i], 14, y + 6, sel ? GOLD : 0xFFDDDDDD);
        }
        gg.lellek.hud.gui.Gfx.text(g, font, "Taste X = Rad", 8, height - 12, MUTED);
        g.fill(SIDEBAR + 12, 10, width - RIGHT - 12, 36, CARD); drawSearch(g);
        int n = cols(); List<Cosmetic> all = all();
        for (int i = 0; i < all.size(); i++) {
            Cosmetic c = all.get(i);
            int x = SIDEBAR + 16 + (i % n) * (CARD_W + GAP), y = 44 + (i / n) * (CARD_H + GAP) + (int) ((1 - in) * 20 * (i % n + 1));
            boolean hov = mouseX >= x && mouseX < x + CARD_W && mouseY >= y && mouseY < y + CARD_H;
            boolean active = c == null ? LellekHud.config.cosmetic.isEmpty() : c.id.equals(LellekHud.config.cosmetic);
            g.fill(x, y, x + CARD_W, y + CARD_H, hov ? CARD_HOVER : CARD);
            if (active) g.outline(x, y, CARD_W, CARD_H, GOLD);
            g.fill(x + 8, y + 8, x + CARD_W - 8, y + 88, 0xFF16110A);
            if (c != null) g.blit(RenderPipelines.GUI_TEXTURED, c.texture, x + 20, y + 16, 0, 0, 80, 64, 80, 64);
            else gg.lellek.hud.gui.Gfx.centered(g, font, "\u2716", x + CARD_W / 2, y + 44, MUTED);
            gg.lellek.hud.gui.Gfx.centered(g, font, c == null ? "Keins" : c.name, x + CARD_W / 2, y + 96, 0xFFFFFFFF);
            g.fill(x + 14, y + 112, x + CARD_W - 14, y + 130, active ? GOLD : hov ? 0xFF3E3122 : LINE);
            gg.lellek.hud.gui.Gfx.centered(g, font, active ? "Getragen" : "Anziehen", x + CARD_W / 2, y + 117, active ? 0xFF2A1400 : 0xFFFFFFFF);
        }
        int rx = width - RIGHT;
        g.fill(rx, 0, width, height, PANEL); g.fill(rx, 0, rx + 1, height, LINE);
        gg.lellek.hud.gui.Gfx.centered(g, font, "Vorschau", rx + RIGHT / 2, 16, GOLD);
        if (!drawPlayerPreview(g, rx + 10, 30, width - 10, height - 50, mouseX, mouseY)) Gfx.centered(g, font, "Vorschau: siehe Spieler", rx + RIGHT / 2, height / 2, MUTED);
        Cosmetic cur = Cosmetics.byId(LellekHud.config.cosmetic);
        gg.lellek.hud.gui.Gfx.centered(g, font, "Getragen:", rx + RIGHT / 2, height - 40, MUTED);
        gg.lellek.hud.gui.Gfx.centered(g, font, cur == null ? "Nichts" : cur.name, rx + RIGHT / 2, height - 28, 0xFFFFFFFF);
    }

    private void drawSearch(GuiGraphicsExtractor g) {
        String v = search.getValue();
        Gfx.text(g, font, v.isEmpty() && !search.isFocused() ? "Suchen\u2026" : v + (search.isFocused() && (System.currentTimeMillis() / 500) % 2 == 0 ? "_" : ""), search.getX() + 4, search.getY() + 6, v.isEmpty() ? MUTED : 0xFFFFFFFF);
    }

    private static java.lang.reflect.Method previewMethod; private static boolean previewSearched;
    /** InventoryScreen.renderEntityInInventory… heißt je nach Version anders – per Reflection suchen und Argumente nach Typ befüllen */
    private boolean drawPlayerPreview(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, int mx, int my) {
        if (minecraft.player == null) return false;
        if (!previewSearched) {
            previewSearched = true;
            for (java.lang.reflect.Method m : net.minecraft.client.gui.screens.inventory.InventoryScreen.class.getMethods())
                if (java.lang.reflect.Modifier.isStatic(m.getModifiers()) && m.getName().toLowerCase().contains("entity") && m.getParameterCount() >= 6 && m.getParameterTypes()[0] == GuiGraphicsExtractor.class) { previewMethod = m; break; }
        }
        if (previewMethod == null) return false;
        try {
            Class<?>[] pt = previewMethod.getParameterTypes(); Object[] args = new Object[pt.length];
            int[] ints = { x1, y1, x2, y2, 60 }; float[] floats = { 0.0625f, mx, my }; int ii = 0, fi = 0;
            for (int i = 0; i < pt.length; i++) {
                if (pt[i] == GuiGraphicsExtractor.class) args[i] = g;
                else if (pt[i] == int.class) args[i] = ii < ints.length ? ints[ii++] : 0;
                else if (pt[i] == float.class) args[i] = fi < floats.length ? floats[fi++] : 0f;
                else if (pt[i].isInstance(minecraft.player)) args[i] = minecraft.player;
                else return false;
            }
            previewMethod.invoke(null, args); return true;
        } catch (Throwable t) { previewMethod = null; return false; }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean dbl) {
        if (super.mouseClicked(e, dbl)) return true;
        int mouseX = (int) e.x(), mouseY = (int) e.y();
        if (mouseX < SIDEBAR) { for (int i = 0; i < Cosmetics.CATEGORIES.length; i++) { int y = 40 + i * 22; if (mouseY >= y && mouseY < y + 20) { category = i; rebuild(); return true; } } return false; }
        int n = cols(); List<Cosmetic> all = all();
        for (int i = 0; i < all.size(); i++) {
            int x = SIDEBAR + 16 + (i % n) * (CARD_W + GAP), y = 44 + (i / n) * (CARD_H + GAP);
            if (mouseX >= x && mouseX < x + CARD_W && mouseY >= y && mouseY < y + CARD_H) { Cosmetic c = all.get(i); LellekHud.config.cosmetic = c == null ? "" : c.id; LellekHud.config.save(); return true; }
        }
        return false;
    }
    @Override
    public void onClose() { LellekHud.config.save(); minecraft.gui.setScreen(parent); }
    @Override
    public boolean isPauseScreen() { return false; }
}
