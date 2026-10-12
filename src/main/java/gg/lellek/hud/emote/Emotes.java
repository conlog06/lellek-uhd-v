package gg.lellek.hud.emote;

import java.util.ArrayList;
import java.util.List;

/** Die 30 eingebauten Emotes. Preise müssen mit SHOP im Cosmetics-Server übereinstimmen. */
public class Emotes {
    public static final List<Emote> ALL = new ArrayList<Emote>();
    public static Emote byId(String id) { for (Emote e : ALL) if (e.id.equals(id)) return e; return null; }
    private static void add(Emote e) { ALL.add(e); }
    static {
        add(new Emote("sit", "Sitzen", 0, 0) { public void pose(float t, Pose p) { p.rLegX = p.lLegX = -90; p.rArmX = p.lArmX = -15; p.offsetY = 0.55f; } });
        add(new Emote("wave", "Winken", 100, 2.5f) { public void pose(float t, Pose p) { p.rArmX = -160; p.rArmZ = 20 + s(t, 12) * 25; p.headY = -10; } });
        add(new Emote("dab", "Dab", 150, 1.6f) { public void pose(float t, Pose p) { p.headX = 20; p.headY = 40; p.rArmX = -100; p.rArmY = -40; p.rArmZ = 40; p.lArmX = -130; p.lArmZ = -80; p.bodyY = 15; } });
        add(new Emote("clap", "Klatschen", 120, 3f) { public void pose(float t, Pose p) { float o = 15 + Math.abs(s(t, 14)) * 30; p.rArmX = p.lArmX = -80; p.rArmY = -o; p.lArmY = o; } });
        add(new Emote("nod", "Nicken", 80, 1.5f) { public void pose(float t, Pose p) { p.headX = 15 + s(t, 10) * 15; } });
        add(new Emote("shake", "Kopfschütteln", 80, 1.5f) { public void pose(float t, Pose p) { p.headY = s(t, 10) * 30; } });
        add(new Emote("bow", "Verbeugen", 150, 2f) { public void pose(float t, Pose p) { float w = (float) Math.sin(Math.min(1f, t / 0.6f) * Math.PI / 2); p.bodyX = 45 * w; p.headX = 20 * w; p.rArmX = p.lArmX = 30 * w; p.offsetY = 0.12f * w; } });
        add(new Emote("cheer", "Jubeln", 200, 2.5f) { public void pose(float t, Pose p) { p.rArmX = p.lArmX = -170 + s(t, 12) * 10; p.rArmZ = 15; p.lArmZ = -15; p.offsetY = -Math.abs(s(t, 12)) * 0.15f; p.rLegX = p.lLegX = Math.abs(s(t, 12)) * -10; } });
        add(new Emote("shrug", "Schulterzucken", 120, 1.8f) { public void pose(float t, Pose p) { p.rArmX = p.lArmX = -30; p.rArmZ = 60; p.lArmZ = -60; p.headY = 15; p.bodyX = -5; } });
        add(new Emote("point", "Zeigen", 100, 1.5f) { public void pose(float t, Pose p) { p.rArmX = -90; p.headX = 5; } });
        add(new Emote("tpose", "T-Pose", 100, 0) { public void pose(float t, Pose p) { p.rArmZ = 90; p.lArmZ = -90; } });
        add(new Emote("salute", "Salut", 150, 2f) { public void pose(float t, Pose p) { p.rArmX = -150; p.rArmY = 50; p.rArmZ = 0; p.headX = -5; } });
        add(new Emote("heart", "Herz", 250, 2.5f) { public void pose(float t, Pose p) { p.rArmX = p.lArmX = -160; p.rArmZ = 45; p.lArmZ = -45; p.rArmY = -20; p.lArmY = 20; p.headX = -10; } });
        add(new Emote("meditate", "Meditieren", 200, 0) { public void pose(float t, Pose p) { p.rLegX = p.lLegX = -90; p.rLegZ = 35; p.lLegZ = -35; p.rArmX = p.lArmX = -30; p.rArmY = -30; p.lArmY = 30; p.offsetY = 0.6f + s(t, 2) * 0.02f; p.headX = 10; } });
        add(new Emote("handsup", "Hände hoch", 120, 0) { public void pose(float t, Pose p) { p.rArmX = p.lArmX = -175; } });
        add(new Emote("boxing", "Boxen", 250, 3f) { public void pose(float t, Pose p) { float a = s(t, 16); p.rArmX = -90 + a * 40; p.lArmX = -90 - a * 40; p.rArmY = -15; p.lArmY = 15; p.bodyY = a * 8; p.headX = 10; } });
        add(new Emote("think", "Nachdenken", 150, 2.5f) { public void pose(float t, Pose p) { p.rArmX = -120; p.rArmY = -50; p.rArmZ = 10; p.headX = 10; p.headY = -15 + s(t, 2) * 5; p.lArmX = -20; p.lArmY = 40; } });
        add(new Emote("yawn", "Gähnen", 120, 2f) { public void pose(float t, Pose p) { p.rArmX = -140; p.rArmY = -50; p.headX = -20; p.lArmX = -60; p.lArmZ = -30; } });
        add(new Emote("laugh", "Lachen", 200, 2.2f) { public void pose(float t, Pose p) { p.headX = -15 + s(t, 18) * 6; p.bodyX = s(t, 18) * 5; p.rArmX = -40; p.rArmY = -40; p.lArmX = -40; p.lArmY = 40; } });
        add(new Emote("cry", "Weinen", 150, 2.5f) { public void pose(float t, Pose p) { p.rArmX = p.lArmX = -140; p.rArmY = -35; p.lArmY = 35; p.headX = 25 + s(t, 10) * 3; p.bodyX = 8; } });
        add(new Emote("wave2", "Beidhändig winken", 150, 2.5f) { public void pose(float t, Pose p) { p.rArmX = p.lArmX = -160; p.rArmZ = 20 + s(t, 12) * 25; p.lArmZ = -20 - s(t, 12) * 25; } });
        add(new Emote("robot", "Roboter", 300, 3.2f) { public void pose(float t, Pose p) { float a = Math.signum(s(t, 8)); p.rArmX = -90; p.lArmX = -90; p.rArmY = a * 40; p.lArmY = a * 40; p.headY = a * 30; p.bodyY = -a * 10; p.rLegX = a > 0 ? -20 : 0; p.lLegX = a > 0 ? 0 : -20; } });
        add(new Emote("hips", "Hüftschwung", 250, 3f) { public void pose(float t, Pose p) { float a = s(t, 8); p.bodyY = a * 20; p.rLegZ = a * 10; p.lLegZ = a * 10; p.rArmX = -20; p.lArmX = -20; p.rArmZ = 20 + a * 10; p.lArmZ = -20 + a * 10; } });
        add(new Emote("squat", "Kniebeuge", 200, 3f) { public void pose(float t, Pose p) { float w = (1 - c(t, 6)) / 2; p.rLegX = p.lLegX = -90 * w; p.offsetY = 0.5f * w; p.rArmX = p.lArmX = -90 * w; p.bodyX = 10 * w; } });
        add(new Emote("hero", "Superheld", 300, 0) { public void pose(float t, Pose p) { p.rArmX = -175; p.lArmX = 20; p.lArmY = 20; p.headX = -15; p.bodyX = -5; } });
        add(new Emote("sleep", "Schlafen", 250, 0) { public void pose(float t, Pose p) { p.bodyX = -90; p.headX = -90 + s(t, 2) * 2; p.rArmX = p.lArmX = -90; p.rLegX = p.lLegX = -90; p.offsetY = 1.2f; } });
        add(new Emote("floss", "Floss", 400, 3.5f) { public void pose(float t, Pose p) { float a = s(t, 12); p.rArmX = -40 - a * 40; p.lArmX = -40 + a * 40; p.rArmZ = 25 * a; p.lArmZ = 25 * a; p.bodyY = a * 15; p.rLegZ = -a * 5; p.lLegZ = -a * 5; } });
        add(new Emote("dance", "Tanzen", 350, 4f) { public void pose(float t, Pose p) { float a = s(t, 10), b = c(t, 10); p.rArmX = -120 + a * 40; p.lArmX = -120 - a * 40; p.rArmZ = 30; p.lArmZ = -30; p.bodyY = b * 12; p.headY = -b * 12; p.rLegX = a > 0 ? -30 : 0; p.lLegX = a < 0 ? -30 : 0; p.offsetY = -Math.abs(a) * 0.08f; } });
        add(new Emote("facepalm", "Facepalm", 150, 2f) { public void pose(float t, Pose p) { p.rArmX = -150; p.rArmY = -40; p.rArmZ = 10; p.headX = 25; p.bodyX = 5; } });
        add(new Emote("pushup", "Liegestütze", 300, 4f) { public void pose(float t, Pose p) { float w = (1 - c(t, 8)) / 2; p.bodyX = 85; p.headX = -60; p.rLegX = p.lLegX = -5; p.rArmX = p.lArmX = -90 + w * 50; p.offsetY = 1.1f + w * 0.2f; } });
    }
}
