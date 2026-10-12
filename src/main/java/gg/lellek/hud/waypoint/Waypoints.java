package gg.lellek.hud.waypoint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Alle Wegpunkte in config/lellek-waypoints.json, gruppiert nach Welt + Dimension. */
public class Waypoints {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE = FabricLoader.getInstance().getConfigDir().resolve("lellek-waypoints.json").toFile();
    public static final List<Waypoint> ALL = new ArrayList<>();

    public static void init() {
        try { if (FILE.exists()) { List<Waypoint> l = GSON.fromJson(new InputStreamReader(new FileInputStream(FILE), StandardCharsets.UTF_8), new TypeToken<List<Waypoint>>() {}.getType()); if (l != null) ALL.addAll(l); } } catch (Exception ignored) {}
    }
    public static void save() { try (Writer w = new OutputStreamWriter(new FileOutputStream(FILE), StandardCharsets.UTF_8)) { GSON.toJson(ALL, w); } catch (IOException ignored) {} }

    public static String currentWorld() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return "";
        String base = mc.isLocalServer() && mc.getSingleplayerServer() != null ? "sp:" + mc.getSingleplayerServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).getParent().getFileName()
                : mc.getCurrentServer() != null ? "mp:" + mc.getCurrentServer().ip : "unknown";
        return base + "#" + mc.level.dimension().identifier();
    }
    public static List<Waypoint> here() { String w = currentWorld(); List<Waypoint> out = new ArrayList<>(); for (Waypoint p : ALL) if (w.equals(p.world)) out.add(p); return out; }
    public static void add(Waypoint p) { ALL.add(p); save(); }
    public static void remove(Waypoint p) { ALL.remove(p); save(); }
}
