package gg.lellek.hud.gui;

import com.mojang.blaze3d.platform.NativeImage;
import gg.lellek.hud.feature.Mods;
import gg.lellek.hud.map.MapStore;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;

/**
 * Vollbild-Weltkarte (Taste M) im Xaero-Stil: ganze Bildschirmfläche, gespeicherte Gebiete, aktuelle Höhlenebene,
 * Mobs und Spieler als Punkte, Wegpunkte. Mausrad zoomt zum Cursor, Ziehen verschiebt, Klick = Wegpunkt, Rechtsklick auf Marker = bearbeiten.
 */
public class FullMapScreen extends Screen {
    private static final float[] SCALES = { 0.25f, 0.5f, 1, 2, 4 }; // Blöcke pro GUI-Pixel
    private static int scaleIdx = 2;
    private static DynamicTexture texture; private static int texW, texH;
    private static final Identifier LOC = Identifier.fromNamespaceAndPath("lellekhud", "fullmap");
    private double cx, cz; private boolean centered;
    private double builtCx = Double.NaN, builtCz; private int builtScale = -1; private long builtAt; private String builtLayer = "";
    private boolean dragging, leftDown; private double dragX, dragY, dragCx, dragCz;

    public FullMapScreen() { super(Component.literal("Karte")); }

