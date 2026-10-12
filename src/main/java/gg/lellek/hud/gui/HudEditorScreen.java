package gg.lellek.hud.gui;

import gg.lellek.hud.LellekHud;
import gg.lellek.hud.element.HudElement;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * HUD-Editor (rechte Shift oder Mod-Menü → HUD verschieben):
 * Elemente ziehen (rastet ein), Mausrad = Größe, Rechtsklick = Einstellungen.
 * Links eine Liste aller Elemente: Kästchen = an/aus, Name = hervorheben, ⚙ = Einstellungen.
 */
public class HudEditorScreen extends Screen {
    private static final int PANEL_W = 136, ROW = 13, GOLD = 0xFFFFB400, MUTED = 0xFFB0A38E;
    private final HudLayoutEditor editor = new HudLayoutEditor();
    private final Screen parent;
    private static boolean panelOpen = true;
    private float panelScroll;

    public HudEditorScreen() { this(null, null); }
    public HudEditorScreen(Screen parent, HudElement focus) { super(Component.literal("LellekClient HUD-Editor")); this.parent = parent; editor.focus(focus); }

    @Override
    protected void init() {
        int cx = width / 2;
        addRenderableWidget(Button.builder(Component.literal("Mod-Menü"), b -> minecraft.gui.setScreen(new ModMenuScreen(this))).bounds(cx - 154, height - 28, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Zurücksetzen"), b -> LellekHud.config.reset()).bounds(cx - 50, height - 28, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("✔ Fertig"), b -> onClose()).bounds(cx + 54, height - 28, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal(panelOpen ? "◀" : "☰"), b -> { panelOpen = !panelOpen; rebuildWidgets(); }).bounds(panelOpen ? PANEL_W + 2 : 4, 4, 18, 18).build());
    }
    /** Kein Weichzeichner – das HUD soll genau so aussehen wie im Spiel */
    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float pt) { g.fill(0, 0, width, height, 0x28000000); }

    private List<HudElement> els() { return LellekHud.config.elements; }
    private int listTop() { return 26; }
    private int maxScroll() { return Math.max(0, els().size() * ROW - (height - 40 - listTop())); }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float pt) {
        // Ziehen auch ohne mouseDragged-Events nachführen (robust gegen Eingabe-Eigenheiten)
        if (editor.isDragging()) { if (!minecraft.mouseHandler.isLeftPressed()) editor.release(); else editor.drag(mouseX, mouseY, width, height); }
        super.extractRenderState(g, mouseX, mouseY, pt);
        editor.render(g, font, width, height, mouseX, mouseY);
        Gfx.centered(g, font, "HUD-Editor", width / 2, 6, GOLD);
        Gfx.centered(g, font, "Ziehen = verschieben · Mausrad = Größe · Rechtsklick = Einstellungen · ESC = speichern", width / 2, 17, 0xFFAAAAAA);
        if (panelOpen) drawPanel(g, mouseX, mouseY);
        for (var r : List.copyOf(children())) if (r instanceof Button b) b.extractRenderState(g, mouseX, mouseY, pt); // Buttons über Panel/Elementen
    }

    private void drawPanel(GuiGraphicsExtractor g, int mx, int my) {
        g.fill(0, 0, PANEL_W, height, 0xE0130E08); g.fill(PANEL_W, 0, PANEL_W + 1, height, 0xFF3B2E1C);
        Gfx.text(g, font, "Elemente", 8, 9, GOLD);
        g.enableScissor(0, listTop(), PANEL_W, height - 34);
        for (int i = 0; i < els().size(); i++) {
            HudElement el = els().get(i); int y = listTop() + i * ROW - (int) panelScroll;
            if (y + ROW < listTop() || y > height - 34) continue;
            boolean hov = mx < PANEL_W && my >= y && my < y + ROW;
            if (hov) g.fill(2, y, PANEL_W - 2, y + ROW, 0x20FFFFFF);
            g.fill(6, y + 2, 15, y + 11, 0xFF3B2E1C); if (el.enabled) g.fill(7, y + 3, 14, y + 10, GOLD);
            Gfx.text(g, font, font.plainSubstrByWidth(el.name, PANEL_W - 40), 20, y + 3, el.enabled ? 0xFFFFFFFF : 0xFF888888);
            Gfx.text(g, font, "⚙", PANEL_W - 14, y + 3, hov && mx >= PANEL_W - 18 ? GOLD : MUTED);
        }
        g.disableScissor();
    }
    private boolean panelClick(int mx, int my, int button) {
        if (!panelOpen || mx >= PANEL_W) return false;
        int i = (int) ((my - listTop() + panelScroll) / ROW);
        if (my < listTop() || i < 0 || i >= els().size()) return true;
        HudElement el = els().get(i);
        if (mx >= PANEL_W - 18 || button == 1) { minecraft.gui.setScreen(new ElementSettingsScreen(this, el)); return true; }
        if (mx < 18) el.enabled = !el.enabled; else editor.focus(el);
        LellekHud.config.save();
        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean doubleClick) {
        if (super.mouseClicked(e, doubleClick)) return true;
        if (panelClick((int) e.x(), (int) e.y(), e.button())) return true;
        HudElement settings = editor.click((int) e.x(), (int) e.y(), e.button());
        if (settings != null) { minecraft.gui.setScreen(new ElementSettingsScreen(this, settings)); return true; }
        return editor.isDragging();
    }
    @Override
    public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) { return editor.drag((int) e.x(), (int) e.y(), width, height) || super.mouseDragged(e, dx, dy); }
    @Override
    public boolean mouseScrolled(double mx, double my, double sdx, double sdy) {
        if (panelOpen && mx < PANEL_W) { panelScroll = (float) Math.max(0, Math.min(maxScroll(), panelScroll - Math.signum(sdy) * ROW * 3)); return true; }
        return editor.scroll((int) mx, (int) my, sdy, width, height) || super.mouseScrolled(mx, my, sdx, sdy);
    }
    /** Pfeiltasten verschieben das gewählte Element um 1 px (mit Shift 10 px) */
    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent e) {
        HudElement f = editor.focused();
        if (f != null) {
            int step = (e.modifiers() & 1) != 0 ? 10 : 1, dx = 0, dy = 0;
            switch (e.key()) {
                case com.mojang.blaze3d.platform.InputConstants.KEY_LEFT -> dx = -step;
                case com.mojang.blaze3d.platform.InputConstants.KEY_RIGHT -> dx = step;
                case com.mojang.blaze3d.platform.InputConstants.KEY_UP -> dy = -step;
                case com.mojang.blaze3d.platform.InputConstants.KEY_DOWN -> dy = step;
                default -> {}
            }
            if (dx != 0 || dy != 0) { HudLayoutEditor.nudge(f, dx, dy, width, height); return true; }
        }
        return super.keyPressed(e);
    }
    @Override
    public boolean mouseReleased(MouseButtonEvent e) { editor.release(); return super.mouseReleased(e); }
    @Override
    public void onClose() { editor.release(); LellekHud.config.save(); if (parent != null) minecraft.gui.setScreen(parent); else super.onClose(); }
    @Override
    public boolean isPauseScreen() { return false; }
}
