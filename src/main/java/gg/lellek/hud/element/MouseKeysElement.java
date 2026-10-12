package gg.lellek.hud.element;

import gg.lellek.hud.ClickTracker;
import gg.lellek.hud.feature.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** LMB / RMB als eigenes Element – frei platzierbar, eigene Farben, CPS. */
public class MouseKeysElement extends HudElement {
    private static final int KEY = 20, GAP = 2, W = 31;
    public MouseKeysElement() {
        super("mousekeys", "Maustasten (LMB/RMB)", BL, 70, 4, false);
        add(new Setting.Color("c_lmb", "Farbe LMB", 0x80000000)); add(new Setting.Color("c_rmb", "Farbe RMB", 0x80000000));
        add(new Setting.Color("pressed", "Farbe gedrückt", 0xC0FFB400)); add(new Setting.Color("text", "Text", 0xFFFFFFFF)); add(new Setting.Color("textpressed", "Text gedrückt", 0xFF1A1000));
        add(new Setting.Bool("cps", "CPS anzeigen", true)); add(new Setting.Choice("layout", "Anordnung", 0, "Nebeneinander", "Untereinander"));
    }
    public int getWidth() { return choiceOf("layout") == 0 ? W * 2 + GAP : W * 2; }
    public int getHeight() { return choiceOf("layout") == 0 ? KEY : KEY * 2 + GAP; }
    protected void render(GuiGraphicsExtractor g) {
        Options o = Minecraft.getInstance().options; boolean cps = boolOf("cps"); boolean col = choiceOf("layout") == 1; int w = col ? W * 2 : W;
        key(g, "LMB", 0, 0, o.keyAttack.isDown(), w, colorOf("c_lmb"), cps ? ClickTracker.leftCps() + " CPS" : null);
        key(g, "RMB", col ? 0 : W + GAP, col ? KEY + GAP : 0, o.keyUse.isDown(), w, colorOf("c_rmb"), cps ? ClickTracker.rightCps() + " CPS" : null);
    }
    private void key(GuiGraphicsExtractor g, String label, int x, int y, boolean down, int w, int idle, String sub) {
        g.fill(x, y, x + w, y + KEY, down ? colorOf("pressed") : idle); int tc = down ? colorOf("textpressed") : colorOf("text");
        if (sub == null) text(g, label, x + (w - font().width(label)) / 2, y + (KEY - 8) / 2, tc);
        else { text(g, label, x + (w - font().width(label)) / 2, y + 2, tc); text(g, sub, x + (w - font().width(sub)) / 2, y + 11, tc); }
    }
}
