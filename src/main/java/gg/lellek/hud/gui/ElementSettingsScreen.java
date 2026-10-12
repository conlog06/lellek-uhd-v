package gg.lellek.hud.gui;

import gg.lellek.hud.LellekHud;
import gg.lellek.hud.element.HudElement;
import gg.lellek.hud.feature.Setting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/** Element-Einstellungen: sichtbar, Hintergrund, Größe, Farbwähler + Hex, eigene Optionen, Farb-Swatches. */
public class ElementSettingsScreen extends Screen {
    private final Screen parent; private final HudElement el;
    private ColorPicker picker; private EditBox hex; private int left, top;
    private final List<Setting.Color> swatches = new ArrayList<>(); private int selectedSwatch = -1;

    public ElementSettingsScreen(Screen parent, HudElement el) { super(Component.literal(el.name)); this.parent = parent; this.el = el; }
    private int currentColor() { return selectedSwatch < 0 ? el.color : swatches.get(selectedSwatch).value; }
    private void setCurrentColor(int c) { if (selectedSwatch < 0) el.color = c; else swatches.get(selectedSwatch).value = (c & 0xFFFFFF) | (swatches.get(selectedSwatch).value & 0xFF000000); }
    private String currentName() { return selectedSwatch < 0 ? "Hauptfarbe" : swatches.get(selectedSwatch).name; }

    @Override
    protected void init() {
        swatches.clear(); left = width / 2 - 150; top = 30;
        addRenderableWidget(Button.builder(Component.literal(vis()), b -> { el.enabled = !el.enabled; b.setMessage(Component.literal(vis())); }).bounds(left, top, 140, 20).build());
        addRenderableWidget(Button.builder(Component.literal(bgl()), b -> { el.background = !el.background; b.setMessage(Component.literal(bgl())); }).bounds(left + 150, top, 150, 20).build());
        addRenderableWidget(new AbstractSliderButton(left, top + 24, 300, 20, Component.literal("Größe: " + el.scale + " %"), (el.scale - 50) / 250.0) {
            @Override protected void updateMessage() { setMessage(Component.literal("Größe: " + el.scale + " %")); }
            @Override protected void applyValue() { el.scale = 50 + (int) Math.round(value * 250); }
        });
        picker = new ColorPicker(left, top + 62, currentColor());
        hex = new EditBox(font, left, top + 186, 80, 18, Component.literal("Hex")); hex.setMaxLength(6); syncHex(); addRenderableWidget(hex);
        addRenderableWidget(Button.builder(Component.literal("OK"), b -> applyHex()).bounds(left + 84, top + 185, 36, 20).build());
        int y = top + 62, rx = left + 150;
        for (Setting s : el.settings) {
            if (s instanceof Setting.Color c) { swatches.add(c); continue; }
            if (s instanceof Setting.Bool bs) addRenderableWidget(Button.builder(Component.literal(s.name + ": " + (bs.value ? "An" : "Aus")), b -> { bs.value = !bs.value; b.setMessage(Component.literal(s.name + ": " + (bs.value ? "An" : "Aus"))); }).bounds(rx, y, 150, 20).build());
            else if (s instanceof Setting.Int n) addRenderableWidget(new AbstractSliderButton(rx, y, 150, 20, Component.literal(s.name + ": " + n.value + " " + n.suffix), (n.value - n.min) / (double) (n.max - n.min)) {
                @Override protected void updateMessage() { setMessage(Component.literal(s.name + ": " + n.value + " " + n.suffix)); }
                @Override protected void applyValue() { n.value = n.min + (int) Math.round(value * (n.max - n.min)); }
            });
            else if (s instanceof Setting.Choice c) addRenderableWidget(Button.builder(Component.literal(s.name + ": " + c.current()), b -> { c.next(); b.setMessage(Component.literal(s.name + ": " + c.current())); }).bounds(rx, y, 150, 20).build());
            y += 22;
        }
        addRenderableWidget(Button.builder(Component.literal("Zurück"), b -> onClose()).bounds(width / 2 - 50, height - 26, 100, 20).build());
    }
    private String vis() { return "Sichtbar: " + (el.enabled ? "An" : "Aus"); }
    private String bgl() { return "Hintergrund: " + (el.background ? "An" : "Aus"); }
    private void syncHex() { hex.setValue(String.format("%06X", currentColor() & 0xFFFFFF)); }
    private void applyHex() { try { setCurrentColor(0xFF000000 | Integer.parseInt(hex.getValue().replace("#", ""), 16)); picker.setColor(currentColor()); } catch (NumberFormatException ignored) {} }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float pt) {
        super.extractRenderState(g, mx, my, pt);
        Gfx.centered(g, font, el.name, width / 2, 10, 0xFFFFB400);
        Gfx.text(g, font, "Farbe: " + currentName(), left, top + 50, 0xFFAAAAAA); picker.draw(g); Gfx.text(g, font, "#", left - 8, top + 191, 0xFFAAAAAA);
        int sx = left, sy = top + 210;
        swatch(g, sx, sy, el.color, selectedSwatch == -1, mx, my); sx += 16;
        for (int i = 0; i < swatches.size(); i++) { swatch(g, sx, sy, swatches.get(i).value | 0xFF000000, selectedSwatch == i, mx, my); sx += 16; }
        if (!swatches.isEmpty()) Gfx.text(g, font, "Swatch anklicken = Farbe wählen", left, sy + 16, 0xFF666666);
        int py = Math.max(top + 74 + el.settings.size() * 22, height - 50 - el.scaledHeight());
        Gfx.text(g, font, "Vorschau", left + 150, py - 12, 0xFFAAAAAA);
        int ox = el.x, oy = el.y; el.x = left + 150; el.y = py; el.draw(g); el.x = ox; el.y = oy;
    }
    private void swatch(GuiGraphicsExtractor g, int x, int y, int c, boolean sel, int mx, int my) { g.fill(x, y, x + 12, y + 12, sel ? 0xFFFFFFFF : (mx >= x && mx < x + 12 && my >= y && my < y + 12 ? 0xFFAAAAAA : 0xFF3B2E1C)); g.fill(x + 1, y + 1, x + 11, y + 11, c); }
    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean dbl) {
        int mx = (int) e.x(), my = (int) e.y(), sy = top + 210;
        if (my >= sy && my < sy + 12 && mx >= left && mx < left + 16 * (swatches.size() + 1)) { selectedSwatch = (mx - left) / 16 - 1; picker.setColor(currentColor()); syncHex(); return true; }
        if (e.button() == gg.lellek.hud.Mouse.LEFT && picker.mouseClicked(e.x(), e.y())) { setCurrentColor(picker.getColor()); syncHex(); return true; }
        return super.mouseClicked(e, dbl);
    }
    @Override
    public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) { picker.mouseDragged(e.x(), e.y()); setCurrentColor(picker.getColor()); syncHex(); return super.mouseDragged(e, dx, dy); }
    @Override
    public boolean mouseReleased(MouseButtonEvent e) { picker.mouseReleased(); return super.mouseReleased(e); }
    @Override
    public void onClose() { LellekHud.config.save(); minecraft.gui.setScreen(parent); }
    @Override
    public boolean isPauseScreen() { return false; }
}
