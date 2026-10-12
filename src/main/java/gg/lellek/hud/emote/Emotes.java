package gg.lellek.hud.emote;

import java.util.ArrayList;
import java.util.List;

import static gg.lellek.hud.emote.EmoteMusic.*;

/**
 * 100 Emotes: 50 frei, 50 im Shop. Posen in Grad (siehe {@link Pose}), Zeit t in Sekunden.
 * Preise > 0 müssen im Cosmetics-Server (SHOP) eingetragen sein – siehe docs/shop-prices.json.
 */
public final class Emotes {
    public static final List<Emote> ALL = new ArrayList<>();
    public static final String GESTE = "Gesten", SITZ = "Sitzen & Liegen", TANZ = "Tänze", SHOW = "Show & Akrobatik", SPORT = "Sport", GEFUEHL = "Gefühle", LUSTIG = "Lustig";
    public static final String[] CATEGORIES = { "Alle", GESTE, SITZ, TANZ, SHOW, SPORT, GEFUEHL, LUSTIG };
    public static List<Emote> shop() { List<Emote> l = new ArrayList<>(); for (Emote e : ALL) if (e.price > 0) l.add(e); return l; }
    public static Emote byId(String id) { for (Emote e : ALL) if (e.id.equals(id)) return e; return null; }

    private Emotes() {}
    private static Emote add(String id, String name, String cat, int price, float dur, Emote.PoseFn fn) { Emote e = new Emote(id, name, cat, price, dur, fn); ALL.add(e); return e; }

    // ---- Hilfsfunktionen ----
    static float S(float t, float hz) { return (float) Math.sin(2 * Math.PI * hz * t); }
    static float C(float t, float hz) { return (float) Math.cos(2 * Math.PI * hz * t); }
    /** 0..1..0 Hüpfer, hz Hüpfer pro Sekunde */
    static float hop(float t, float hz) { return (float) Math.abs(Math.sin(Math.PI * hz * t)); }
    static float ease(float x) { x = Math.max(0, Math.min(1, x)); return x * x * (3 - 2 * x); }
    static float ramp(float t, float a, float b) { return ease((t - a) / (b - a)); }
    static float pos(float v) { return Math.max(0, v); }
    static float lerp(float a, float b, float k) { return a + (b - a) * k; }
    /** Keyframes: kf(t, t0, v0, t1, v1, ...) mit weichen Übergängen */
    static float kf(float t, float... tv) {
        if (t <= tv[0]) return tv[1];
        for (int i = 2; i < tv.length; i += 2) if (t <= tv[i]) return lerp(tv[i - 1], tv[i + 1], ease((t - tv[i - 2]) / (tv[i] - tv[i - 2])));
        return tv[tv.length - 1];
    }
    /** Posenfolge: rate Posen pro Sekunde, weiches Umschalten. Frame = {rax, ray, raz, lax, lay, laz} */
    static void seq(Pose p, float t, float rate, float[][] frames) {
        float x = t * rate; int i = (int) Math.floor(x); float f = ease(Math.min(1, (x - i) / 0.35f));
        float[] a = frames[Math.floorMod(i - 1, frames.length)], b = frames[Math.floorMod(i, frames.length)];
        p.rax = lerp(a[0], b[0], f); p.ray = lerp(a[1], b[1], f); p.raz = lerp(a[2], b[2], f);
        p.lax = lerp(a[3], b[3], f); p.lay = lerp(a[4], b[4], f); p.laz = lerp(a[5], b[5], f);
    }
    static void sit(Pose p) { p.rootY = -0.62f; p.legs(-90, 8, 0); }
    static void lie(Pose p) { p.pitch = -90; p.rootY = -0.77f; }
    static void breathe(Pose p, float t) { p.bx += S(t, 0.25f) * 1.5f; p.hx += S(t, 0.25f) * 2; }

