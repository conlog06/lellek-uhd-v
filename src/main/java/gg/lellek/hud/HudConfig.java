package gg.lellek.hud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import gg.lellek.hud.element.*;
import gg.lellek.hud.feature.Feature;
import gg.lellek.hud.feature.Mods;
import gg.lellek.hud.feature.Setting;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** JSON-Config in config/lellekhud.json – Elemente, Features mit Einstellungen, Cosmetic. */
public class HudConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("lellekhud.json");

    public final transient List<HudElement> elements = new ArrayList<>();
    public String cosmetic = "";
    public JsonObject elementData = new JsonObject(), featureData = new JsonObject();

    public HudConfig() {
        elements.add(new FpsElement()); elements.add(new CpsElement()); elements.add(new CoordsElement());
        elements.add(new PingElement()); elements.add(new ClockElement()); elements.add(new KeystrokesElement());
        elements.add(new SpotifyElement()); elements.add(new ArmorElement()); elements.add(new PotionsElement());
        elements.add(new ReachElement()); elements.add(new ComboElement()); elements.add(new ServerElement()); elements.add(new MemoryElement());
        elements.add(new MinimapElement()); elements.add(new DirectionElement()); elements.add(new SprintElement()); elements.add(new ItemCountElement()); elements.add(new VoiceElement());
        elements.add(new CoordXElement()); elements.add(new CoordYElement()); elements.add(new CoordZElement());
        elements.add(new MouseKeysElement()); elements.add(new HealthElement()); elements.add(new ArmorValueElement()); elements.add(new BiomeElement()); elements.add(new LightElement());
        elements.add(new HeldItemElement()); elements.add(new TargetDistanceElement()); elements.add(new SpeedElement()); elements.add(new TpsElement()); elements.add(new StopwatchElement()); elements.add(new SessionElement());
    }

    public static HudConfig load() {
        HudConfig c = new HudConfig();
        try (Reader r = new InputStreamReader(new FileInputStream(FILE.toFile()), StandardCharsets.UTF_8)) {
            HudConfig l = GSON.fromJson(r, HudConfig.class);
            if (l != null) { c.cosmetic = l.cosmetic == null ? "" : l.cosmetic; if (l.elementData != null) c.elementData = l.elementData; if (l.featureData != null) c.featureData = l.featureData; }
        } catch (Exception ignored) {}
        for (HudElement el : c.elements) {
            if (!c.elementData.has(el.id)) continue;
            JsonObject j = c.elementData.getAsJsonObject(el.id);
            if (j.has("enabled")) el.enabled = j.get("enabled").getAsBoolean();
            if (j.has("ax") && j.has("ay")) { el.ax = j.get("ax").getAsFloat(); el.ay = j.get("ay").getAsFloat(); el.ox = j.has("ox") ? j.get("ox").getAsInt() : 0; el.oy = j.has("oy") ? j.get("oy").getAsInt() : 0; }
            else if (j.has("x") && j.has("y")) el.setLegacyPosition(j.get("x").getAsInt(), j.get("y").getAsInt());
            if (j.has("color")) el.color = (int) Long.parseLong(j.get("color").getAsString(), 16);
            if (j.has("scale")) el.scale = j.get("scale").getAsInt();
            if (j.has("background")) el.background = j.get("background").getAsBoolean();
            for (Setting s : el.settings) if (j.has("s_" + s.id)) s.set(j.get("s_" + s.id).getAsString());
        }
        for (Feature f : Mods.ALL) {
            if (!c.featureData.has(f.id)) continue;
            JsonObject j = c.featureData.getAsJsonObject(f.id);
            if (j.has("enabled")) f.enabled = j.get("enabled").getAsBoolean();
            for (Setting s : f.settings) if (j.has(s.id)) s.set(j.get(s.id).getAsString());
        }
        return c;
    }

    public void save() {
        for (HudElement el : elements) {
            JsonObject j = new JsonObject();
            j.addProperty("enabled", el.enabled);
            if (el.hasCustomAnchor()) { j.addProperty("ax", el.ax); j.addProperty("ay", el.ay); j.addProperty("ox", el.ox); j.addProperty("oy", el.oy); }
            else if (el.hasLegacyPosition()) { j.addProperty("x", el.legacyX()); j.addProperty("y", el.legacyY()); }
            j.addProperty("color", String.format("%08X", el.color)); j.addProperty("scale", el.scale); j.addProperty("background", el.background);
            for (Setting s : el.settings) j.addProperty("s_" + s.id, s.get());
            elementData.add(el.id, j);
        }
        for (Feature f : Mods.ALL) {
            JsonObject j = new JsonObject();
            j.addProperty("enabled", f.enabled);
            for (Setting s : f.settings) j.addProperty(s.id, s.get());
            featureData.add(f.id, j);
        }
        try (Writer w = new OutputStreamWriter(new FileOutputStream(FILE.toFile()), StandardCharsets.UTF_8)) { GSON.toJson(this, w); } catch (IOException ignored) {}
    }

    public void reset() { for (HudElement el : elements) el.resetPosition(); save(); }
}
