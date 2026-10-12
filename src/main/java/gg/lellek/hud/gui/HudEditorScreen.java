package gg.lellek.hud.gui;

import gg.lellek.hud.LellekHud;
import gg.lellek.hud.element.HudElement;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** HUD-Editor: ziehen = verschieben (rastet an Mitte/Rändern/anderen Elementen ein), Mausrad = Größe, Rechtsklick = Einstellungen, ESC = speichern. */
public class HudEditorScreen extends Screen {
    private static final int SNAP = 4;
    private HudElement dragging;
    private int dragOffX, dragOffY;
    private int guideX = -1, guideY = -1;

    public HudEditorScreen() { super(Component.literal("LellekClient HUD-Editor")); }

    @Override
    protected void init() {
        int cx = width / 2;
        addRenderableWidget(Button.builder(Component.literal("Mod-Menü"), b -> minecraft.gui.setScreen(new ModMenuScreen(this))).bounds(cx - 103, height - 28, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Zurücksetzen"), b -> LellekHud.config.reset()).bounds(cx + 3, height - 28, 100, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float pt) {
        super.extractRenderState(g, mouseX, mouseY, pt);
        if (dragging != null) {
            if (guideX >= 0) g.fill(guideX, 0, guideX + 1, height, 0x80FFB400);
            if (guideY >= 0) g.fill(0, guideY, width, guideY + 1, 0x80FFB400);
        }
        for (HudElement el : LellekHud.config.elements) {
            el.layout(width, height);
            boolean hover = el.contains(mouseX, mouseY);
            int border = el == dragging ? 0xFFFF8F1C : hover ? 0xFFFFB400 : el.enabled ? 0x60FFFFFF : 0x60FF5555;
            g.outline(el.x - 1, el.y - 1, el.scaledWidth() + 2, el.scaledHeight() + 2, border);
            if (el.enabled) el.draw(g);
            else gg.lellek.hud.gui.Gfx.centered(g, font, el.name + " (aus)", el.x + el.scaledWidth() / 2, el.y + el.scaledHeight() / 2 - 4, 0xFF888888);
        }
        gg.lellek.hud.gui.Gfx.centered(g, font, "LellekClient HUD-Editor", width / 2, 8, 0xFFFFB400);
        gg.lellek.hud.gui.Gfx.centered(g, font, "Ziehen = verschieben   Mausrad = Größe   Rechtsklick = Einstellungen   ESC = speichern", width / 2, 20, 0xFFAAAAAA);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean doubleClick) {
        if (super.mouseClicked(e, doubleClick)) return true;
        int mx = (int) e.x(), my = (int) e.y();
        for (int i = LellekHud.config.elements.size() - 1; i >= 0; i--) {
            HudElement el = LellekHud.config.elements.get(i);
            if (!el.contains(mx, my)) continue;
            if (e.button() == 1) { minecraft.gui.setScreen(new ElementSettingsScreen(this, el)); return true; }
            if (e.button() == 0) { dragging = el; dragOffX = mx - el.x; dragOffY = my - el.y; return true; }
        }
        return false;
    }
    @Override
    public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) {
        if (dragging != null) {
            int nx = (int) e.x() - dragOffX, ny = (int) e.y() - dragOffY, ew = dragging.scaledWidth(), eh = dragging.scaledHeight();
            guideX = guideY = -1;
            // Einrasten: Bildschirmmitte, Ränder (2 px Abstand) und Kanten anderer Elemente
            int[] sx = snap(nx, ew, width, true), sy = snap(ny, eh, height, false);
            nx = sx[0]; guideX = sx[1]; ny = sy[0]; guideY = sy[1];
            dragging.moveTo(nx, ny, width, height);
            return true;
        }
        return super.mouseDragged(e, dx, dy);
    }
    /** liefert {neue Position, Hilfslinie oder -1} */
    private int[] snap(int pos, int size, int screen, boolean horizontal) {
        int center = screen / 2;
        if (Math.abs(pos + size / 2 - center) <= SNAP) return new int[] { center - size / 2, center };
        if (Math.abs(pos - 2) <= SNAP) return new int[] { 2, -1 };
        if (Math.abs(pos + size - (screen - 2)) <= SNAP) return new int[] { screen - 2 - size, -1 };
        for (HudElement o : LellekHud.config.elements) {
            if (o == dragging || !o.enabled) continue;
            int a = horizontal ? o.x : o.y, b = a + (horizontal ? o.scaledWidth() : o.scaledHeight());
            if (Math.abs(pos - a) <= SNAP) return new int[] { a, a };
            if (Math.abs(pos - b) <= SNAP) return new int[] { b, b };
            if (Math.abs(pos + size - b) <= SNAP) return new int[] { b - size, b };
            if (Math.abs(pos + size - a) <= SNAP) return new int[] { a - size, a };
        }
        return new int[] { pos, -1 };
    }
    @Override
    public boolean mouseScrolled(double mx, double my, double sdx, double sdy) {
        for (int i = LellekHud.config.elements.size() - 1; i >= 0; i--) {
            HudElement el = LellekHud.config.elements.get(i);
            if (!el.contains((int) mx, (int) my) || sdy == 0) continue;
            // Größe ändern – der Anker (z. B. rechte Kante) bleibt dabei stehen
            el.scale = Math.max(50, Math.min(300, el.scale + (sdy > 0 ? 10 : -10)));
            el.layout(width, height);
            return true;
        }
        return super.mouseScrolled(mx, my, sdx, sdy);
    }
    @Override
    public boolean mouseReleased(MouseButtonEvent e) { dragging = null; guideX = guideY = -1; return super.mouseReleased(e); }
    @Override
    public void onClose() { LellekHud.config.save(); super.onClose(); }
    @Override
    public boolean isPauseScreen() { return false; }
}
