package gg.lellek.hud.map;

import gg.lellek.hud.LellekHud;
import gg.lellek.hud.waypoint.Waypoints;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gespeicherte Weltkarte: Jeder geladene Chunk wird eingefärbt und pro Welt + Dimension (Overworld, Nether, End)
 * als PNG-Regionen (512×512 Blöcke) in config/lellek-map/ abgelegt. Minimap und Vollbild-Karte lesen daraus –
 * schon besuchte Gebiete bleiben sichtbar, auch wenn die Chunks längst entladen sind.
 */
public final class MapStore {
    private static final int REGION = 512, MAX_CACHED = 48;
    private static final File ROOT = FabricLoader.getInstance().getConfigDir().resolve("lellek-map").toFile();
    /** Regionen im Speicher, LRU; Wert 0 = unbekannt */
    private static final LinkedHashMap<Long, int[]> regions = new LinkedHashMap<>(64, 0.75f, true);
    private static final Set<Long> dirty = new HashSet<>();
    private static final Map<Long, Long> scannedAt = new HashMap<>();
    private static final Set<String> saving = ConcurrentHashMap.newKeySet();
    private static String world = "";
    private static long lastSave;
    private static int ring; // Fortschritt der Spiral-Abtastung

    private MapStore() {}

    private static long key(int rx, int rz) { return ((long) rx << 32) ^ (rz & 0xFFFFFFFFL); }
    private static File dir() { return new File(ROOT, world.replaceAll("[^a-zA-Z0-9._-]", "_")); }
    public static boolean isNether() { return world.contains("nether"); }

    /** Pro Client-Tick aufrufen. */
    public static void tick(Minecraft mc) {
        String w = mc.level == null ? "" : Waypoints.currentWorld();
        if (!w.equals(world)) { flush(); synchronized (regions) { regions.clear(); dirty.clear(); } scannedAt.clear(); world = w; ring = 0; }
        if (mc.level == null || mc.player == null || world.isEmpty()) return;
        scan(mc.level, mc.player.getBlockX() >> 4, mc.player.getBlockZ() >> 4);
        if (System.currentTimeMillis() - lastSave > 60_000) { lastSave = System.currentTimeMillis(); flush(); }
    }

    /** Geladene Chunks rund um den Spieler einfärben – wenige pro Tick, damit es nicht ruckelt. */
    private static void scan(ClientLevel level, int pcx, int pcz) {
        int budget = isNether() ? 2 : 4, radius = Math.min(12, Minecraft.getInstance().options.getEffectiveRenderDistance());
        long now = System.currentTimeMillis();
        int tries = 0;
        while (budget > 0 && tries++ < 400) {
            // Spirale: Ringe von innen nach außen, dann wieder von vorn
            int[] off = spiral(ring++); if (Math.max(Math.abs(off[0]), Math.abs(off[1])) > radius) { ring = 0; return; }
            int cx = pcx + off[0], cz = pcz + off[1];
            long ck = key(cx, cz); Long last = scannedAt.get(ck);
            boolean near = Math.abs(off[0]) <= 1 && Math.abs(off[1]) <= 1;
            if (last != null && now - last < (near ? 3_000 : 30_000)) continue;
            if (!level.hasChunk(cx, cz)) continue;
            scannedAt.put(ck, now); budget--;
            for (int z = 0; z < 16; z++) for (int x = 0; x < 16; x++) {
                int bx = (cx << 4) + x, bz = (cz << 4) + z, c = compute(level, bx, bz);
                if (c != 0) put(bx, bz, c);
            }
        }
    }
    private static int[] spiral(int n) {
        if (n == 0) return new int[] { 0, 0 };
        int r = 1; while ((2 * r + 1) * (2 * r + 1) <= n) r++;
        int i = n - (2 * r - 1) * (2 * r - 1), side = i / (2 * r), pos = i % (2 * r);
        return switch (side) { case 0 -> new int[] { -r + pos, -r }; case 1 -> new int[] { r, -r + pos }; case 2 -> new int[] { r - pos, r }; default -> new int[] { -r, r - pos }; };
    }

    private static int[] region(int rx, int rz, boolean create) {
        long k = key(rx, rz);
        synchronized (regions) {
            int[] r = regions.get(k);
            if (r != null) return r;
            r = load(rx, rz);
            if (r == null) r = new int[REGION * REGION];
            regions.put(k, r);
            if (regions.size() > MAX_CACHED) {
                Iterator<Map.Entry<Long, int[]>> it = regions.entrySet().iterator();
                while (regions.size() > MAX_CACHED && it.hasNext()) { Map.Entry<Long, int[]> e = it.next(); if (dirty.contains(e.getKey())) continue; it.remove(); }
            }
            return r;
        }
    }
    private static void put(int bx, int bz, int argb) {
        int rx = Math.floorDiv(bx, REGION), rz = Math.floorDiv(bz, REGION);
        int[] r = region(rx, rz, true);
        int i = Math.floorMod(bz, REGION) * REGION + Math.floorMod(bx, REGION);
        if (r[i] != argb) { r[i] = argb; synchronized (regions) { dirty.add(key(rx, rz)); } }
    }
    /** Gespeicherte Farbe (0 = noch nie gesehen). Geladene Chunks werden live berechnet. */
    public static int color(ClientLevel level, int bx, int bz) {
        int[] r = region(Math.floorDiv(bx, REGION), Math.floorDiv(bz, REGION), false);
        int c = r[Math.floorMod(bz, REGION) * REGION + Math.floorMod(bx, REGION)];
        if (c != 0) return c;
        if (level != null && level.hasChunk(bx >> 4, bz >> 4)) { c = compute(level, bx, bz); if (c != 0) put(bx, bz, c); return c == 0 ? 0xFF101010 : c; }
        return 0xFF101010;
    }