    @Override
    protected void init() {
        if (!centered && minecraft.player != null) { cx = minecraft.player.getX(); cz = minecraft.player.getZ(); centered = true; }
        addRenderableWidget(Button.builder(Component.literal("Wegpunkte"), b -> minecraft.gui.setScreen(new WaypointsScreen(this))).bounds(width - 84, height - 24, 80, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Zentrieren"), b -> { if (minecraft.player != null) { cx = minecraft.player.getX(); cz = minecraft.player.getZ(); } }).bounds(width - 168, height - 24, 80, 20).build());
    }
    private float scale() { return SCALES[scaleIdx]; }
    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float pt) {}

    /** Kartenbild in Bildschirmauflösung neu aufbauen (nur gespeicherte Daten → schnell) */
    private void rebuild() {
        int w = width, h = height;
        if (texture == null || texW != w || texH != h) {
            if (texture != null) texture.close();
            texture = new DynamicTexture(() -> "lellek_fullmap", w, h, false); texW = w; texH = h;
            minecraft.getTextureManager().register(LOC, texture); builtCx = Double.NaN;
        }
        NativeImage img = texture.getPixels(); if (img == null) return;
        float s = scale(); double left = cx - w / 2.0 * s, top = cz - h / 2.0 * s;
        int[] reg = null; int regX = Integer.MIN_VALUE, regZ = Integer.MIN_VALUE;
        for (int y = 0; y < h; y++) {
            int bz = (int) Math.floor(top + (y + 0.5) * s), rz = Math.floorDiv(bz, 512), oz = Math.floorMod(bz, 512) * 512;
            for (int x = 0; x < w; x++) {
                int bx = (int) Math.floor(left + (x + 0.5) * s), rx = Math.floorDiv(bx, 512);
                if (rx != regX || rz != regZ) { reg = MapStore.regionArray(rx, rz); regX = rx; regZ = rz; }
                int c = reg[oz + Math.floorMod(bx, 512)];
                img.setPixel(x, y, c == 0 ? 0xFF0A0A0A : c);
            }
        }
        texture.upload();
        builtCx = cx; builtCz = cz; builtScale = scaleIdx; builtAt = System.currentTimeMillis(); builtLayer = MapStore.layerName();
    }
    private int sx(double bx) { return (int) Math.round(width / 2.0 + (bx - cx) / scale()); }
    private int sy(double bz) { return (int) Math.round(height / 2.0 + (bz - cz) / scale()); }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float pt) {
        if (minecraft.level == null || minecraft.player == null) return;
        if (dragging && leftDown) { cx = dragCx - (mouseX - dragX) * scale(); cz = dragCz - (mouseY - dragY) * scale(); }
        if (Double.isNaN(builtCx) || builtCx != cx || builtCz != cz || builtScale != scaleIdx || System.currentTimeMillis() - builtAt > 1000 || !builtLayer.equals(MapStore.layerName())) rebuild();
        g.blit(RenderPipelines.GUI_TEXTURED, LOC, 0, 0, 0, 0, width, height, width, height);

        // Mobs & Spieler (nur in geladener Reichweite sichtbar)
        if (Mods.MINIMAP.boolOf("mapmobs")) {
            double r = 160;
            for (Entity e : minecraft.level.getEntities(minecraft.player, minecraft.player.getBoundingBox().inflate(r, 64, r), en -> en instanceof LivingEntity)) {
                int x = sx(e.getX()), y = sy(e.getZ()); if (x < 0 || y < 0 || x > width || y > height) continue;
                boolean pl = e instanceof Player;
                int col = pl ? 0xFFFFFFFF : e.getType().getCategory() == MobCategory.MONSTER ? 0xFFFF4040 : 0xFFE8C840;
                g.fill(x - 2, y - 2, x + 3, y + 3, 0xFF000000); g.fill(x - 1, y - 1, x + 2, y + 2, col);
                if (pl) Gfx.centered(g, font, e.getName().getString(), x, y - 11, 0xFFFFFFFF);
            }
        }
        // Wegpunkte
        for (Waypoint w : Waypoints.here()) {
            if (!w.visible) continue; int wx = sx(w.x + 0.5), wz = sy(w.z + 0.5);
            if (wx < -10 || wz < -10 || wx > width + 10 || wz > height + 10) continue;
            g.fill(wx - 4, wz - 4, wx + 4, wz + 4, 0xFF000000); g.fill(wx - 3, wz - 3, wx + 3, wz + 3, w.color);
            Gfx.centered(g, font, w.name, wx, wz - 14, 0xFFFFFFFF);
        }
        // Eigener Spieler als Pfeil
        int px = sx(minecraft.player.getX()), pz = sy(minecraft.player.getZ());
        g.pose().pushMatrix(); g.pose().translate(px, pz); g.pose().rotate((float) Math.toRadians(minecraft.player.getYRot() + 180));
        g.fill(-2, -7, 3, 6, 0xFF000000); g.fill(-4, 0, 5, 4, 0xFF000000);
        g.fill(-1, -6, 2, 5, 0xFFFFFFFF); g.fill(-3, 1, 4, 3, 0xFFFFFFFF); g.fill(-1, -6, 2, -3, 0xFFFF4040);
        g.pose().popMatrix();

        // Kopfzeile wie Xaero: Cursor-Koordinaten + Ebene, unten Zoom
        int bx = (int) Math.floor(cx + (mouseX - width / 2.0) * scale()), bz = (int) Math.floor(cz + (mouseY - height / 2.0) * scale());
        String head = "X: " + bx + "  Z: " + bz + "   ·   " + MapStore.layerName();
        g.fill(width / 2 - font.width(head) / 2 - 6, 2, width / 2 + font.width(head) / 2 + 6, 14, 0x90000000);
        Gfx.centered(g, font, head, width / 2, 4, 0xFFFFFFFF);
        String zoom = (scale() >= 1 ? (int) scale() + " Blöcke/Pixel" : (int) (1 / scale()) + " Pixel/Block") + "  ·  Mausrad = Zoom · Ziehen = verschieben · Klick = Wegpunkt";
        Gfx.text(g, font, zoom, 6, height - 14, 0xFFBBBBBB);
        super.extractRenderState(g, mouseX, mouseY, pt);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean dbl) {
        if (super.mouseClicked(e, dbl)) return true;
        int mouseX = (int) e.x(), mouseY = (int) e.y();
        for (Waypoint w : Waypoints.here()) { int wx = sx(w.x + 0.5), wz = sy(w.z + 0.5); if (Math.abs(mouseX - wx) <= 5 && Math.abs(mouseY - wz) <= 5) { if (e.button() == gg.lellek.hud.Mouse.RIGHT) minecraft.gui.setScreen(new WaypointEditScreen(this, w, false)); return true; } }
        if (e.button() == gg.lellek.hud.Mouse.LEFT) { dragging = true; leftDown = true; dragX = e.x(); dragY = e.y(); dragCx = cx; dragCz = cz; }
        return true;
    }
    @Override
    public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) { if (dragging) { cx = dragCx - (e.x() - dragX) * scale(); cz = dragCz - (e.y() - dragY) * scale(); return true; } return super.mouseDragged(e, dx, dy); }
    @Override
    public boolean mouseReleased(MouseButtonEvent e) {
        if (e.button() == gg.lellek.hud.Mouse.LEFT) leftDown = false;
        if (dragging && Math.abs(e.x() - dragX) < 3 && Math.abs(e.y() - dragY) < 3 && minecraft.player != null) {
            int bx = (int) Math.floor(cx + (e.x() - width / 2.0) * scale()), bz = (int) Math.floor(cz + (e.y() - height / 2.0) * scale());
            minecraft.gui.setScreen(new WaypointEditScreen(this, new Waypoint("", Waypoints.currentWorld(), bx, minecraft.player.getBlockY(), bz, 0xFFFFB400), true));
        }
        dragging = false; return super.mouseReleased(e);
    }
    /** Zoom zum Mauszeiger */
    @Override
    public boolean mouseScrolled(double mx, double my, double dx, double dy) {
        double wx = cx + (mx - width / 2.0) * scale(), wz = cz + (my - height / 2.0) * scale();
        scaleIdx = Math.max(0, Math.min(SCALES.length - 1, scaleIdx - (int) Math.signum(dy)));
        cx = wx - (mx - width / 2.0) * scale(); cz = wz - (my - height / 2.0) * scale();
        return true;
    }
    @Override
    public boolean isPauseScreen() { return false; }
}
