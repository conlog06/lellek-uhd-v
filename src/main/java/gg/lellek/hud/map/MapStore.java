package gg.lellek.hud.map;

import gg.lellek.hud.LellekHud;
import gg.lellek.hud.feature.Mods;
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
 * Gespeicherte Weltkarte (wie Xaero): geladene Chunks werden eingefärbt und pro Welt/Server, Dimension und
 * <b>Ebene</b> als PNG-Regionen (512×512 Blöcke) in config/lellek-map/ abgelegt.
 * Ebenen: "surface" = Oberfläche; unter Tage und im Nether "cave_N" = Höhlenebene pro 16 Blöcke Höhe.
 */
public final class MapStore {
    public static final int UNKNOWN = 0, SOLID = 0xFF0E0D0C;
    private static final int REGION = 512, MAX_CACHED = 64;
    private static final File ROOT = FabricLoader.getInstance().getConfigDir().resolve("lellek-map").toFile();
    private static final LinkedHashMap<Long, int[]> regions = new LinkedHashMap<>(64, 0.75f, true);
    private static final Set<Long> dirty = new HashSet<>();
    private static final Map<Long, Long> scannedAt = new HashMap<>();
    private static final Set<String> saving = ConcurrentHashMap.newKeySet();
    private static String world = "", layer = "surface", key = "";
    private static int caveBand; private static boolean cave; private static int caveVotes;
    private static long lastSave; private static int ring, forcedThisTick;

    private MapStore() {}

    private static long key(int rx, int rz) { return ((long) rx << 32) ^ (rz & 0xFFFFFFFFL); }
    private static File dir() { return new File(new File(ROOT, world.replaceAll("[^a-zA-Z0-9._-]", "_")), layer); }
    public static boolean isNether() { return world.contains("nether"); }
    public static boolean caveMode() { return cave; }
    public static String layerName() { return cave ? "Höhle Y " + (caveBand * 16) + "–" + (caveBand * 16 + 15) : "Oberfläche"; }

    /** Pro Client-Tick aufrufen. */
    public static void tick(Minecraft mc) {
        forcedThisTick = 0;
        String w = mc.level == null ? "" : Waypoints.currentWorld();
        if (!w.equals(world)) { switchTo(w, "surface"); cave = false; caveVotes = 0; }
        if (mc.level == null || mc.player == null || world.isEmpty()) return;
        updateLayer(mc);
        scan(mc.level, mc.player.getBlockX() >> 4, mc.player.getBlockZ() >> 4);
        if (System.currentTimeMillis() - lastSave > 60_000) { lastSave = System.currentTimeMillis(); flush(); }
    }
    private static void switchTo(String w, String l) {
        flush(); synchronized (regions) { regions.clear(); dirty.clear(); } scannedAt.clear();
        world = w; layer = l; key = w + "/" + l; ring = 0;
    }
    /** Höhlenmodus: Nether immer; sonst wenn über dem Spieler keine Sicht zum Himmel und dicke Decke (mit Verzögerung gegen Flackern) */
    private static void updateLayer(Minecraft mc) {
        boolean auto = Mods.MINIMAP.setting("caves") == null || Mods.MINIMAP.boolOf("caves");
        BlockPos pp = mc.player.blockPosition();
        boolean want;
        if (isNether()) want = true;
        else if (!auto) want = false;
        else want = !mc.level.canSeeSky(pp.above()) && mc.level.getHeight(Heightmap.Types.MOTION_BLOCKING, pp.getX(), pp.getZ()) - pp.getY() > 6;
        if (want != cave) { if (++caveVotes > 20) { cave = want; caveVotes = 0; } } else caveVotes = 0;
        int band = Math.floorDiv(pp.getY() + 1, 16);
        String l = cave ? "cave_" + band : "surface";
        if (cave) caveBand = band;
        if (!l.equals(layer)) switchTo(world, l);
    }

