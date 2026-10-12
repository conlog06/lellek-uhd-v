package gg.lellek.hud.gui;

import gg.lellek.hud.waypoint.Waypoint;
import gg.lellek.hud.waypoint.Waypoints;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;

/** Liste der Wegpunkte dieser Welt: sichtbar, bearbeiten, teilen, löschen. */
public class WaypointsScreen extends Screen {
    private static final int ROW = 24;
    private final Screen parent; private int scroll;
    public WaypointsScreen(Screen parent) { super(Component.literal("Wegpunkte")); this.parent = parent; }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.literal("+ Neu (hier)"), b -> minecraft.gui.setScreen(new WaypointEditScreen(this, new Waypoint("", Waypoints.currentWorld(), minecraft.player.getBlockX(), minecraft.player.getBlockY(), minecraft.player.getBlockZ(), 0xFFFFB400), true))).bounds(width / 2 - 104, height - 28, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Zurück"), b -> onClose()).bounds(width / 2 + 4, height - 28, 100, 20).build());
    }
    private int left() { return width / 2 - 170; }
    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float pt) {
        super.extractRenderState(g, mouseX, mouseY, pt);
        gg.lellek.hud.gui.Gfx.centered(g, font, "Wegpunkte", width / 2, 12, 0xFFFFB400);
        gg.lellek.hud.gui.Gfx.centered(g, font, Waypoints.currentWorld().replace("#", " · "), width / 2, 24, 0xFF888888);
        List<Waypoint> l = Waypoints.here(); int x = left();
        if (l.isEmpty()) gg.lellek.hud.gui.Gfx.centered(g, font, "Noch keine Wegpunkte hier. Taste N setzt einen an deiner Position.", width / 2, height / 2, 0xFFAAAAAA);
        for (int i = 0; i < l.size(); i++) {
            Waypoint w = l.get(i); int y = 40 - scroll + i * ROW; if (y < 36 || y > height - 40) continue;
            boolean hov = mouseX >= x && mouseX < x + 340 && mouseY >= y && mouseY < y + ROW - 2;
            g.fill(x, y, x + 340, y + ROW - 2, hov ? 0xFF32271A : 0xFF261E13); g.fill(x, y, x + 3, y + ROW - 2, w.color);
            double d = minecraft.player == null ? 0 : Math.hypot(w.x + 0.5 - minecraft.player.getX(), w.z + 0.5 - minecraft.player.getZ());
            gg.lellek.hud.gui.Gfx.text(g, font, w.name, x + 10, y + 7, w.visible ? 0xFFFFFFFF : 0xFF777777);
            gg.lellek.hud.gui.Gfx.text(g, font, w.x + " " + w.y + " " + w.z + "  " + (int) d + "m", x + 130, y + 7, 0xFFAAAAAA);
            btn(g, x + 236, y + 3, "\u25cf", w.visible ? 0xFF7BD88F : 0xFF555555, mouseX, mouseY); btn(g, x + 262, y + 3, "\u270e", 0xFFFFFFFF, mouseX, mouseY);
            btn(g, x + 288, y + 3, "\u2192", 0xFFFFFFFF, mouseX, mouseY); btn(g, x + 314, y + 3, "\u2716", 0xFFF07C7C, mouseX, mouseY);
        }
        gg.lellek.hud.gui.Gfx.centered(g, font, "\u25cf sichtbar   \u270e bearbeiten   \u2192 im Chat teilen   \u2716 löschen", width / 2, height - 42, 0xFF666666);
    }
    private void btn(GuiGraphicsExtractor g, int x, int y, String s, int color, int mx, int my) { boolean h = mx >= x && mx < x + 22 && my >= y && my < y + 18; g.fill(x, y, x + 22, y + 18, h ? 0xFF3E3122 : 0xFF1F1810); gg.lellek.hud.gui.Gfx.centered(g, font, s, x + 11, y + 5, color); }
    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean dbl) {
        if (super.mouseClicked(e, dbl)) return true;
        int mouseX = (int) e.x(), mouseY = (int) e.y(); List<Waypoint> l = Waypoints.here(); int x = left();
        for (int i = 0; i < l.size(); i++) {
            int y = 40 - scroll + i * ROW; if (mouseY < y + 3 || mouseY >= y + 21) continue; Waypoint w = l.get(i);
            if (mouseX >= x + 236 && mouseX < x + 258) { w.visible = !w.visible; Waypoints.save(); }
            else if (mouseX >= x + 262 && mouseX < x + 284) minecraft.gui.setScreen(new WaypointEditScreen(this, w, false));
            else if (mouseX >= x + 288 && mouseX < x + 310) { if (minecraft.getConnection() != null) minecraft.getConnection().sendChat("[Wegpunkt] " + w.name + ": " + w.x + " " + w.y + " " + w.z); }
            else if (mouseX >= x + 314 && mouseX < x + 336) Waypoints.remove(w);
            return true;
        }
        return false;
    }
    @Override
    public boolean mouseScrolled(double mx, double my, double dx, double dy) { scroll = Math.max(0, Math.min(Math.max(0, Waypoints.here().size() * ROW - (height - 90)), scroll - (int) Math.signum(dy) * 24)); return true; }
    @Override
    public void onClose() { minecraft.gui.setScreen(parent); }
    @Override
    public boolean isPauseScreen() { return false; }
}
