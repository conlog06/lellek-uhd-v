package gg.lellek.hud.element;

import gg.lellek.hud.feature.Mods;
import gg.lellek.hud.feature.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.List;

/** Basis für alle HUD-Elemente: Anker-Standardposition, Sichtbarkeit, Farbe, Größe, Hintergrund, eigene Einstellungen. */
public abstract class HudElement {
    public static final int GOLD = 0xFFFFB400, WHITE = 0xFFFFFFFF, PAD = 3;
    public static final int TL = 0, TR = 1, BL = 2, BR = 3, BC = 4, TC = 5;

    public final String id, name;
    /** Zeichen-Position in GUI-Pixeln – wird jedes Frame aus Anker + Abstand berechnet (layout). */
    public int x, y;
    /** Anker je Achse: 0 = links/oben, 0.5 = Mitte, 1 = rechts/unten; -1 = Standard-Anker. ox/oy = Abstand zum Anker. */
    public float ax = -1, ay = -1;
    public int ox, oy;
    private int legacyX = Integer.MIN_VALUE, legacyY;
    public boolean enabled = true, background = true;
    public int color = GOLD, scale = 100;
    public final List<Setting> settings = new ArrayList<>();
    private final int anchor, dx, dy;

    protected HudElement(String id, String name, int anchor, int dx, int dy) { this.id = id; this.name = name; this.anchor = anchor; this.dx = dx; this.dy = dy; }
    protected HudElement(String id, String name, int anchor, int dx, int dy, boolean enabledByDefault) { this(id, name, anchor, dx, dy); this.enabled = enabledByDefault; }

    public abstract int getWidth();
    public abstract int getHeight();
    protected abstract void render(GuiGraphicsExtractor g);

    protected Setting add(Setting s) { settings.add(s); return s; }
    public Setting setting(String id) { for (Setting s : settings) if (s.id.equals(id)) return s; return null; }
    public int intOf(String id) { return ((Setting.Int) setting(id)).value; }
    public boolean boolOf(String id) { return ((Setting.Bool) setting(id)).value; }
    public int choiceOf(String id) { return ((Setting.Choice) setting(id)).index; }
    public int colorOf(String id) { return ((Setting.Color) setting(id)).value; }

    public float scale() { return scale / 100f; }
    public int scaledWidth() { return Math.round(getWidth() * scale()); }
    public int scaledHeight() { return Math.round(getHeight() * scale()); }

    public void draw(GuiGraphicsExtractor g) { g.pose().pushMatrix(); g.pose().translate(x, y); g.pose().scale(scale(), scale()); render(g); g.pose().popMatrix(); }
    public void resetPosition() { ax = ay = -1; ox = oy = 0; legacyX = Integer.MIN_VALUE; enabled = true; color = GOLD; scale = 100; background = true; }
    /** Alte Configs (≤1.2) speicherten absolute Pixel – werden beim ersten Layout in Anker + Abstand umgerechnet. */
    public void setLegacyPosition(int lx, int ly) { if (lx >= 0 && ly >= 0) { legacyX = lx; legacyY = ly; } }
    public boolean hasCustomAnchor() { return ax >= 0 && ay >= 0; }
    public boolean hasLegacyPosition() { return legacyX != Integer.MIN_VALUE; }
    public int legacyX() { return legacyX; }
    public int legacyY() { return legacyY; }

    private void applyDefaultAnchor() {
        switch (anchor) {
            case TR -> { ax = 1; ay = 0; ox = -dx; oy = dy; }
            case BL -> { ax = 0; ay = 1; ox = dx; oy = -dy; }
            case BR -> { ax = 1; ay = 1; ox = -dx; oy = -dy; }
            case BC -> { ax = 0.5f; ay = 1; ox = dx; oy = -dy; }
            case TC -> { ax = 0.5f; ay = 0; ox = dx; oy = dy; }
            default -> { ax = 0; ay = 0; ox = dx; oy = dy; }
        }
    }
    private static float pickAnchor(float center, int size) { return center < size / 3f ? 0f : center > size * 2f / 3f ? 1f : 0.5f; }

    /** Berechnet x/y für die aktuelle Bildschirmgröße. Die gespeicherte Position (Anker + Abstand) wird dabei nie verändert,
     *  deshalb bleibt ein Element an seinem Platz, egal ob Fenster, GUI-Skalierung oder Element-Größe sich ändern. */
    public void layout(int w, int h) {
        if (!hasCustomAnchor()) {
            if (legacyX != Integer.MIN_VALUE) { moveTo(legacyX, legacyY, w, h); legacyX = Integer.MIN_VALUE; return; }
            applyDefaultAnchor();
        }
        int ew = scaledWidth(), eh = scaledHeight();
        x = Math.round(ax * (w - ew)) + ox;
        y = Math.round(ay * (h - eh)) + oy;
        x = Math.max(0, Math.min(x, w - ew)); y = Math.max(0, Math.min(y, h - eh));
    }
    /** Element an eine Pixel-Position setzen (Editor): wählt den nächsten Anker (Drittel des Bildschirms) und speichert den Abstand dazu. */
    public void moveTo(int nx, int ny, int w, int h) {
        int ew = scaledWidth(), eh = scaledHeight();
        nx = Math.max(0, Math.min(nx, w - ew)); ny = Math.max(0, Math.min(ny, h - eh));
        ax = pickAnchor(nx + ew / 2f, w); ay = pickAnchor(ny + eh / 2f, h);
        ox = nx - Math.round(ax * (w - ew)); oy = ny - Math.round(ay * (h - eh));
        x = nx; y = ny;
    }
    /** Kompatibilität: früher clamp(), jetzt layout(). */
    public void clamp(int w, int h) { layout(w, h); }
    public boolean contains(int mx, int my) { return mx >= x && my >= y && mx < x + scaledWidth() && my < y + scaledHeight(); }

    protected static Font font() { return Minecraft.getInstance().font; }
    protected static void text(GuiGraphicsExtractor g, String s, int x, int y, int color) { g.text(font(), s, x, y, color, Mods.SHADOW.enabled); }
    protected void bg(GuiGraphicsExtractor g, int w, int h) { if (background) g.fill(0, 0, w, h, 0x80000000); }
    protected void labeled(GuiGraphicsExtractor g, String label, String value) { bg(g, labeledWidth(label, value), 9 + PAD * 2); text(g, label, PAD, PAD, color); text(g, value, PAD + font().width(label + " "), PAD, WHITE); }
    protected static int labeledWidth(String label, String value) { return font().width(label + " " + value) + PAD * 2; }
}
