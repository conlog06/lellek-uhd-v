package gg.lellek.hud.feature;

import java.util.ArrayList;
import java.util.List;

/** Ein schaltbares Feature mit Beschreibung, Kategorie und Einstellungen. */
public class Feature {
    public final String id, name, desc, category;
    public boolean enabled;
    /** Nicht null = in dieser Version (noch) nicht verfügbar, Text wird im Menü gezeigt */
    public String unavailable;
    public final List<Setting> settings = new ArrayList<Setting>();

    public Feature(String id, String name, String desc, String category, boolean enabled, Setting... settings) {
        this.id = id; this.name = name; this.desc = desc; this.category = category; this.enabled = enabled;
        for (Setting s : settings) this.settings.add(s);
    }
    public Feature notYet(String why) { unavailable = why; enabled = false; return this; }
    public Setting setting(String id) { for (Setting s : settings) if (s.id.equals(id)) return s; return null; }
    public int intOf(String id) { return ((Setting.Int) setting(id)).value; }
    public boolean boolOf(String id) { return ((Setting.Bool) setting(id)).value; }
    public int choiceOf(String id) { return ((Setting.Choice) setting(id)).index; }
    public String textOf(String id) { return ((Setting.Text) setting(id)).value; }
    public int colorOf(String id) { return ((Setting.Color) setting(id)).value; }
}
