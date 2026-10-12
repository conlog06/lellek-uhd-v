package gg.lellek.hud.emote;

/** Ein Emote: Name, Preis, Dauer (0 = Schleife) und die Pose als Funktion der Zeit. */
public abstract class Emote {
    public final String id, name; public final int price; public final float duration;
    protected Emote(String id, String name, int price, float duration) { this.id = id; this.name = name; this.price = price; this.duration = duration; }
    /** t = Sekunden seit Start; Winkel in Grad in p schreiben (p ist bereits genullt) */
    public abstract void pose(float t, Pose p);
    protected static float s(float t, float speed) { return (float) Math.sin(t * speed); }
    protected static float c(float t, float speed) { return (float) Math.cos(t * speed); }
    /** Ein-/Ausblenden der Pose in 0.25 s */
    public float weight(float t) { float in = Math.min(1f, t / 0.25f); if (duration <= 0) return in; return Math.min(in, Math.max(0f, (duration - t) / 0.25f)); }
}
