package gg.lellek.hud.gui;

import gg.lellek.hud.waypoint.Waypoint;
import gg.lellek.hud.waypoint.Waypoints;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Wegpunkt anlegen oder bearbeiten */
public class WaypointEditScreen extends Screen {
    private final Screen parent; private final Waypoint wp; private final boolean isNew;
    private EditBox name, fx, fy, fz; private ColorPicker picker;
    public WaypointEditScreen(Screen parent, Waypoint wp, boolean isNew) { super(Component.literal("Wegpunkt")); this.parent = parent; this.wp = wp; this.isNew = isNew; }

    @Override
    protected void init() {
        int left = width / 2 - 130, top = 40;
        name = new EditBox(font, left, top + 10, 260, 20, Component.literal("Name")); name.setMaxLength(32); name.setValue(wp.name == null ? "" : wp.name);
        fx = new EditBox(font, left, top + 50, 80, 20, Component.literal("X")); fx.setValue(String.valueOf(wp.x));
        fy = new EditBox(font, left + 90, top + 50, 80, 20, Component.literal("Y")); fy.setValue(String.valueOf(wp.y));
        fz = new EditBox(font, left + 180, top + 50, 80, 20, Component.literal("Z")); fz.setValue(String.valueOf(wp.z));
        for (EditBox e : new EditBox[] { name, fx, fy, fz }) addRenderableWidget(e);
        setInitialFocus(name);
        picker = new ColorPicker(left, top + 90, wp.color);
        addRenderableWidget(Button.builder(Component.literal(isNew ? "Anlegen" : "Speichern"), b -> apply()).bounds(width / 2 - 104, height - 30, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Abbrechen"), b -> onClose()).bounds(width / 2 + 4, height - 30, 100, 20).build());
    }
    private void apply() {
        wp.name = name.getValue().trim().isEmpty() ? "Wegpunkt" : name.getValue().trim();
        try { wp.x = Integer.parseInt(fx.getValue().trim()); wp.y = Integer.parseInt(fy.getValue().trim()); wp.z = Integer.parseInt(fz.getValue().trim()); } catch (NumberFormatException ignored) {}
        wp.color = picker.getColor();
        if (isNew) Waypoints.add(wp); else Waypoints.save();
        minecraft.gui.setScreen(parent);
    }
    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float pt) {
        super.extractRenderState(g, mx, my, pt);
        int left = width / 2 - 130, top = 40;
        gg.lellek.hud.gui.Gfx.centered(g, font, isNew ? "Neuer Wegpunkt" : "Wegpunkt bearbeiten", width / 2, 14, 0xFFFFB400);
        gg.lellek.hud.gui.Gfx.text(g, font, "Name", left, top, 0xFFAAAAAA); gg.lellek.hud.gui.Gfx.text(g, font, "X", left, top + 40, 0xFFAAAAAA); gg.lellek.hud.gui.Gfx.text(g, font, "Y", left + 90, top + 40, 0xFFAAAAAA); gg.lellek.hud.gui.Gfx.text(g, font, "Z", left + 180, top + 40, 0xFFAAAAAA);
        gg.lellek.hud.gui.Gfx.text(g, font, "Farbe", left, top + 80, 0xFFAAAAAA);
        picker.draw(g);
    }
    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean dbl) { if (e.button() == gg.lellek.hud.Mouse.LEFT && picker.mouseClicked(e.x(), e.y())) return true; return super.mouseClicked(e, dbl); }
    @Override
    public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) { picker.mouseDragged(e.x(), e.y()); return super.mouseDragged(e, dx, dy); }
    @Override
    public boolean mouseReleased(MouseButtonEvent e) { picker.mouseReleased(); return super.mouseReleased(e); }
    @Override
    public void onClose() { minecraft.gui.setScreen(parent); }
    @Override
    public boolean isPauseScreen() { return false; }
}
