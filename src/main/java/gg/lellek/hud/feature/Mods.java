package gg.lellek.hud.feature;

import java.util.ArrayList;
import java.util.List;

/** Alle Features des LellekClient. Neue: hier anlegen, Logik in Features.java. */
public class Mods {
    public static final List<Feature> ALL = new ArrayList<Feature>();
    public static final String[] CATEGORIES = { "Spiel", "Welt", "Karte", "HUD", "Chat", "Social" };

    public static final Feature ZOOM = add(new Feature("zoom", "Zoom", "Taste C halten, Mausrad zoomt stufenlos näher ran (bis 50x)", "Spiel", true,
            new Setting.Int("factor", "Start-Zoom", 4, 2, 50, "x"), new Setting.Bool("animate", "Weiches Zoomen", true), new Setting.Int("speed", "Zoom-Tempo", 5, 1, 10, ""),
            new Setting.Bool("scroll", "Mausrad-Zoom", true), new Setting.Int("max", "Maximaler Zoom", 50, 10, 100, "x"), new Setting.Bool("remember", "Mausrad-Stufe merken", false),
            new Setting.Bool("sens", "Maus an Zoom anpassen", true), new Setting.Bool("smooth", "Kino-Kamera", false), new Setting.Bool("indicator", "Zoom-Stufe anzeigen", true)));
    public static final Feature TOGGLE_SPRINT = add(new Feature("togglesprint", "ToggleSprint", "Beim Vorwärtslaufen automatisch sprinten", "Spiel", false));
    public static final Feature TOGGLE_SNEAK = add(new Feature("togglesneak", "ToggleSneak", "Taste V schaltet Schleichen dauerhaft an/aus", "Spiel", false));
    public static final Feature FULLBRIGHT = add(new Feature("fullbright", "Fullbright", "Alles hell, auch Höhlen und Nacht", "Spiel", false));
    public static final Feature HITBOXES = add(new Feature("hitboxes", "Hitboxen", "Trefferboxen aller Entities anzeigen", "Spiel", false));
    public static final Feature NO_HURT_CAM = add(new Feature("nohurtcam", "Kein Schadens-Wackeln", "Kamera wackelt bei Schaden nicht mehr", "Spiel", false));
    public static final Feature TIME = add(new Feature("time", "Tageszeit", "Feste Tageszeit, nur für dich sichtbar", "Welt", false,
            new Setting.Choice("time", "Zeit", 0, "Tag", "Sonnenuntergang", "Nacht", "Sonnenaufgang")));
    public static final Feature WEATHER = add(new Feature("weather", "Immer klares Wetter", "Regen und Gewitter ausblenden", "Welt", false));
    public static final Feature CROSSHAIR = add(new Feature("crosshair", "Eigenes Fadenkreuz", "Form, Farbe und Größe frei wählen", "HUD", false,
            new Setting.Choice("style", "Form", 0, "Kreuz", "Punkt", "Kreis", "Plus mit Lücke"), new Setting.Int("size", "Größe", 5, 2, 12, "px"),
            new Setting.Int("thickness", "Dicke", 1, 1, 3, "px"), new Setting.Color("color", "Farbe", 0xFFFFB400),
            new Setting.Bool("target", "Bei Gegner färben", true), new Setting.Color("targetcolor", "Gegner-Farbe", 0xFFFF5F5F), new Setting.Bool("dynamic", "Dynamisch beim Laufen", false)));
    public static final Feature SHADOW = add(new Feature("shadow", "Textschatten", "HUD-Texte mit Schatten zeichnen", "HUD", true));
    public static final Feature MENU = add(new Feature("menu", "Lellek-Hauptmenü", "Hauptmenü mit Logo und Gitterboden", "HUD", true));
    public static final Feature CHAT_TIME = add(new Feature("chattime", "Chat-Zeitstempel", "Uhrzeit vor jeder Chatnachricht", "Chat", false,
            new Setting.Bool("seconds", "Mit Sekunden", false)));