    /** Geladene Chunks rund um den Spieler einfärben – wenige pro Tick, damit es nicht ruckelt. */
    private static void scan(ClientLevel level, int pcx, int pcz) {
        int budget = cave ? 3 : 6, radius = Math.min(cave ? 6 : 12, Minecraft.getInstance().options.getEffectiveRenderDistance());
        long now = System.currentTimeMillis(); int tries = 0;
        while (budget > 0 && tries++ < 600) {
            int[] off = spiral(ring++); if (Math.max(Math.abs(off[0]), Math.abs(off[1])) > radius) { ring = 0; return; }
            int cx = pcx + off[0], cz = pcz + off[1];
            Long last = scannedAt.get(key(cx, cz));
            boolean near = Math.abs(off[0]) <= 1 && Math.abs(off[1]) <= 1;
            if (last != null && now - last < (near ? 2_000 : 30_000)) continue;
            if (!level.hasChunk(cx, cz)) continue;
            scanChunk(level, cx, cz); budget--;
        }
    }
    private static int[] spiral(int n) {
        if (n == 0) return new int[] { 0, 0 };
        int r = 1; while ((2 * r + 1) * (2 * r + 1) <= n) r++;
        int i = n - (2 * r - 1) * (2 * r - 1), side = i / (2 * r), pos = i % (2 * r);
        return switch (side) { case 0 -> new int[] { -r + pos, -r }; case 1 -> new int[] { r, -r + pos }; case 2 -> new int[] { r - pos, r }; default -> new int[] { -r, r - pos }; };
    }

    // ---- Einfärben ----
    private static final int[] colH = new int[18 * 18], colC = new int[18 * 18], colW = new int[18 * 18];
    /** Ganzen Chunk berechnen: erst Höhe + Grundfarbe je Säule (mit 1 Block Rand), dann Relief-Schattierung */
    public static void scanChunk(ClientLevel level, int cx, int cz) {
        scannedAt.put(key(cx, cz), System.currentTimeMillis());
        int x0 = cx << 4, z0 = cz << 4;
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        int start = (caveBand + 1) * 16 + 2;
        for (int z = -1; z <= 16; z++) for (int x = -1; x <= 16; x++) {
            int i = (z + 1) * 18 + (x + 1), bx = x0 + x, bz = z0 + z;
            boolean edge = x < 0 || z < 0 || x > 15 || z > 15;
            if (edge && !level.hasChunk(bx >> 4, bz >> 4)) { colH[i] = Integer.MIN_VALUE; continue; }
            column(level, p, bx, bz, start, i, edge);
        }
        for (int z = 0; z < 16; z++) for (int x = 0; x < 16; x++) {
            int i = (z + 1) * 18 + (x + 1), h = colH[i], c = colC[i];
            if (h == Integer.MIN_VALUE || c == 0) continue;
            int argb;
            if (c == SOLID) argb = SOLID;
            else if (colW[i] > 0) { float k = Math.min(1f, colW[i] / 10f); argb = 0xFF000000 | scale(c, (1f - k * 0.45f) * 0.92f); }
            else {
                int hn = colH[i - 18], hw = colH[i - 1];
                if (hn == Integer.MIN_VALUE) hn = h; if (hw == Integer.MIN_VALUE) hw = h;
                float d = (h - hn) + (h - hw) * 0.6f;
                float shade = 0.94f + Math.max(-0.3f, Math.min(0.2f, d * 0.08f));
                if (cave) shade *= 1f - Math.min(0.35f, Math.max(0, start - h) / 60f); // tiefer = dunkler
                argb = 0xFF000000 | scale(c, shade);
            }
            put(x0 + x, z0 + z, argb);
        }
    }
    private static void column(ClientLevel level, BlockPos.MutableBlockPos p, int bx, int bz, int start, int i, boolean edgeOnlyHeight) {
        int h; BlockState st;
        if (cave) {
            h = Integer.MIN_VALUE; boolean open = false; int min = Math.max(level.getMinY(), start - 72);
            st = null;
            for (int y = start; y > min; y--) {
                p.set(bx, y, bz); BlockState s = level.getBlockState(p);
                if (s.isAir()) open = true; else if (open) { h = y; st = s; break; }
            }
            if (h == Integer.MIN_VALUE) { colH[i] = start; colC[i] = SOLID; colW[i] = 0; return; }
        } else {
            h = level.getHeight(Heightmap.Types.MOTION_BLOCKING, bx, bz) - 1;
            if (h < level.getMinY()) { colH[i] = Integer.MIN_VALUE; colC[i] = 0; return; }
            p.set(bx, h, bz); st = level.getBlockState(p);
        }
        colH[i] = h; colW[i] = 0;
        if (edgeOnlyHeight) { colC[i] = 1; return; }
        p.set(bx, h, bz);
        int c = st.getMapColor(level, p).col;
        if (!st.getFluidState().isEmpty()) {
            int depth = 0; BlockState below = st;
            while (depth < 16 && !below.getFluidState().isEmpty()) { p.move(0, -1, 0); below = level.getBlockState(p); depth++; }
            int floor = below.getMapColor(level, p).col;
            if (floor != 0) c = blend(c, floor, 0.25f * (1 - Math.min(1f, depth / 10f)));
            colW[i] = depth;
        }
        colC[i] = c == 0 ? 0 : c;
    }
    private static int scale(int rgb, float f) {
        int r = Math.min(255, (int) (((rgb >> 16) & 255) * f)), g = Math.min(255, (int) (((rgb >> 8) & 255) * f)), b = Math.min(255, (int) ((rgb & 255) * f));
        return (r << 16) | (g << 8) | b;
    }
    private static int blend(int a, int b, float k) {
        int r = (int) (((a >> 16) & 255) * (1 - k) + ((b >> 16) & 255) * k), g = (int) (((a >> 8) & 255) * (1 - k) + ((b >> 8) & 255) * k), bl = (int) ((a & 255) * (1 - k) + (b & 255) * k);
        return (r << 16) | (g << 8) | bl;
    }

