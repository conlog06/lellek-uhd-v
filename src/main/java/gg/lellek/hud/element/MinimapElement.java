package gg.lellek.hud.element;

import com.mojang.blaze3d.platform.NativeImage;
import gg.lellek.hud.feature.Mods;
import gg.lellek.hud.waypoint.Waypoint;
import gg.lellek.hud.waypoint.Waypoints;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/** Eigene Minimap: Oberfläche um den Spieler, Norden oben, Spielerpfeil, Wegpunkte. */
public class MinimapElement extends HudElement {
    private static final int SIZE = 96, TEX = 128;
    private DynamicTexture texture; private Identifier location;
    private int lastX = Integer.MIN_VALUE, lastZ = Integer.MIN_VALUE, lastZoom = -1; private long lastUpdate;

    public MinimapElement() { super("minimap", "Minimap", TR, 4, 4); }
    public int getWidth() { return SIZE + 4; }
    public int getHeight() { return SIZE + 4 + 10; }
    private int zoom() { return Math.max(1, Math.min(4, Mods.MINIMAP.intOf("zoom"))); }

    private void update(Minecraft mc) {
        if (texture == null) { texture = new DynamicTexture(() -> "lellek_minimap", TEX, TEX, false); location = Identifier.fromNamespaceAndPath("lellekhud", "minimap"); mc.getTextureManager().register(location, texture); }
        int px = mc.player.getBlockX(), pz = mc.player.getBlockZ(), zoom = zoom(); long now = System.currentTimeMillis();
        if (px == lastX && pz == lastZ && zoom == lastZoom && now - lastUpdate < 2000) return;
        // Neu berechnen kostet tausende Block-Abfragen: höchstens 4x pro Sekunde, im Hintergrund nur alle 5 s
        if (now - lastUpdate < (gg.lellek.hud.LellekHud.features != null && gg.lellek.hud.LellekHud.features.inBackground() ? 5000 : 250)) return;
        lastX = px; lastZ = pz; lastZoom = zoom; lastUpdate = now;
        NativeImage img = texture.getPixels(); if (img == null) return;
        for (int py = 0; py < TEX; py++) for (int pxl = 0; pxl < TEX; pxl++) img.setPixel(pxl, py, colorAt(mc.level, px + (pxl - TEX / 2) * zoom, pz + (py - TEX / 2) * zoom));
        texture.upload();
    }
    private static int toAbgr(int argb) { return (argb & 0xFF00FF00) | ((argb >> 16) & 0xFF) | ((argb & 0xFF) << 16); }
    public static int colorAt(ClientLevel level, int bx, int bz) {
        if (!level.hasChunkAt(new BlockPos(bx, 64, bz))) return 0xFF101010;
        int h = level.getHeight(Heightmap.Types.MOTION_BLOCKING, bx, bz) - 1; if (h < level.getMinY()) return 0xFF101010;
        BlockPos top = new BlockPos(bx, h, bz); BlockState st = level.getBlockState(top);
        int c = st.getMapColor(level, top).col; if (c == 0) return 0xFF101010;
        int north = level.getHeight(Heightmap.Types.MOTION_BLOCKING, bx, bz - 1) - 1;
        float shade = h > north ? 1.12f : h < north ? 0.82f : 1f;
        int r = Math.min(255, (int) (((c >> 16) & 255) * shade)), g = Math.min(255, (int) (((c >> 8) & 255) * shade)), b = Math.min(255, (int) ((c & 255) * shade));
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
    protected void render(GuiGraphicsExtractor g) {
        Minecraft mc = Minecraft.getInstance(); if (mc.player == null || mc.level == null) return;
        update(mc);
        bg(g, SIZE + 4, SIZE + 4 + 10);
        g.fill(1, 1, SIZE + 3, SIZE + 3, color);
        g.blit(RenderPipelines.GUI_TEXTURED, location, 2, 2, 16, 16, SIZE, SIZE, TEX, TEX);
        int zoom = zoom();
        if (Mods.MINIMAP.boolOf("waypoints")) for (Waypoint w : Waypoints.here()) {
            if (!w.visible) continue;
            int wx = Math.max(3, Math.min(SIZE - 1, 2 + SIZE / 2 + (int) ((w.x + 0.5 - mc.player.getX()) / zoom))), wz = Math.max(3, Math.min(SIZE - 1, 2 + SIZE / 2 + (int) ((w.z + 0.5 - mc.player.getZ()) / zoom)));
            g.fill(wx - 2, wz - 2, wx + 2, wz + 2, w.color);
        }
        g.pose().pushMatrix(); g.pose().translate(2 + SIZE / 2f, 2 + SIZE / 2f); g.pose().rotate((float) Math.toRadians(mc.player.getYRot() + 180));
        g.fill(-1, -4, 1, 3, 0xFFFFFFFF); g.fill(-3, 1, 3, 3, 0xFFFFFFFF);
        g.pose().popMatrix();
        text(g, "N", 2 + SIZE / 2 - 2, 3, color);
        String coords = mc.player.getBlockX() + " " + mc.player.getBlockY() + " " + mc.player.getBlockZ();
        text(g, coords, 2 + (SIZE - font().width(coords)) / 2, SIZE + 5, WHITE);
    }
}
