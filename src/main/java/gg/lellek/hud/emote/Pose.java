package gg.lellek.hud.emote;

/**
 * Emote-Pose in Grad (Gelenke) bzw. Blöcken (root*). Alles ist relativ: 0 = gerade stehen, Arme hängen.
 * Arme: x negativ = nach vorn/oben (-180 = gerade hoch), z positiv = rechter Arm nach außen (links umgekehrt).
 * Körper: bx = Vorbeugen aus der Hüfte, by = Oberkörper drehen, bz = seitlich neigen.
 * Root bewegt den ganzen Spieler: rootY hoch/runter, rootZ vor, rootX seitlich, yaw drehen, pitch kippen (+ = vorne), roll seitlich.
 */
public class Pose {
    public float hx, hy, hz, bx, by, bz;
    public float rax, ray, raz, lax, lay, laz;
    public float rlx, rly, rlz, llx, lly, llz;
    public float rootX, rootY, rootZ, yaw, pitch, roll;
    public Pose reset() { hx = hy = hz = bx = by = bz = rax = ray = raz = lax = lay = laz = rlx = rly = rlz = llx = lly = llz = rootX = rootY = rootZ = yaw = pitch = roll = 0; return this; }
    /** Beide Arme gleich (gespiegelt) */
    public Pose arms(float x, float y, float z) { rax = x; ray = y; raz = z; lax = x; lay = -y; laz = -z; return this; }
    public Pose legs(float x, float y, float z) { rlx = x; rly = y; rlz = z; llx = x; lly = -y; llz = -z; return this; }
}
