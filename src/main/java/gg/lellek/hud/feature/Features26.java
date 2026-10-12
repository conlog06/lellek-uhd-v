package gg.lellek.hud.feature;

import gg.lellek.hud.LellekHud;
import gg.lellek.hud.waypoint.WorldLines;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

/** Die Features, die in 26.2 einen anderen Weg brauchen als in 1.8.9/1.12.2. */
public class Features26 {
    private static boolean hitboxWanted; private static Method hitboxSetter; private static Object hitboxTarget; private static boolean hitboxSearched, hitboxState;
    private static Method timeSetter; private static Object timeTarget; private static boolean timeSearched;
    private static long lastShot; private static File shotDir;

    public static void install() {
        // Block-Umrandung: Vanilla-Umriss abschalten und eigenen zeichnen
        try {
            LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register((ctx, hit) -> !Mods.BLOCK_OVERLAY.enabled);
            LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(ctx -> {
                if (!Mods.BLOCK_OVERLAY.enabled) return;
                Minecraft mc = Minecraft.getInstance();
                if (mc.player == null || !(mc.hitResult instanceof BlockHitResult bh) || mc.hitResult.getType() != HitResult.Type.BLOCK) return;
                BlockPos pos = bh.getBlockPos(); if (mc.level.getBlockState(pos).isAir()) return;
                AABB bb = mc.level.getBlockState(pos).getShape(mc.level, pos).bounds().move(pos).inflate(0.003);
                Vec3 cam = ctx.levelState().cameraRenderState.pos;
                WorldLines.box(ctx.poseStack(), ctx.submitNodeCollector(), bb.minX - cam.x, bb.minY - cam.y, bb.minZ - cam.z, bb.maxX - cam.x, bb.maxY - cam.y, bb.maxZ - cam.z, Mods.BLOCK_OVERLAY.colorOf("color"));
            });
        } catch (Throwable t) { LellekHud.log("Block-Umrandung nicht verfügbar: " + t); }
    }

    public static void tick(Minecraft mc) {
        if (mc.player == null) return;
        hitboxes(mc); time(mc); screenshots(mc);
    }

    // ---- Hitboxen: Setter im EntityRenderDispatcher per Reflection (Name variiert zwischen Versionen) ----
    private static void hitboxes(Minecraft mc) {
        if (Mods.HITBOXES.enabled == hitboxState) return;
        if (!hitboxSearched) {
            hitboxSearched = true;
            Object d = mc.getEntityRenderDispatcher();
            for (Method m : d.getClass().getMethods()) if (m.getName().toLowerCase().contains("hitbox") && m.getParameterCount() == 1 && m.getParameterTypes()[0] == boolean.class) { hitboxSetter = m; hitboxTarget = d; break; }
            if (hitboxSetter == null) LellekHud.log("Hitboxen: kein Setter gefunden – nutze F3+B");
        }
        if (hitboxSetter == null) { hitboxState = Mods.HITBOXES.enabled; return; }
        try { hitboxSetter.invoke(hitboxTarget, Mods.HITBOXES.enabled); hitboxState = Mods.HITBOXES.enabled; } catch (Exception e) { LellekHud.log("Hitboxen: " + e); hitboxSetter = null; }
    }

    // ---- Tageszeit: setDayTime auf den Level-Daten oder dem Level ----
    private static final long[] TIMES = { 6000, 12500, 18000, 23500 };
    private static void time(Minecraft mc) {
        if (!Mods.TIME.enabled || mc.level == null) return;
        if (!timeSearched) {
            timeSearched = true;
            for (Object target : new Object[] { mc.level.getLevelData(), mc.level }) {
                for (Method m : target.getClass().getMethods()) if ((m.getName().equals("setDayTime") || m.getName().equals("setDayTimeFromServer")) && m.getParameterCount() == 1 && m.getParameterTypes()[0] == long.class) { timeSetter = m; timeTarget = target; break; }
                if (timeSetter != null) break;
            }
            if (timeSetter == null) LellekHud.log("Tageszeit: keine passende Methode gefunden");
        }
        if (timeSetter == null) return;
        try { timeSetter.invoke(timeTarget, TIMES[Mods.TIME.choiceOf("time")]); } catch (Exception e) { LellekHud.log("Tageszeit: " + e); timeSetter = null; }
    }

    // ---- Screenshot in Zwischenablage: neue Datei im screenshots-Ordner erkennen ----
    private static void screenshots(Minecraft mc) {
        if (!Mods.SCREENSHOT.enabled) return;
        if (shotDir == null) shotDir = new File(mc.gameDirectory, "screenshots");
        if (!shotDir.exists()) return;
        long mod = shotDir.lastModified(); if (mod <= lastShot) return;
        if (lastShot == 0) { lastShot = mod; return; } // beim Start nichts nachholen
        lastShot = mod;
        File newest = null; for (File f : shotDir.listFiles()) if (f.getName().endsWith(".png") && (newest == null || f.lastModified() > newest.lastModified())) newest = f;
        if (newest == null || System.currentTimeMillis() - newest.lastModified() > 5000) return;
        final File file = newest;
        new Thread(() -> { try { Thread.sleep(300); final BufferedImage img = javax.imageio.ImageIO.read(file); if (img == null) return;
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new Transferable() {
                public DataFlavor[] getTransferDataFlavors() { return new DataFlavor[] { DataFlavor.imageFlavor }; }
                public boolean isDataFlavorSupported(DataFlavor f) { return DataFlavor.imageFlavor.equals(f); }
                public Object getTransferData(DataFlavor f) throws UnsupportedFlavorException, IOException { if (!isDataFlavorSupported(f)) throw new UnsupportedFlavorException(f); return img; }
            }, null); } catch (Exception e) { LellekHud.log("Screenshot-Kopie: " + e); } }, "LellekHUD-Screenshot").start();
    }
}
