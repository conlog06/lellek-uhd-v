package gg.lellek.hud.waypoint;

/** Ein Wegpunkt: Name, Position, Farbe (ARGB), Welt-Schlüssel, sichtbar. */
public class Waypoint {
    public String name, world;
    public int x, y, z, color = 0xFFFFB400;
    public boolean visible = true;
    public Waypoint() {}
    public Waypoint(String name, String world, int x, int y, int z, int color) { this.name = name; this.world = world; this.x = x; this.y = y; this.z = z; this.color = color; }
}
