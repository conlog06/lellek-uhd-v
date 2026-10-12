package gg.lellek.hud.emote;

/** Ein Emote: Name, Kategorie, Preis (0 = frei), Dauer (0 = Schleife), Pose als Funktion der Zeit, optional Musik. */
public class Emote {
    public interface PoseFn { void apply(float t, Pose p); }
    public final String id, name, category; public final int price; public final float duration;
    private final PoseFn fn; public EmoteMusic music;
    public Emote(String id, String name, String category, int price, float duration, PoseFn fn) { this.id = id; this.name = name; this.category = category; this.price = price; this.duration = duration; this.fn = fn; }
    public Emote music(EmoteMusic m) { this.music = m; return this; }
    public void pose(float t, Pose p) { fn.apply(t, p); }
    /** Weiches Ein-/Ausblenden (0.3 s) */
    public float weight(float t) {
        float in = smooth(t / 0.3f); if (duration <= 0) return in;
        return Math.min(in, smooth((duration - t) / 0.3f));
    }
    static float smooth(float x) { x = Math.max(0, Math.min(1, x)); return x * x * (3 - 2 * x); }
}