    public static final Feature WAYPOINTS = add(new Feature("waypoints", "Wegpunkte", "Taste N setzt einen Wegpunkt, M öffnet die Karte. Schild + Lichtstrahl in der Welt", "Karte", true,
            new Setting.Bool("beam", "Lichtstrahl", true), new Setting.Int("maxdist", "Max. Entfernung (0 = alle)", 0, 0, 5000, "m")));
    public static final Feature MINIMAP = add(new Feature("minimap", "Minimap", "Einstellungen der Minimap – das Element selbst unter HUD-Elemente", "Karte", true,
            new Setting.Int("zoom", "Blöcke pro Pixel", 1, 1, 4, ""), new Setting.Bool("waypoints", "Wegpunkte anzeigen", true)));
    public static final Feature PERSPECTIVE = add(new Feature("perspective", "Perspektive (Freelook)", "F6 halten: Kamera dreht 360° um dich, dein Körper bleibt", "Spiel", true));
    public static final Feature DAMAGE = add(new Feature("damage", "Schadensanzeige", "Schwebende Zahlen bei Schaden und Heilung an Entities", "Spiel", false));
    public static final Feature BLOCK_OVERLAY = add(new Feature("blockoverlay", "Block-Umrandung", "Farbe und Dicke des Block-Auswahlrahmens", "HUD", false,
            new Setting.Color("color", "Farbe", 0xFFFFB400), new Setting.Int("thickness", "Dicke", 2, 1, 5, "px"), new Setting.Bool("fill", "Flächen füllen", false)));
    public static final Feature AUTO_GG = add(new Feature("autogg", "Auto-GG", "Schreibt nach Spielende automatisch gg in den Chat", "Chat", false,
            new Setting.Choice("trigger", "Auslöser", 0, "Sieg-Meldungen", "Game-Over-Meldungen"), new Setting.Choice("text", "Text", 0, "gg", "GG!", "gg wp"), new Setting.Int("delay", "Verzögerung", 1, 0, 5, "s")));
    public static final Feature SCREENSHOT = add(new Feature("screenshot", "Screenshot in Zwischenablage", "F2 kopiert das Bild zusätzlich in die Zwischenablage", "Spiel", true));

    public static final Feature COSMETIC_SYNC = add(new Feature("cosmeticsync", "Cosmetics-Sync", "Andere LellekClient-Spieler sehen dein Cosmetic und du ihres – über den Cosmetics-Server", "Social", true,
            new Setting.Text("server", "Server-URL", "http://127.0.0.1:8765")));

    public static final Feature VOICE = add(new Feature("voice", "Voice-Chat", "Proximity-Voice mit Push-to-Talk (Taste B) über den Voice-Server – in jeder Version, ohne Server-Mod", "Social", false,
            new Setting.Text("server", "Server-URL", "http://127.0.0.1:8766"), new Setting.Int("volume", "Lautstärke", 100, 0, 200, "%"), new Setting.Int("micgain", "Mikrofon", 100, 20, 300, "%")));

    public static final Feature CHAT_MENTION = add(new Feature("chatmention", "Chat-Erwähnung", "Ton und Markierung, wenn dein Name im Chat auftaucht", "Chat", true, new Setting.Color("color", "Markierungsfarbe", 0xFFFFB400)));
    public static final Feature AUTO_RESPAWN = add(new Feature("autorespawn", "Auto-Respawn", "Nach dem Tod automatisch neu spawnen", "Spiel", false, new Setting.Int("delay", "Verzögerung", 2, 0, 10, "s")));
    public static final Feature DURABILITY = add(new Feature("durability", "Haltbarkeits-Warnung", "Warnt, wenn Rüstung oder Werkzeug fast kaputt ist", "Spiel", true, new Setting.Int("percent", "Warnen unter", 10, 1, 50, "%")));

    private static Feature add(Feature f) { ALL.add(f); return f; }
    public static Feature byId(String id) { for (Feature f : ALL) if (f.id.equals(id)) return f; return null; }
}
