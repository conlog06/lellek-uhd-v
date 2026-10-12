package gg.lellek.hud;

/** Laufzeit-Diagnose: Mixins setzen ihr Flag, sobald ihr Code einmal lief. Sichtbar im Mod-Menü. */
public class Diag {
    public static boolean fov, mouse, scroll, gamma, camera, turn, playerModel;
    public static String summary() {
        StringBuilder ok = new StringBuilder(), no = new StringBuilder();
        String[][] all = { { "Zoom-FOV", String.valueOf(fov) }, { "Maus", String.valueOf(mouse) }, { "Zoom-Mausrad", String.valueOf(scroll) }, { "Fullbright", String.valueOf(gamma) }, { "Kamera", String.valueOf(camera) }, { "Freelook", String.valueOf(turn) }, { "Emote-Pose", String.valueOf(playerModel) } };
        for (String[] a : all) (a[1].equals("true") ? ok : no).append(a[0]).append(' ');
        return "Aktiv: " + (ok.length() == 0 ? "-" : ok) + "| Nicht gegriffen: " + (no.length() == 0 ? "-" : no);
    }
}
