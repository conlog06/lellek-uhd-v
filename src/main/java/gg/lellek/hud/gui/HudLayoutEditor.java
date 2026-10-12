package gg.lellek.hud.gui;

import gg.lellek.hud.LellekHud;
import gg.lellek.hud.element.HudElement;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Gemeinsame Verschiebe-Logik für HUD-Editor und Mod-Menü: ziehen (mit Einrasten), Mausrad = Größe. */
public class HudLayoutEditor {
    private static final int SNAP = 4;
    private HudElement dragging, focus;
    private int dragOffX, dragOffY, guideX = -1, guideY = -1;
    private long focusSince;

    /** Element hervorheben (z. B. aus dem Mod-Menü heraus gewählt) */
    public void focus(HudElement el) { focus = el; focusSince = System.currentTimeMillis(); if (el != null && !el.enabled) el.enabled = true; }

    public void render(GuiGraphicsExtractor g, Font font, int width, int height, int mouseX, int mouseY) {
        if (dragging != null) {
            if (guideX >= 0) g.fill(guideX, 0, guideX + 1, height, 0x80FFB400);
            if (guideY >= 0) g.fill(0, guideY, width, guideY + 1, 0x80FFB400);
        }
        boolean pulse = focus != null && System.currentTimeMillis() - focusSince < 2500 && (System.currentTimeMillis() / 250) % 2 == 0;
        for (HudElement el : LellekHud.config.elements) {
            el.layout(width, height);
            if (!el.enabled) continue; // ausgeschaltete Elemente nicht als Platzhalter über die aktiven legen
            boolean hover = el == hoveredTop(mouseX, mouseY);
            int border = el == dragging ? 0xFFFF8F1C : (el == focus && pulse) ? 0xFFFFFFFF : hover || el == focus ? 0xFFFFB400 : el.enabled ? 0x60FFFFFF : 0x60FF5555;
            g.outline(el.x - 1, el.y - 1, el.scaledWidth() + 2, el.scaledHeight() + 2, border);
            if (el.enabled) el.draw(g);
            if (el == dragging || hover) { String tag = el.name + " \u00b7 " + el.scale + "%"; int ty = el.y >= 12 ? el.y - 11 : el.y + el.scaledHeight() + 2; g.fill(el.x - 1, ty - 1, el.x + font.width(tag) + 3, ty + 9, 0xC0000000); g.text(font, tag, el.x + 1, ty, 0xFFFFB400, false); }
            else Gfx.centered(g, font, el.name + " (aus)", el.x + el.scaledWidth() / 2, el.y + el.scaledHeight() / 2 - 4, 0xFF888888);
        }
    }

    /** Oberstes sichtbares Element unter der Maus */
    public HudElement hoveredTop(int mx, int my) {
        for (int i = LellekHud.config.elements.size() - 1; i >= 0; i--) { HudElement el = LellekHud.config.elements.get(i); if (el.enabled && el.contains(mx, my)) return el; }
        return null;
    }
    /** @return angeklicktes Element bei Rechtsklick (für Einstellungen), sonst null */
    public HudElement click(int mx, int my, int button) {
        for (int i = LellekHud.config.elements.size() - 1; i >= 0; i--) {
            HudElement el = LellekHud.config.elements.get(i);
            if (!el.enabled || !el.contains(mx, my)) continue;
            if (button == gg.lellek.hud.Mouse.RIGHT) return el;
            if (button == gg.lellek.hud.Mouse.LEFT) { dragging = el; focus = el; dragOffX = mx - el.x; dragOffY = my - el.y; }
            return null;
        }
        return null;
    }
    public boolean isOverElement(int mx, int my) { for (HudElement el : LellekHud.config.elements) if (el.contains(mx, my)) return true; return false; }
    public boolean isDragging() { return dragging != null; }
    public HudElement focused() { return focus; }
    /** Element pixelgenau verschieben (Pfeiltasten / Buttons) */
    public static void nudge(HudElement el, int dx, int dy, int width, int height) {
        if (el == null) return; el.layout(width, height); el.moveTo(el.x + dx, el.y + dy, width, height); LellekHud.config.save();
    }

    public boolean drag(int mx, int my, int width, int height) {
        if (dragging == null) return false;
        int nx = mx - dragOffX, ny = my - dragOffY;
        int[] sx = snap(nx, dragging.scaledWidth(), width, true), sy = snap(ny, dragging.scaledHeight(), height, false);
        guideX = sx[1]; guideY = sy[1];
        dragging.moveTo(sx[0], sy[0], width, height);
        return true;
    }
    /** liefert {neue Position, Hilfslinie oder -1}: Bildschirmmitte, Ränder, Kanten anderer Elemente */
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
    /** Mausrad über Element = Größe ändern, Anker bleibt stehen */
    public boolean scroll(int mx, int my, double dy, int width, int height) {
        if (dy == 0) return false;
        for (int i = LellekHud.config.elements.size() - 1; i >= 0; i--) {
            HudElement el = LellekHud.config.elements.get(i);
            if (!el.enabled || !el.contains(mx, my)) continue;
            el.scale = Math.max(50, Math.min(300, el.scale + (dy > 0 ? 10 : -10)));
            el.layout(width, height);
            return true;
        }
        return false;
    }
    public void release() { if (dragging != null) LellekHud.config.save(); dragging = null; guideX = guideY = -1; }
}
