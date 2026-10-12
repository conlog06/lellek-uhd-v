package gg.lellek.hud.gui;

import gg.lellek.hud.element.MinimapElement;
import gg.lellek.hud.waypoint.Waypoint;
import gg.lellek.hud.waypoint.Waypoints;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** Vollbild-Karte (Taste M) mit gespeicherten Gebieten (Overworld/Nether/End getrennt): Mausrad zoomt, Ziehen verschiebt, Klick setzt Wegpunkt, Rechtsklick auf Marker bearbeitet. */
public class FullMapScreen extends Screen {
    private static final int TEX = 256;
    private static DynamicTexture texture; private static final Identifier LOC = Identifier.fromNamespaceAndPath("lellekhud", "fullmap");
    private double cx, cz; private int zoom = 2; private int lastCx = Integer.MIN_VALUE, lastCz, lastZoom = -1;
    private boolean dragging; private double dragX, dragY, dragCx, dragCz;
    public FullMapScreen() { super(Component.literal("Karte")); }

    @Override
    protected void init() {
        if (lastCx == Integer.MIN_VALUE && minecraft.player != null) { cx = minecraft.player.getX(); cz = minecraft.player.getZ(); }
        addRenderableWidget(Button.builder(Component.literal("Wegpunkt-Liste"), b -> minecraft.gui.setScreen(new WaypointsScreen(this))).bounds(width - 110, height - 26, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Zentrieren"), b -> { if (minecraft.player != null) { cx = minecraft.player.getX(); cz = minecraft.player.getZ(); } }).bounds(width - 216, height - 26, 100, 20).build());
    }
    private int size() { return Math.min(width, height) - 40; }
    private int mapX() { return (width - size()) / 2; }
    private int mapY() { return 14; }
    private double scale() { return TEX * zoom / (double) size(); }
    private void update() {
        if (texture == null) { texture = new DynamicTexture(() -> "lellek_fullmap", TEX, TEX, false); minecraft.getTextureManager().register(LOC, texture); }
        int icx = (int) Math.floor(cx), icz = (int) Math.floor(cz);
        if (icx == lastCx && icz == lastCz && zoom == lastZoom) return;
        lastCx = icx; lastCz = icz; lastZoom = zoom;
        var img = texture.getPixels(); if (img == null) return;
        for (int py = 0; py < TEX; py++) for (int px = 0; px < TEX; px++) { int argb = MinimapElement.colorAt(minecraft.level, icx + (px - TEX / 2) * zoom, icz + (py - TEX / 2) * zoom); img.setPixel(px, py, argb); }
        texture.upload();
    }
    private int toScreenX(double bx) { return mapX() + (int) ((bx - cx) / scale()) + size() / 2; }
    private int toScreenZ(double bz) { return mapY() + (int) ((bz - cz) / scale()) + size() / 2; }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float pt) {
        super.extractRenderState(g, mouseX, mouseY, pt);
        if (minecraft.level == null) return;
        update();
        int s = size(), x = mapX(), y = mapY();
        g.fill(x - 2, y - 2, x + s + 2, y + s + 2, 0xFFFFB400);
        g.blit(RenderPipelines.GUI_TEXTURED, LOC, x, y, 0, 0, s, s, TEX, TEX, TEX, TEX);
        for (Waypoint w : Waypoints.here()) {
            if (!w.visible) continue; int wx = toScreenX(w.x + 0.5), wz = toScreenZ(w.z + 0.5);
            if (wx < x || wx > x + s || wz < y || wz > y + s) continue;
            g.fill(wx - 3, wz - 3, wx + 3, wz + 3, w.color); g.fill(wx - 1, wz - 1, wx + 1, wz + 1, 0xFFFFFFFF); Gfx.centered(g, font, w.name, wx, wz - 12, 0xFFFFFFFF);
        }
        if (minecraft.player != null) { int px = toScreenX(minecraft.player.getX()), pz = toScreenZ(minecraft.player.getZ()); g.fill(px - 2, pz - 2, px + 2, pz + 2, 0xFFFFFFFF); }
        int bx = (int) Math.floor(cx + (mouseX - x - s / 2) * scale()), bz = (int) Math.floor(cz + (mouseY - y - s / 2) * scale());
        Gfx.text(g, font, "Karte \u00b7 " + zoom + " Blöcke/Pixel \u00b7 Cursor " + bx + " " + bz, 10, height - 20, 0xFFAAAAAA);
        Gfx.text(g, font, "Klick = Wegpunkt \u00b7 Rechtsklick = bearbeiten \u00b7 Ziehen = verschieben \u00b7 Mausrad = Zoom", 10, height - 10, 0xFF666666);
    }
    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean dbl) {
        if (super.mouseClicked(e, dbl)) return true;
        int mouseX = (int) e.x(), mouseY = (int) e.y(), s = size(), x = mapX(), y = mapY();
        if (mouseX < x || mouseX > x + s || mouseY < y || mouseY > y + s) return false;
        for (Waypoint w : Waypoints.here()) { int wx = toScreenX(w.x + 0.5), wz = toScreenZ(w.z + 0.5); if (Math.abs(mouseX - wx) <= 5 && Math.abs(mouseY - wz) <= 5) { if (e.button() == 1) minecraft.gui.setScreen(new WaypointEditScreen(this, w, false)); return true; } }
        if (e.button() == 0) { dragging = true; dragX = e.x(); dragY = e.y(); dragCx = cx; dragCz = cz; }
        return true;
    }
    @Override
    public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) { if (dragging) { cx = dragCx - (e.x() - dragX) * scale(); cz = dragCz - (e.y() - dragY) * scale(); return true; } return super.mouseDragged(e, dx, dy); }
    @Override
    public boolean mouseReleased(MouseButtonEvent e) {
        if (dragging && Math.abs(e.x() - dragX) < 3 && Math.abs(e.y() - dragY) < 3 && minecraft.player != null) {
            int bx = (int) Math.floor(cx + (e.x() - mapX() - size() / 2) * scale()), bz = (int) Math.floor(cz + (e.y() - mapY() - size() / 2) * scale());
            minecraft.gui.setScreen(new WaypointEditScreen(this, new Waypoint("", Waypoints.currentWorld(), bx, minecraft.player.getBlockY(), bz, 0xFFFFB400), true));
        }
        dragging = false; return super.mouseReleased(e);
    }
    @Override
    public boolean mouseScrolled(double mx, double my, double dx, double dy) { zoom = Math.max(1, Math.min(8, dy > 0 ? zoom / 2 : zoom * 2)); return true; }
    @Override
    public boolean isPauseScreen() { return false; }
}