    static {
        // ===================== GESTEN =====================
        add("wave", "Winken", GESTE, 0, 2.5f, (t, p) -> { p.rax = -160; p.raz = 22 + S(t, 2.2f) * 26; p.hz = 5; p.bz = -3; p.lax = -4; });
        add("wave2", "Beidhändig winken", GESTE, 0, 2.5f, (t, p) -> { float a = S(t, 2.2f) * 25; p.rax = p.lax = -165; p.raz = 20 + a; p.laz = -20 + a; p.rootY = hop(t, 2.2f) * 0.04f; });
        add("hello", "Hallo!", GESTE, 0, 2f, (t, p) -> { p.rax = -172; p.raz = 30 + S(t, 1.6f) * 38; p.bz = -6 + S(t, 1.6f) * 4; p.hx = -8; });
        add("bye", "Tschüss", GESTE, 0, 2.5f, (t, p) -> { p.rax = -115; p.ray = -20; p.raz = S(t, 3) * 18; p.hz = -6; });
        add("point", "Zeigen", GESTE, 0, 1.8f, (t, p) -> { float k = ramp(t, 0, 0.25f); p.rax = -92 * k; p.ray = -5; p.hy = -6; p.bx = -3 * k; });
        add("pointup", "Nach oben zeigen", GESTE, 0, 1.8f, (t, p) -> { p.rax = -178 * ramp(t, 0, 0.3f); p.hx = -28; p.bx = -4; });
        add("salute", "Salutieren", GESTE, 0, 2.2f, (t, p) -> { float k = ramp(t, 0, 0.25f); p.rax = -122 * k; p.ray = -32 * k; p.raz = -8; p.hx = -6; p.lax = 2; });
        add("thumbsup", "Daumen hoch", GESTE, 0, 1.8f, (t, p) -> { p.rax = -82; p.ray = -12; p.hy = 8; p.hx = kf(t, 0, 0, 0.3f, 12, 0.6f, 0); });
        add("facepalm", "Facepalm", GESTE, 0, 2.2f, (t, p) -> { p.rax = -118; p.ray = -42; p.hx = 26; p.bx = 9; p.hy = S(t, 1) * 6; });
        add("shrug", "Schulterzucken", GESTE, 0, 1.8f, (t, p) -> { float k = kf(t, 0, 0, 0.3f, 1, 1.3f, 1, 1.8f, 0); p.arms(-30 * k, 0, 42 * k); p.hz = 9 * k; p.hx = -4 * k; p.rootY = 0.02f * k; });
        add("clap", "Klatschen", GESTE, 0, 3f, (t, p) -> { float c = 1 - hop(t, 2); p.arms(-74, -6 - 30 * c, 0); p.hx = 4; }).music(CLAPS);
        add("nod", "Nicken", GESTE, 0, 1.6f, (t, p) -> p.hx = 18 * hop(t, 2.2f));
        add("shake", "Kopfschütteln", GESTE, 0, 1.6f, (t, p) -> { p.hy = S(t, 2.5f) * 32; p.hx = 6; });
        add("bow", "Verbeugen", GESTE, 0, 2.3f, (t, p) -> { float k = kf(t, 0, 0, 0.5f, 1, 1.5f, 1, 2.1f, 0); p.bx = 52 * k; p.rax = -62 * k; p.ray = -48 * k; p.lax = 18 * k; p.hx = 8 * k; });
        add("think", "Nachdenken", GESTE, 0, 3f, (t, p) -> { p.rax = -124; p.ray = -46; p.lax = -42; p.lay = 52; p.hx = 12; p.hy = -14 + S(t, 0.4f) * 8; p.bz = 3; });
        add("beckon", "Herwinken", GESTE, 0, 2f, (t, p) -> { p.rax = -82 + S(t, 2) * 24; p.ray = -10; p.hx = 4; p.hz = 6; });
        add("stop", "Stopp!", GESTE, 0, 1.6f, (t, p) -> { float k = ramp(t, 0, 0.18f); p.rax = -96 * k; p.bx = -6 * k; p.hx = -4; });
        add("peace", "Peace", GESTE, 0, 2f, (t, p) -> { p.rax = -158; p.raz = 24; p.hz = -9; p.bz = -4; p.rootY = 0.02f * hop(t, 1); });
        add("heart", "Herz", GESTE, 0, 2.6f, (t, p) -> { p.arms(-160, 0, -30); p.hx = -10; p.bz = S(t, 0.8f) * 4; });
        add("crossarms", "Arme verschränken", GESTE, 0, 0, (t, p) -> { p.arms(-52, -52, 0); p.hx = -4; breathe(p, t); });
        add("akimbo", "Hände in die Hüften", GESTE, 0, 0, (t, p) -> { p.arms(-12, 22, 30); p.hz = 5; p.hx = -5; breathe(p, t); });
        add("shush", "Psst!", GESTE, 0, 2f, (t, p) -> { p.rax = -118; p.ray = -52; p.hx = 6; });
        add("listen", "Lauschen", GESTE, 0, 2.6f, (t, p) -> { p.rax = -132; p.ray = 26; p.hz = -16; p.bz = -6; p.hy = 10; });
        add("lookaround", "Umsehen", GESTE, 0, 3.2f, (t, p) -> { p.hy = S(t, 0.35f) * 60; p.hx = -6; p.rax = -142; p.ray = -32; p.by = S(t, 0.35f) * 12; });
        add("selfie", "Selfie", GESTE, 0, 2.5f, (t, p) -> { p.rax = -138; p.ray = 18; p.hz = -12; p.hy = 12; p.lax = -150; p.laz = -20; p.bz = -5; });

        // ===================== SITZEN & LIEGEN =====================
        add("sit", "Sitzen", SITZ, 0, 0, (t, p) -> { sit(p); p.arms(-34, -8, 0); breathe(p, t); });
        add("sitcross", "Schneidersitz", SITZ, 0, 0, (t, p) -> { p.rootY = -0.66f; p.legs(-86, 32, 8); p.arms(-30, -18, 0); p.bx = 4; breathe(p, t); });
        add("sitlean", "Zurückgelehnt", SITZ, 0, 0, (t, p) -> { sit(p); p.legs(-82, 10, 0); p.bx = -22; p.arms(32, 0, 12); p.hx = 8; breathe(p, t); });
        add("sitdangle", "Beine baumeln", SITZ, 0, 0, (t, p) -> { sit(p); p.rlx = -88 + S(t, 0.7f) * 16; p.llx = -88 - S(t, 0.7f) * 16; p.arms(-28, -6, 4); p.hy = S(t, 0.2f) * 12; });
        add("kneel", "Knien", SITZ, 0, 0, (t, p) -> { p.rootY = -0.36f; p.rlx = -84; p.llx = 28; p.arms(-12, 0, 6); p.hx = 6; breathe(p, t); });
        add("thinker", "Der Denker", SITZ, 0, 0, (t, p) -> { sit(p); p.bx = 22; p.rax = -118; p.ray = -28; p.lax = -40; p.hx = 8; });
        add("laydown", "Hinlegen", SITZ, 0, 0, (t, p) -> { lie(p); p.arms(-6, 0, 6); p.rootY += S(t, 0.25f) * 0.005f; });
        add("sunbathe", "Sonnen", SITZ, 0, 0, (t, p) -> { lie(p); p.arms(-168, 0, -24); p.rlz = 4; p.llx = -14; });
        add("sleep", "Schlafen", SITZ, 0, 0, (t, p) -> { p.roll = 90; p.rootY = -0.64f; p.arms(-34, 0, 0); p.rlx = -26; p.llx = -10; p.hx = 12 + S(t, 0.2f) * 2; });
        add("meditate", "Meditieren", SITZ, 0, 0, (t, p) -> { p.rootY = -0.6f + S(t, 0.3f) * 0.05f; p.legs(-86, 32, 8); p.arms(-40, -28, 22); p.hx = 10; });
        add("pray", "Beten", SITZ, 0, 0, (t, p) -> { p.rootY = -0.36f; p.rlx = -84; p.llx = 28; p.arms(-78, -34, 0); p.hx = 22; });

        // ===================== TÄNZE (frei) =====================
        add("dance", "Tanzen", TANZ, 0, 0, (t, p) -> { float a = S(t, 1); p.rootY = hop(t, 2) * 0.06f; p.rax = -60 + a * 50; p.lax = -60 - a * 50; p.raz = 20; p.laz = -20; p.by = a * 15; p.hy = -a * 9; p.rlx = -25 * pos(a); p.llx = -25 * pos(-a); }).music(POP);
        add("dab", "Dab", TANZ, 0, 1.6f, (t, p) -> { float k = ramp(t, 0, 0.18f); p.rax = -136 * k; p.raz = 38 * k; p.lax = -112 * k; p.lay = 62 * k; p.hx = 34 * k; p.hy = 28 * k; p.bx = 12 * k; p.bz = -8 * k; });
        add("hipsway", "Hüftschwung", TANZ, 0, 0, (t, p) -> { float a = S(t, 0.9f); p.rootX = a * 0.07f; p.bz = -a * 8; p.by = a * 10; p.arms(-24, 0, 22 + a * 10); p.hz = a * 6; }).music(LATIN);
        add("headbang", "Headbang", TANZ, 0, 0, (t, p) -> { p.hx = 18 + S(t, 2.3f) * 30; p.bx = 8 + S(t, 2.3f) * 6; p.rax = -168; p.raz = 18; p.rax += hop(t, 2.3f) * 12; p.lax = -20; }).music(AIRGUITAR);
        add("hiphop", "Hip-Hop", TANZ, 0, 0, (t, p) -> { float h = hop(t, 1.75f); p.rootY = -0.05f * h; p.bx = 10 + h * 8; p.hx = h * 14; p.arms(-34, 0, 14 + h * 10); p.by = S(t, 0.88f) * 14; }).music(FUNK);
        add("victory", "Siegestanz", TANZ, 0, 3f, (t, p) -> { p.rax = -170 + 22 * S(t, 2); p.lax = -170 - 22 * S(t, 2); p.raz = 15; p.laz = -15; p.rootY = hop(t, 2) * 0.16f; p.by = S(t, 1) * 14; p.hx = -10; }).music(FANFARE);
        add("raiseroof", "Raise the Roof", TANZ, 0, 0, (t, p) -> { float h = hop(t, 2); p.arms(-150 - h * 22, 0, -12); p.rootY = h * 0.05f; p.hx = -8 + h * 8; p.by = S(t, 1) * 10; }).music(POP);
        add("conga", "Conga", TANZ, 0, 0, (t, p) -> { float b = pos(S(t, 1.05f)); p.arms(-88, -10, 0); p.rlz = 32 * b; p.rootX = S(t, 0.52f) * 0.05f; p.bz = -S(t, 0.52f) * 6; p.hy = S(t, 0.52f) * 10; }).music(CHIPTUNE);
        add("tpose", "T-Pose", LUSTIG, 0, 0, (t, p) -> { p.arms(0, 0, 90); p.hx = -4; });
        add("jumpingjack", "Hampelmann", SPORT, 0, 0, (t, p) -> { float j = hop(t, 1.5f); p.arms(0, 0, lerp(8, 168, j)); p.rlz = 16 * j; p.llz = -16 * j; p.rootY = 0.24f * j; }).music(DRUMS);
        add("pushup", "Liegestütze", SPORT, 0, 0, (t, p) -> { float w = (1 - C(t, 0.7f)) / 2; p.pitch = 90; p.rootY = -0.6f + 0.22f * w; p.arms(-90, 0, 0); p.hx = -60; }).music(DRUMS);
        add("situp", "Sit-ups", SPORT, 0, 0, (t, p) -> { lie(p); float w = (1 - C(t, 0.6f)) / 2; p.bx = 72 * w; p.arms(-150, 0, -20); p.legs(-25, 0, 0); });
        add("boxing", "Boxen", SPORT, 0, 0, (t, p) -> { float a = S(t, 2.5f); p.rax = -86 + a * 34; p.lax = -86 - a * 34; p.ray = -16; p.lay = 16; p.by = a * 10; p.hx = 10; p.bx = 6; p.rootY = hop(t, 2.5f) * 0.03f; });
        add("stretch", "Strecken", SPORT, 0, 3f, (t, p) -> { float k = kf(t, 0, 0, 0.8f, 1, 2.4f, 1, 3f, 0); p.arms(-176 * k, 0, 8); p.bz = S(t, 0.5f) * 12 * k; p.rootY = 0.04f * k; p.hx = -10 * k; });

        // ===================== GEFÜHLE / LUSTIG (frei) =====================
        add("cry", "Weinen", GEFUEHL, 0, 3f, (t, p) -> { p.arms(-136, -46, 0); p.hx = 26 + S(t, 3) * 4; p.bx = 10; p.rootY = hop(t, 3) * 0.015f; }).music(SAD);
        add("laugh", "Lachen", GEFUEHL, 0, 2.5f, (t, p) -> { p.hx = -22 + S(t, 5) * 6; p.bx = 4 + S(t, 2.5f) * 6; p.arms(-36, -34, 0); });
        add("angry", "Wütend", GEFUEHL, 0, 3f, (t, p) -> { p.rlx = -36 * pos(S(t, 1.5f)); p.llx = -36 * pos(-S(t, 1.5f)); p.arms(-40 + S(t, 8) * 8, 0, 18); p.bx = 10; p.hx = 12; }).music(DRUMS);
        add("scared", "Erschrocken", GEFUEHL, 0, 2.2f, (t, p) -> { p.arms(-140, -10, 34); p.bx = -10; p.hx = -12; p.rootX = S(t, 11) * 0.012f; p.rootZ = -0.15f * ramp(t, 0, 0.2f); });
        add("faint", "Ohnmacht", GEFUEHL, 0, 3.5f, (t, p) -> { float k = ease(ramp(t, 0.4f, 0.95f)); p.pitch = -90 * k; p.rootY = -0.77f * k; p.arms(-20 * k, 0, 40 * k); p.hx = -10 + S(t, 6) * 6 * (1 - k); });
        add("chicken", "Huhn", LUSTIG, 0, 0, (t, p) -> { float h = hop(t, 3); p.arms(-6, 0, 12 + h * 42); p.bx = 14; p.hx = S(t, 3) * 12; p.rootY = 0.03f * h; });
        add("zombie", "Zombie", LUSTIG, 0, 0, (t, p) -> { p.arms(-90, 0, 0); p.hz = 15; p.bz = 7; p.rlx = S(t, 0.6f) * 14; p.llx = -S(t, 0.6f) * 14; p.hx = 10; });
        add("superhero", "Superheld", SHOW, 0, 0, (t, p) -> { p.rax = -180; p.lax = 10; p.hx = -14; p.bx = -5; p.legs(0, 0, 6); });
        add("flex", "Muskeln zeigen", SPORT, 0, 2.6f, (t, p) -> { float h = hop(t, 1.5f); p.arms(-22, 0, 104 + h * 10); p.hx = -10; p.bx = -6; p.rootY = 0.02f * h; });
        add("spin", "Pirouette", SHOW, 0, 1.4f, (t, p) -> { p.yaw = 360 * ease(t / 1.2f); p.arms(-172, 0, -12); p.llx = -30; p.rootY = 0.06f; });

        // ===================== SHOP: TÄNZE =====================
        add("floss", "Floss", TANZ, 300, 0, (t, p) -> { float a = S(t, 2), c = C(t, 2); p.raz = a * 34; p.laz = a * 34; p.rax = 22 * c; p.lax = -22 * c; p.rootX = -a * 0.07f; p.by = -a * 10; p.rootY = hop(t, 2) * 0.02f; }).music(POP);
        add("robot", "Roboter", TANZ, 250, 0, (t, p) -> seqRobot(p, t)).music(CHIPTUNE);
        add("twist", "Twist", TANZ, 200, 0, (t, p) -> { float a = S(t, 2); p.by = a * 34; p.rly = -a * 20; p.lly = -a * 20; p.rootY = -0.07f - 0.04f * hop(t, 2); p.arms(-60, 0, 32); p.bx = 6; }).music(FUNK);
        add("macarena", "Makarena", TANZ, 350, 0, (t, p) -> { seq(p, t, 2.13f, new float[][] { { -90, 0, 0, 0, 0, 0 }, { -90, 0, 0, -90, 0, 0 }, { -90, 0, 0, -90, 0, 0 }, { -100, -50, 0, -90, 0, 0 }, { -100, -50, 0, -100, 50, 0 }, { -150, -40, 0, -100, 50, 0 }, { -150, -40, 0, -150, 40, 0 }, { -20, 30, 20, -150, 40, 0 }, { -20, 30, 20, -20, -30, -20 } }); p.rootX = S(t, 1.06f) * 0.06f; p.by = S(t, 1.06f) * 10; }).music(LATIN);
        add("disco", "Disco", TANZ, 250, 0, (t, p) -> { float k = (S(t, 1) + 1) / 2; p.rax = lerp(-20, -165, k); p.raz = lerp(-14, 34, k); p.lax = -24; p.bz = lerp(6, -8, k); p.hz = lerp(-10, 10, k); p.rootY = hop(t, 2) * 0.04f; }).music(DISCO);
        add("cancan", "Cancan", TANZ, 300, 0, (t, p) -> { p.rlx = -88 * pos(S(t, 1.25f)); p.llx = -88 * pos(-S(t, 1.25f)); p.arms(-12, 0, 52); p.rootY = hop(t, 2.5f) * 0.05f; p.bx = -6; }).music(CANCAN);
        add("shuffle", "Shuffle", TANZ, 300, 0, (t, p) -> { float a = S(t, 2); p.rlz = 22 * pos(a); p.llz = -22 * pos(-a); p.rootX = a * 0.1f; p.arms(-34 + a * 14, 0, 22); p.rootY = hop(t, 4) * 0.04f; p.hx = hop(t, 4) * 8; }).music(ELECTRO);
        add("moonwalk", "Moonwalk", TANZ, 400, 4.5f, (t, p) -> { p.rootZ = -0.32f * t; p.rlx = 22 * S(t, 1.4f); p.llx = -22 * S(t, 1.4f); p.rax = -16 * S(t, 1.4f); p.lax = 16 * S(t, 1.4f); p.bx = -4; p.hy = 10; }).music(FUNK);
        add("chickendance", "Ententanz", TANZ, 250, 0, (t, p) -> { float h = hop(t, 2.1f); p.arms(-18, 0, 14 + h * 58); p.bx = 10; p.rootY = -0.04f * h; p.hy = S(t, 1.05f) * 12; }).music(CHICKEN);
        add("ymca", "YMCA", TANZ, 350, 0, (t, p) -> { seq(p, t, 2.05f, new float[][] { { -165, 0, 30, -165, 0, -30 }, { -150, 0, -50, -150, 0, 50 }, { -150, 0, 42, -60, 0, -10 }, { -170, 0, -16, -170, 0, 16 } }); p.rootY = hop(t, 2.05f) * 0.04f; }).music(DISCO);
        add("wavedance", "Wellen-Tanz", TANZ, 200, 0, (t, p) -> { p.raz = 90 + S(t, 1.4f) * 22; p.laz = -90 + S(t + 0.18f, 1.4f) * 22; p.bz = S(t, 1.4f) * 7; p.hz = S(t - 0.1f, 1.4f) * 8; }).music(ELECTRO);
        add("salsa", "Salsa", TANZ, 300, 0, (t, p) -> { float a = S(t, 1.07f); p.rlx = 26 * a; p.llx = -26 * a; p.by = a * 20; p.rax = -82; p.ray = -18 + a * 18; p.lax = -42; p.lay = 20; p.rootX = a * 0.04f; }).music(LATIN);
        add("ballet", "Ballett", TANZ, 300, 0, (t, p) -> { p.yaw = t * 150; p.arms(-168, 0, -20); p.rootY = 0.08f; p.llx = -42; p.llz = -26; p.hx = -10; }).music(WALTZ);
        add("horse", "Pferdetanz", TANZ, 350, 0, (t, p) -> { float h = hop(t, 2.2f); p.rootY = h * 0.08f; p.rlx = -22 * pos(S(t, 1.1f)); p.llx = -22 * pos(-S(t, 1.1f)); p.arms(-72 - h * 10, -40, 0); p.hy = S(t, 0.55f) * 8; p.bx = 6; }).music(ELECTRO);
        add("charleston", "Charleston", TANZ, 250, 0, (t, p) -> { float a = S(t, 1.2f); p.rlx = 40 * a; p.llx = -40 * a; p.rlz = 8 * a; p.rax = -40 * a; p.lax = 40 * a; p.bz = a * 6; p.rootY = hop(t, 2.4f) * 0.03f; }).music(FUNK);
        add("breakdance", "Breakdance", TANZ, 500, 0, (t, p) -> { p.rootY = -0.58f; p.yaw = t * 300; p.pitch = -16; p.legs(-88, 38, 10); p.rlx += S(t, 2.5f) * 30; p.arms(40, 0, 22); }).music(DRUMS);
        add("helicopter", "Helikopter", TANZ, 300, 0, (t, p) -> { p.arms(0, 0, 90); p.yaw = t * 480; p.rootY = hop(t, 2) * 0.05f; p.hx = -8; }).music(ELECTRO);
        add("techno", "Techno", TANZ, 200, 0, (t, p) -> { float h = hop(t, 2.2f); p.rax = -128 - 34 * h; p.lax = -20; p.rootY = 0.05f * h; p.hx = -10 * h + 6; p.by = S(t, 0.55f) * 8; }).music(ELECTRO);
        add("carlton", "Carlton", TANZ, 350, 0, (t, p) -> { float a = S(t, 1.2f); p.rax = -30; p.lax = -30; p.raz = 40 * a + 10; p.laz = 40 * a - 10; p.rootX = -0.06f * a; p.hz = -a * 8; p.rlx = 16 * pos(a); p.llx = 16 * pos(-a); }).music(POP);
        add("swim", "Schwimm-Tanz", TANZ, 200, 0, (t, p) -> { p.rax = -180 * ((t * 0.9f) % 1); p.lax = -180 * ((t * 0.9f + 0.5f) % 1); p.bz = S(t, 0.9f) * 8; p.hz = -S(t, 0.9f) * 8; p.rootY = hop(t, 1.8f) * 0.03f; }).music(POP);
        add("sprinkler", "Rasensprenger", TANZ, 250, 0, (t, p) -> { int step = (int) (t * 4) % 8; float yaw = step < 6 ? -40 + step * 16 : 40 - (step - 5) * 40; p.rax = -90; p.ray = -yaw; p.lax = -158; p.lay = 40; p.laz = -30; p.by = yaw * 0.4f; p.hy = yaw * 0.3f; }).music(POP);
        add("electro", "Elektro-Arme", TANZ, 300, 0, (t, p) -> { p.raz = 90 + S(t, 2) * 30; p.rax = S(t + 0.12f, 2) * 30; p.laz = -90 + S(t + 0.25f, 2) * 30; p.lax = S(t + 0.37f, 2) * 30; p.bz = S(t, 1) * 6; p.rootY = hop(t, 2) * 0.03f; }).music(ELECTRO);
        add("polka", "Polka", TANZ, 200, 0, (t, p) -> { float h = hop(t, 2); p.rootY = h * 0.1f; p.rlx = -34 * pos(S(t, 1)); p.llx = -34 * pos(-S(t, 1)); p.arms(-14, 22, 30); p.yaw = S(t, 0.5f) * 25; }).music(COWBELLS);
        add("zumba", "Zumba", TANZ, 250, 0, (t, p) -> { float a = S(t, 2.1f); p.rax = -100 + a * 70; p.lax = -100 - a * 70; p.raz = 20; p.laz = -20; p.rootX = S(t, 1.05f) * 0.07f; p.bz = -S(t, 1.05f) * 6; p.rootY = hop(t, 2.1f) * 0.05f; }).music(LATIN);
        add("tango", "Tango", TANZ, 300, 0, (t, p) -> { float a = S(t, 0.55f); p.rax = -92; p.ray = 26; p.lax = -104; p.lay = 18; p.bx = -6 + a * 8; p.rootZ = a * 0.2f; p.hy = (a > 0 ? 1 : -1) * 34; p.rlx = 28 * a; }).music(WALTZ);
        add("dj", "DJ", TANZ, 300, 0, (t, p) -> { p.rax = -62; p.ray = -32 + S(t, 4) * 20; p.lax = -158; p.lay = 62; p.hx = 10 + hop(t, 2.2f) * 12; p.rootY = hop(t, 2.2f) * 0.03f; }).music(ELECTRO);
        add("hula", "Hula-Hoop", TANZ, 300, 0, (t, p) -> { p.rootX = S(t, 1.6f) * 0.06f; p.rootZ = C(t, 1.6f) * 0.06f; p.bz = -S(t, 1.6f) * 6; p.bx = -C(t, 1.6f) * 6; p.arms(-168, 0, -14); }).music(LATIN);
        add("vogue", "Vogue", TANZ, 250, 0, (t, p) -> { seq(p, t, 1.4f, new float[][] { { -170, 0, -40, -10, 0, 0 }, { -120, -60, 0, -120, 60, 0 }, { -30, 0, 60, -170, 0, 40 }, { -150, -40, -10, -20, 0, -30 } }); p.hz = S(t, 0.7f) * 10; p.bz = S(t, 0.7f) * 6; }).music(DISCO);

        // ===================== SHOP: SHOW & AKROBATIK =====================
        add("backflip", "Rückwärtssalto", SHOW, 450, 1.4f, (t, p) -> { float c = kf(t, 0, 0, 0.25f, 1, 0.35f, 0); float r = ease(ramp(t, 0.3f, 1.05f)); p.rootY = -0.15f * c + (float) Math.sin(Math.PI * r) * 1.1f; p.pitch = -360 * r; p.arms(-60 * c - 170 * (float) Math.sin(Math.PI * r), 0, 10); p.legs(-60 * (float) Math.sin(Math.PI * r), 0, 0); });
        add("frontflip", "Salto vorwärts", SHOW, 450, 1.4f, (t, p) -> { float c = kf(t, 0, 0, 0.25f, 1, 0.35f, 0); float r = ease(ramp(t, 0.3f, 1.05f)); p.rootY = -0.15f * c + (float) Math.sin(Math.PI * r) * 1.0f; p.pitch = 360 * r; p.arms(-90 * (float) Math.sin(Math.PI * r), 0, 10); p.legs(-70 * (float) Math.sin(Math.PI * r), 0, 0); });
        add("cartwheel", "Rad schlagen", SHOW, 400, 1.6f, (t, p) -> { float r = ease(ramp(t, 0.1f, 1.4f)); p.roll = 360 * r; p.rootX = -1.4f * r; p.rootY = (float) Math.sin(Math.PI * r) * 0.25f; p.arms(-10, 0, 165); p.legs(0, 0, 30); });
        add("handstand", "Handstand", SHOW, 350, 0, (t, p) -> { float k = ramp(t, 0, 0.6f); p.pitch = 180 * k; p.rootY = 0.6f * k; p.arms(-178 * k, 0, 8); p.legs(0, 0, 10 + S(t, 0.5f) * 4); p.roll = S(t, 0.7f) * 3 * k; });
        add("headspin", "Kopfdrehung", SHOW, 500, 0, (t, p) -> { p.pitch = 180; p.rootY = 0.02f; p.yaw = t * 520; p.legs(0, 0, 34); p.arms(-90, 0, 50); }).music(DRUMS);
        add("levitate", "Schweben", SHOW, 300, 0, (t, p) -> { float k = ramp(t, 0, 1.2f); p.rootY = 0.6f * k - 0.3f + S(t, 0.4f) * 0.06f; p.legs(-86, 32, 8); p.arms(-30, 0, 34); p.hx = -6; }).music(MAGIC);
        add("fly", "Fliegen", SHOW, 400, 0, (t, p) -> { float k = ramp(t, 0, 0.8f); p.pitch = 80 * k; p.rootY = 1.0f * k + S(t, 0.5f) * 0.08f; p.rax = -178; p.lax = 6; p.hx = -70 * k; p.legs(4, 0, 4); });
        add("magic", "Zaubern", SHOW, 300, 3f, (t, p) -> { float up = ramp(t, 2, 2.4f); p.arms(lerp(-82 + S(t, 4) * 10, -172, up), -14, lerp(0, 30, up)); p.rootY = 0.1f * up; p.hx = -10 * up; }).music(MAGIC);
        add("karate", "Karate-Kick", SHOW, 300, 1.6f, (t, p) -> { float k = kf(t, 0, 0, 0.35f, 0.3f, 0.6f, 1, 1.0f, 1, 1.4f, 0); p.rlx = -100 * k; p.bx = -16 * k; p.rax = -80; p.lax = -80; p.ray = 20; p.lay = -20; p.yaw = -20 * k; p.rootY = -0.05f; }).music(EPIC);
        add("kungfu", "Kung-Fu", SHOW, 300, 0, (t, p) -> { p.llx = -86; p.arms(-36, 0, 118 + S(t, 0.6f) * 10); p.hx = -8; p.rootY = S(t, 0.6f) * 0.02f; }).music(EPIC);
        add("ninja", "Ninja", SHOW, 250, 0, (t, p) -> { p.rootY = -0.08f; p.legs(-20, 0, 20); p.rax = -90; p.ray = -58; p.lax = -58; p.lay = 30; p.hy = -20; p.bx = 10; p.yaw = 25; });
        add("airguitar", "Luftgitarre", SHOW, 300, 0, (t, p) -> { p.lax = -56; p.lay = 42; p.rax = -40 + S(t, 4) * 22; p.ray = -34; p.hx = 14 + S(t, 2.3f) * 15; p.bx = 10; p.legs(0, 0, 9); }).music(AIRGUITAR);
        add("airdrums", "Luftschlagzeug", SHOW, 300, 0, (t, p) -> { p.rax = -58 - hop(t, 4) * 32; p.lax = -58 - hop(t + 0.125f, 4) * 32; p.ray = -16; p.lay = 16; p.hx = 8 + hop(t, 2) * 10; }).music(DRUMS);
        add("conductor", "Dirigent", SHOW, 250, 0, (t, p) -> { p.rax = -100 + S(t, 1.25f) * 40; p.raz = 20 * C(t, 1.25f); p.lax = -70 + S(t, 0.625f) * 30; p.laz = -20; p.hx = -10; p.by = S(t, 0.625f) * 8; }).music(WALTZ);
        add("celebrate", "Feiern", SHOW, 200, 4f, (t, p) -> { float h = hop(t, 1.6f); p.rootY = h * 0.35f; p.arms(-170, 0, 25 + h * 15); p.legs(-12 * h, 0, 8 * h); }).music(FANFARE);
        add("bodybuilder", "Bodybuilder", SPORT, 200, 0, (t, p) -> { seq(p, t, 0.8f, new float[][] { { -22, 0, 110, -22, 0, -110 }, { -170, 0, 40, -170, 0, -40 }, { -60, -60, 0, -60, 60, 0 } }); p.hx = -8; p.bx = -4; });
        add("yoga", "Yoga-Baum", SPORT, 200, 0, (t, p) -> { p.rlx = -30; p.rlz = 44; p.rootY = 0.0f; p.arms(-178, 0, -10); p.bz = S(t, 0.3f) * 3; });
        add("golf", "Golfschwung", SPORT, 200, 2.2f, (t, p) -> { float k = kf(t, 0, 0, 0.8f, -1, 1.1f, 1, 1.6f, 1, 2.2f, 0); p.arms(-40, -30, 0); p.by = 70 * k; p.hy = -40 * k; p.bx = 22; p.rootY = -0.04f; });
        add("fishing", "Angeln", SPORT, 150, 0, (t, p) -> { float cast = kf(t % 4, 0, 0, 0.5f, -1, 0.8f, 1, 4, 1); p.rax = -60 + cast * 40; p.ray = -10; p.lax = -40; p.hx = 6; });
        add("sneeze", "Niesen", LUSTIG, 150, 1.8f, (t, p) -> { float w = kf(t, 0, 0, 0.8f, 1, 0.9f, -1.4f, 1.3f, 0); p.hx = -22 * w; p.bx = -8 * w; p.arms(-40 - 20 * Math.max(0, w), -30, 0); });
        add("popcorn", "Popcorn", LUSTIG, 150, 0, (t, p) -> { p.lax = -56; p.lay = 30; p.rax = -70 - hop(t, 1.2f) * 50; p.ray = -40; p.hx = 6 - hop(t, 1.2f) * 6; });
        add("selfie2", "Gruppen-Selfie", LUSTIG, 150, 2.5f, (t, p) -> { p.rax = -150; p.ray = 30; p.lax = -158; p.laz = -30; p.hz = -14; p.bz = -8; p.rootY = 0.03f * hop(t, 1); });
    }

    /** Roboter: harte Schritte statt fließender Bewegung */
    private static void seqRobot(Pose p, float t) {
        int s = (int) (t * 2.33f) % 4;
        float[][] f = { { -90, 0, 0, 0, 0, 0 }, { -90, 40, 0, -90, 40, 0 }, { 0, 0, 0, -90, 0, 0 }, { -90, -40, 0, -90, -40, 0 } };
        float[] a = f[s]; p.rax = a[0]; p.ray = a[1]; p.lax = a[3]; p.lay = a[4];
        p.hy = s == 1 ? 30 : s == 3 ? -30 : 0; p.by = -p.hy / 3; p.rlx = s == 1 ? -20 : 0; p.llx = s == 3 ? -20 : 0;
    }
}
