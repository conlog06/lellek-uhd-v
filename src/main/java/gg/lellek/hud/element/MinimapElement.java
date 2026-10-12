package gg.lellek.hud.element;

import com.mojang.blaze3d.platform.NativeImage;
import gg.lellek.hud.feature.Mods;
import gg.lellek.hud.feature.Setting;
import gg.lellek.hud.map.MapStore;
import gg.lellek.hud.waypoint.Waypoint;
import gg.lellek.hud.waypoint.Waypoints;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;

/**
 * Minimap im Xaero-Stil: dreht sich mit der Blickrichtung (abschaltbar), Himmelsrichtungen am Rand,
 * Mobs als Punkte (Spieler weiß, Monster rot, Tiere gelb), Wegpunkte, Koordinaten darunter.
 * Kartendaten aus {@link MapStore} – besuchte Gebiete bleiben gespeichert (Overworld, Nether, End).
 */
public class MinimapElement extends HudElement {
    private DynamicTexture texture; private Identifier location; private int texSize;
    /** Basispuffer (nicht gedreht) rund um den Spieler, 1 Wert pro Kartenpixel */
    private int[] base = new int[0]; private int baseN, baseCx = Integer.MIN_VALUE, baseCz, baseZoomIdx = -1; private long baseTime;
    private static final float[] ZOOMS = { 0.5f, 1, 2, 4 };

    public MinimapElement() {
        super("minimap", "Minimap", TR, 4, 4);
        add(new Setting.Int("size", "Größe", 110, 64, 200, "px"));
        add(new Setting.Choice("zoom", "Zoom", 1, "Nah (0,5)", "Normal (1)", "Weit (2)", "Sehr weit (4)"));
        add(new Setting.Bool("rotate", "Mit Blickrichtung drehen", true));
        add(new Setting.Bool("mobs", "Mobs anzeigen", true));
        add(new Setting.Bool("players", "Spieler anzeigen", true));
        add(new Setting.Bool("coords", "Koordinaten anzeigen", true));
        add(new Setting.Color("frame", "Rahmen", 0xFF2A2A2A));
    }
    private int size() { return intOf("size"); }
    public int getWidth() { return size() + 4; }
    public int getHeight() { return size() + 4 + (boolOf("coords") ? 12 : 0); }
    private float zoom() { return ZOOMS[choiceOf("zoom")]; }

    /** Farbe für Vollbild-Karte & Co. */
    public static int colorAt(ClientLevel level, int bx, int bz) { return MapStore.color(level, bx, bz); }

    private void rebuildBase(Minecraft mc, int cx, int cz) {
        int s = size(); float z = zoom();
        int n = (int) Math.ceil(s * 1.45f) + 4; if (n % 2 == 1) n++;
        if (base.length != n * n) base = new int[n * n];
        baseN = n; baseCx = cx; baseCz = cz; baseZoomIdx = choiceOf("zoom"); baseTime = System.currentTimeMillis();
        int half = n / 2;
        for (int y = 0; y < n; y++) for (int x = 0; x < n; x++)
            base[y * n + x] = MapStore.color(mc.level, cx + (int) Math.floor((x - half) * z), cz + (int) Math.floor((y - half) * z));
    }

