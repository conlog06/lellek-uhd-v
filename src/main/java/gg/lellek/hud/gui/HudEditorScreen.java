package gg.lellek.hud.gui;

import gg.lellek.hud.LellekHud;
import gg.lellek.hud.element.HudElement;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** HUD-Editor: Elemente mit der Maus ziehen (rastet ein), Mausrad = Größe, Rechtsklick = Einstellungen, ESC = speichern. */
public class HudEditorScreen extends Screen {
    private final HudLayoutEditor editor = new HudLayoutEditor();
    private final Screen parent;
    /** eigener Tastenzustand – MouseHandler.isLeftPressed() meldet bei offenem Screen immer false */
    private boolean leftDown;

    public HudEditorScreen() { this(null, null); }
    public HudEditorScreen(Screen parent, HudElement focus) { super(Component.literal("LellekClient HUD-Editor")); this.parent = parent; editor.focus(focus); }

    @Override
    protected void init() {
        int cx = width / 2;
        addRenderableWidget(Button.builder(Component.literal("Mod-Menü"), b -> minecraft.gui.setScreen(new ModMenuScreen(this))).bounds(cx - 103, height - 28, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Zurücksetzen"), b -> LellekHud.config.reset()).bounds(cx + 3, height - 28, 100, 20).build());
    }
    /** Kein Weichzeichner – das HUD sieht genau so aus wie im Spiel */
    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float pt) { g.fill(0, 0, width, height, 0x28000000); }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float pt) {
        // Ziehen jedes Frame der Maus nachführen – funktioniert auch, wenn 26.3 keine Drag-Events schickt
        if (editor.isDragging() && leftDown) editor.drag(mouseX, mouseY, width, height);
        editor.render(g, font, width, height, mouseX, mouseY);
        Gfx.centered(g, font, "HUD-Editor", width / 2, 6, 0xFFFFB400);
        Gfx.centered(g, font, "Ziehen = verschieben · Mausrad = Größe · Rechtsklick = Einstellungen · ESC = speichern", width / 2, 17, 0xFFAAAAAA);
        super.extractRenderState(g, mouseX, mouseY, pt);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean doubleClick) {
        if (super.mouseClicked(e, doubleClick)) return true;
        if (e.button() == 0) leftDown = true;
        HudElement settings = editor.click((int) e.x(), (int) e.y(), e.button());
        if (settings != null) { minecraft.gui.setScreen(new ElementSettingsScreen(this, settings)); return true; }
        return editor.isDragging();
    }
    @Override
    public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) { return editor.drag((int) e.x(), (int) e.y(), width, height) || super.mouseDragged(e, dx, dy); }
    @Override
    public boolean mouseScrolled(double mx, double my, double sdx, double sdy) { return editor.scroll((int) mx, (int) my, sdy, width, height) || super.mouseScrolled(mx, my, sdx, sdy); }
    /** Pfeiltasten schieben das zuletzt angeklickte Element pixelgenau (Shift = 10 px) */
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
    public boolean mouseReleased(MouseButtonEvent e) { if (e.button() == 0) leftDown = false; editor.release(); return super.mouseReleased(e); }
    @Override
    public void onClose() { editor.release(); LellekHud.config.save(); if (parent != null) minecraft.gui.setScreen(parent); else super.onClose(); }
    @Override
    public boolean isPauseScreen() { return false; }
}
