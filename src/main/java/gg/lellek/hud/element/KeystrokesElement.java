package gg.lellek.hud.element;

import gg.lellek.hud.ClickTracker;
import gg.lellek.hud.feature.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** W / A S D / LMB RMB / Leertaste – jede Taste mit eigener Farbe, Farbe beim Drücken, optional CPS in LMB/RMB. */
public class KeystrokesElement extends HudElement {
    private static final int KEY = 20, GAP = 2;
    public KeystrokesElement() {
        super("keystrokes", "Keystrokes", BL, 4, 4);
        for (String k : new String[] { "W", "A", "S", "D", "LMB", "RMB", "Leertaste" }) add(new Setting.Color("c_" + k.toLowerCase(), "Farbe " + k, 0x80000000));
        add(new Setting.Color("pressed", "Farbe gedrückt", 0xC0FFB400)); add(new Setting.Color("text", "Text", 0xFFFFFFFF)); add(new Setting.Color("textpressed", "Text gedrückt", 0xFF1A1000)); add(new Setting.Bool("cps", "CPS in LMB/RMB", true)); add(new Setting.Bool("mouse", "Maustasten anzeigen", true)); add(new Setting.Bool("space", "Leertaste anzeigen", true));
    }
    public int getWidth() { return KEY * 3 + GAP * 2; }
    public int getHeight() { int h = KEY * 2 + GAP; if (boolOf("mouse")) h += KEY + GAP; if (boolOf("space")) h += 10 + GAP; return h + GAP; }
    protected void render(GuiGraphicsExtractor g) {
        Options o = Minecraft.getInstance().options;
        int c2 = KEY + GAP, c3 = (KEY + GAP) * 2, r2 = KEY + GAP, r3 = r2 + KEY + GAP, r4 = r3 + KEY + GAP;
        key(g, "W", c2, 0, o.keyUp.isDown(), KEY, colorOf("c_w"), null); key(g, "A", 0, r2, o.keyLeft.isDown(), KEY, colorOf("c_a"), null);
        key(g, "S", c2, r2, o.keyDown.isDown(), KEY, colorOf("c_s"), null); key(g, "D", c3, r2, o.keyRight.isDown(), KEY, colorOf("c_d"), null);
        int half = (getWidth() - GAP) / 2; boolean cps = boolOf("cps"); int row = r3;
        if (boolOf("mouse")) {
            key(g, "LMB", 0, row, o.keyAttack.isDown(), half, colorOf("c_lmb"), cps ? ClickTracker.leftCps() + " CPS" : null);
            key(g, "RMB", half + GAP, row, o.keyUse.isDown(), half, colorOf("c_rmb"), cps ? ClickTracker.rightCps() + " CPS" : null);
            row += KEY + GAP;
        }
        if (boolOf("space")) { boolean jump = o.keyJump.isDown(); g.fill(0, row, getWidth(), row + 10, jump ? colorOf("pressed") : colorOf("c_leertaste")); g.fill(6, row + 4, getWidth() - 6, row + 6, jump ? colorOf("textpressed") : colorOf("text")); }
    }
    private void key(GuiGraphicsExtractor g, String label, int x, int y, boolean down, int w, int idle, String sub) {
        g.fill(x, y, x + w, y + KEY, down ? colorOf("pressed") : idle); int tc = down ? colorOf("textpressed") : colorOf("text");
        if (sub == null) text(g, label, x + (w - font().width(label)) / 2, y + (KEY - 8) / 2, tc);
        else { text(g, label, x + (w - font().width(label)) / 2, y + 2, tc); text(g, sub, x + (w - font().width(sub)) / 2, y + 11, tc); }
    }
}