    /** Farbe einer Säule: Oberfläche (Overworld/End) bzw. erster Boden unter der Decke (Nether). */
    public static int compute(ClientLevel level, int bx, int bz) {
        int h;
        if (isNether()) {
            h = Integer.MIN_VALUE; boolean air = false; BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
            for (int y = 120; y > level.getMinY(); y--) {
                p.set(bx, y, bz); BlockState st = level.getBlockState(p);
                if (st.isAir()) air = true; else if (air) { h = y; break; }
            }
            if (h == Integer.MIN_VALUE) return 0;
        } else {
            h = level.getHeight(Heightmap.Types.MOTION_BLOCKING, bx, bz) - 1;
            if (h < level.getMinY()) return 0;
        }
        BlockPos top = new BlockPos(bx, h, bz); BlockState st = level.getBlockState(top);
        int c = st.getMapColor(level, top).col; if (c == 0) return 0;
        // Wasser: Farbe nach Tiefe abdunkeln, Grund leicht durchscheinen lassen (wie Xaero)
        if (!st.getFluidState().isEmpty()) {
            int depth = 0; BlockPos.MutableBlockPos q = top.mutable(); BlockState below = st;
            while (depth < 16 && !below.getFluidState().isEmpty()) { q.move(0, -1, 0); below = level.getBlockState(q); depth++; }
            int floor = below.getMapColor(level, q).col;
            float k = Math.min(1f, depth / 10f), dark = 1f - k * 0.45f;
            int mix = floor == 0 ? c : blend(c, floor, 0.25f * (1 - k));
            return 0xFF000000 | scale(mix, dark * 0.92f);
        }
        // Relief: Höhenunterschied zu Nord- und West-Nachbar
        int hn, hw;
        if (isNether()) { hn = level.getBlockState(top.north()).isAir() ? h - 1 : !level.getBlockState(top.north().above()).isAir() ? h + 1 : h; hw = level.getBlockState(top.west()).isAir() ? h - 1 : !level.getBlockState(top.west().above()).isAir() ? h + 1 : h; }
        else { hn = level.getHeight(Heightmap.Types.MOTION_BLOCKING, bx, bz - 1) - 1; hw = level.getHeight(Heightmap.Types.MOTION_BLOCKING, bx - 1, bz) - 1; }
        float d = (h - hn) + (h - hw) * 0.6f;
        float shade = 0.94f + Math.max(-0.28f, Math.min(0.2f, d * 0.07f));
        return 0xFF000000 | scale(c, shade);
    }
    private static int scale(int rgb, float f) {
        int r = Math.min(255, (int) (((rgb >> 16) & 255) * f)), g = Math.min(255, (int) (((rgb >> 8) & 255) * f)), b = Math.min(255, (int) ((rgb & 255) * f));
        return (r << 16) | (g << 8) | b;
    }
    private static int blend(int a, int b, float k) {
        int r = (int) (((a >> 16) & 255) * (1 - k) + ((b >> 16) & 255) * k), g = (int) (((a >> 8) & 255) * (1 - k) + ((b >> 8) & 255) * k), bl = (int) ((a & 255) * (1 - k) + (b & 255) * k);
        return (r << 16) | (g << 8) | bl;
    }

    // ---- Speichern / Laden ----
    private static File file(int rx, int rz) { return new File(dir(), "r." + rx + "." + rz + ".png"); }
    private static int[] load(int rx, int rz) {
        File f = file(rx, rz); if (!f.exists()) return null;
        try { BufferedImage img = ImageIO.read(f); if (img == null) return null; int[] a = new int[REGION * REGION]; img.getRGB(0, 0, REGION, REGION, a, 0, REGION); return a; }
        catch (Exception e) { LellekHud.log("Karte: Region " + f.getName() + " nicht lesbar: " + e); return null; }
    }
    /** Geänderte Regionen im Hintergrund schreiben */
    public static void flush() {
        List<Object[]> jobs = new ArrayList<>();
        synchronized (regions) {
            for (Long k : dirty) { int[] r = regions.get(k); if (r != null) jobs.add(new Object[] { (int) (k >> 32), (int) (long) k, r.clone() }); }
            dirty.clear();
        }
        if (jobs.isEmpty() || world.isEmpty()) return;
        File d = dir();
        Thread t = new Thread(() -> {
            d.mkdirs();
            for (Object[] j : jobs) {
                File f = new File(d, "r." + j[0] + "." + j[1] + ".png");
                if (!saving.add(f.getPath())) continue;
                try { BufferedImage img = new BufferedImage(REGION, REGION, BufferedImage.TYPE_INT_ARGB); img.setRGB(0, 0, REGION, REGION, (int[]) j[2], 0, REGION); File tmp = new File(d, f.getName() + ".tmp"); ImageIO.write(img, "png", tmp); if (f.exists()) f.delete(); tmp.renameTo(f); }
                catch (Exception e) { LellekHud.log("Karte speichern: " + e); }
                finally { saving.remove(f.getPath()); }
            }
        }, "LellekHUD-MapSave");
        t.setDaemon(false); t.start();
    }
}