    // ---- Lesen / Schreiben ----
    private static int[] region(int rx, int rz) {
        long k = key(rx, rz);
        synchronized (regions) {
            int[] r = regions.get(k);
            if (r != null) return r;
            r = load(rx, rz); if (r == null) r = new int[REGION * REGION];
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
        int[] r = region(rx, rz); int i = Math.floorMod(bz, REGION) * REGION + Math.floorMod(bx, REGION);
        if (r[i] != argb) { r[i] = argb; synchronized (regions) { dirty.add(key(rx, rz)); } }
    }
    /** Direkter Zugriff auf eine Region (512×512, Index z*512+x) – für schnelles Zeichnen */
    public static int[] regionArray(int rx, int rz) { return region(rx, rz); }
    /** Nur gespeicherte Farbe (0 = unbekannt) – schnell, für die Vollbild-Karte */
    public static int stored(int bx, int bz) { return region(Math.floorDiv(bx, REGION), Math.floorDiv(bz, REGION))[Math.floorMod(bz, REGION) * REGION + Math.floorMod(bx, REGION)]; }
    /** Gespeicherte Farbe; noch unbekannte, aber geladene Chunks werden sofort berechnet */
    public static int color(ClientLevel level, int bx, int bz) {
        int c = stored(bx, bz);
        if (c != 0) return c;
        if (level != null && forcedThisTick < 6 && level.hasChunk(bx >> 4, bz >> 4)) {
            Long last = scannedAt.get(key(bx >> 4, bz >> 4));
            if (last == null || System.currentTimeMillis() - last > 2000) { forcedThisTick++; scanChunk(level, bx >> 4, bz >> 4); c = stored(bx, bz); }
        }
        return c == 0 ? 0xFF101010 : c;
    }

    private static File file(int rx, int rz) { return new File(dir(), "r." + rx + "." + rz + ".png"); }
    private static int[] load(int rx, int rz) {
        if (world.isEmpty()) return null;
        File f = file(rx, rz);
        if (!f.exists() && layer.equals("surface")) { File old = new File(f.getParentFile().getParentFile(), f.getName()); if (old.exists()) f = old; } // Karten aus 1.3.2 übernehmen
        if (!f.exists()) return null;
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