    protected void render(GuiGraphicsExtractor g) {
        Minecraft mc = Minecraft.getInstance(); if (mc.player == null || mc.level == null) return;
        int s = size(); float z = zoom();
        boolean rotate = boolOf("rotate");
        double px = mc.player.getX(), pz = mc.player.getZ();
        int bcx = (int) Math.floor(px), bcz = (int) Math.floor(pz);
        long now = System.currentTimeMillis();
        boolean bg = gg.lellek.hud.LellekHud.features != null && gg.lellek.hud.LellekHud.features.inBackground();
        if (baseCx == Integer.MIN_VALUE || baseZoomIdx != choiceOf("zoom") || base.length < 4
                || Math.abs(bcx - baseCx) >= Math.max(2, z * 2) || Math.abs(bcz - baseCz) >= Math.max(2, z * 2) || now - baseTime > (bg ? 5000 : 700)) rebuildBase(mc, bcx, bcz);

        // Gedrehtes Bild pro Frame aus dem Basispuffer abtasten (flüssige Drehung, kein Zerreißen am Rand)
        if (texture == null || texSize != s) {
            if (texture != null) texture.close();
            texture = new DynamicTexture(() -> "lellek_minimap", s, s, false); texSize = s;
            location = Identifier.fromNamespaceAndPath("lellekhud", "minimap"); mc.getTextureManager().register(location, texture);
        }
        NativeImage img = texture.getPixels(); if (img == null) return;
        double yaw = rotate ? Math.toRadians(mc.player.getYRot() + 180) : 0, cos = Math.cos(yaw), sin = Math.sin(yaw);
        double offX = (px - baseCx) / z, offZ = (pz - baseCz) / z; // Spieler zwischen den Blöcken → weiches Gleiten
        int n = baseN, half = n / 2; float hs = s / 2f;
        for (int y = 0; y < s; y++) for (int x = 0; x < s; x++) {
            double dx = x + 0.5 - hs, dy = y + 0.5 - hs;
            double wx = dx * cos - dy * sin + offX, wz = dx * sin + dy * cos + offZ;
            int bx = (int) Math.floor(wx) + half, by = (int) Math.floor(wz) + half;
            int c = bx >= 0 && by >= 0 && bx < n && by < n ? base[by * n + bx] : 0xFF101010;
            img.setPixel(x, y, c);
        }
        texture.upload();

        // Rahmen + Karte
        int frame = colorOf("frame");
        g.fill(0, 0, s + 4, s + 4, frame); g.fill(1, 1, s + 3, s + 3, 0xFF000000);
        g.blit(RenderPipelines.GUI_TEXTURED, location, 2, 2, 0, 0, s, s, s, s);
        float c2 = 2 + hs;

        // Wegpunkte
        if (Mods.MINIMAP.boolOf("waypoints")) for (Waypoint w : Waypoints.here()) {
            if (!w.visible) continue;
            int[] p = project(w.x + 0.5 - px, w.z + 0.5 - pz, z, cos, sin, s, true);
            g.fill(p[0] - 2, p[1] - 2, p[0] + 3, p[1] + 3, 0xFF000000); g.fill(p[0] - 1, p[1] - 1, p[0] + 2, p[1] + 2, w.color);
        }
        // Mobs & Spieler
        boolean mobs = boolOf("mobs"), players = boolOf("players");
        if (mobs || players) {
            double r = s / 2.0 * z * 1.42;
            for (Entity e : mc.level.getEntities(mc.player, mc.player.getBoundingBox().inflate(r, 40, r), en -> en instanceof LivingEntity)) {
                boolean isPlayer = e instanceof Player; if (isPlayer ? !players : !mobs) continue;
                int[] p = project(e.getX() - px, e.getZ() - pz, z, cos, sin, s, false); if (p == null) continue;
                int col = isPlayer ? 0xFFFFFFFF : e.getType().getCategory() == MobCategory.MONSTER ? 0xFFFF4040 : 0xFFE8C840;
                double dyEnt = e.getY() - mc.player.getY(); if (Math.abs(dyEnt) > 12) col = (col & 0x00FFFFFF) | 0x90000000;
                dot(g, p[0], p[1], col, isPlayer);
            }
        }
        // Spieler-Markierung in der Mitte
        g.pose().pushMatrix(); g.pose().translate(c2, c2);
        if (!rotate) g.pose().rotate((float) Math.toRadians(mc.player.getYRot() + 180));
        g.fill(-1, -5, 2, 4, 0xFF000000); g.fill(-3, 0, 4, 3, 0xFF000000);
        g.fill(0, -4, 1, 3, 0xFFFFFFFF); g.fill(-2, 1, 3, 2, 0xFFFFFFFF); g.fill(-1, -3, 2, -2, 0xFFFF4040);
        g.pose().popMatrix();
        // Himmelsrichtungen am Rand
        String[] dirs = { "N", "O", "S", "W" }; double[][] vec = { { 0, -1 }, { 1, 0 }, { 0, 1 }, { -1, 0 } };
        for (int i = 0; i < 4; i++) {
            int[] p = project(vec[i][0] * 10000, vec[i][1] * 10000, 1, cos, sin, s, true);
            int lx = Math.max(6, Math.min(s - 2, p[0])), ly = Math.max(6, Math.min(s - 2, p[1]));
            g.fill(lx - 4, ly - 5, lx + 5, ly + 5, 0xC0000000);
            text(g, dirs[i], lx - font().width(dirs[i]) / 2 + 1, ly - 3, i == 0 ? 0xFFFF5555 : 0xFF8FB4FF);
        }
        if (boolOf("coords")) {
            String co = mc.player.getBlockX() + ", " + mc.player.getBlockY() + ", " + mc.player.getBlockZ();
            if (background) g.fill(0, s + 4, s + 4, s + 16, 0x80000000);
            text(g, co, 2 + (s - font().width(co)) / 2, s + 6, WHITE);
        }
    }
    /** Weltversatz → Pixel in der Karte; clamp = an den Rand setzen statt ausblenden */
    private static int[] project(double dx, double dz, float zoom, double cos, double sin, int s, boolean clamp) {
        double x = dx / zoom, y = dz / zoom;
        double rx = x * cos + y * sin, ry = -x * sin + y * cos; // inverse Drehung
        double hs = s / 2.0, lim = hs - 3;
        if (Math.abs(rx) > lim || Math.abs(ry) > lim) {
            if (!clamp) return null;
            double k = lim / Math.max(Math.abs(rx), Math.abs(ry)); rx *= k; ry *= k;
        }
        return new int[] { (int) Math.round(2 + hs + rx), (int) Math.round(2 + hs + ry) };
    }
    private static void dot(GuiGraphicsExtractor g, int x, int y, int col, boolean big) {
        // Raute mit dunklem Rand (wie Xaero)
        int a = col & 0xFF000000 | 0x000000; int dark = (col >>> 24) < 0xF0 ? 0x60000000 : 0xFF000000;
        g.fill(x - 1, y - 2, x + 2, y + 3, dark); g.fill(x - 2, y - 1, x + 3, y + 2, dark);
        g.fill(x, y - 1, x + 1, y + 2, col); g.fill(x - 1, y, x + 2, y + 1, col);
        if (big) { g.fill(x - 1, y - 1, x + 2, y + 2, col); }
    }
}
