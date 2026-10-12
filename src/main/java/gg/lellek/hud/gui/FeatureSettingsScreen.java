package gg.lellek.hud.gui;

import gg.lellek.hud.LellekHud;
import gg.lellek.hud.feature.Feature;
import gg.lellek.hud.feature.Features;
import gg.lellek.hud.feature.Setting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Einstellungen eines Features: Bool = Schalter, Zahl = Regler, Auswahl = Durchklicken, Farbe = Farbwähler. */
public class FeatureSettingsScreen extends Screen {
    private final Screen parent;
    private final Feature feature;
    private ColorPicker picker; private Setting.Color colorSetting;
    private final java.util.Map<EditBox, String> textLabels = new java.util.HashMap<>();

    public FeatureSettingsScreen(Screen parent, Feature f) { super(Component.literal(f.name)); this.parent = parent; this.feature = f; }

    @Override
    protected void init() {
        int left = width / 2 - 100, y = 44;
        addRenderableWidget(Button.builder(Component.literal("Aktiv: " + (feature.enabled ? "An" : "Aus")), b -> { feature.enabled = !feature.enabled; b.setMessage(Component.literal("Aktiv: " + (feature.enabled ? "An" : "Aus"))); }).bounds(left, y, 200, 20).build());
        y += 26;
        for (Setting s : feature.settings) {
            if (s instanceof Setting.Bool bs) addRenderableWidget(Button.builder(Component.literal(s.name + ": " + (bs.value ? "An" : "Aus")), b -> { bs.value = !bs.value; b.setMessage(Component.literal(s.name + ": " + (bs.value ? "An" : "Aus"))); }).bounds(left, y, 200, 20).build());
            else if (s instanceof Setting.Int n) addRenderableWidget(new AbstractSliderButton(left, y, 200, 20, Component.literal(s.name + ": " + n.value + " " + n.suffix), (n.value - n.min) / (double) (n.max - n.min)) {
                @Override protected void updateMessage() { setMessage(Component.literal(s.name + ": " + n.value + " " + n.suffix)); }
                @Override protected void applyValue() { n.value = n.min + (int) Math.round(value * (n.max - n.min)); }
            });
            else if (s instanceof Setting.Choice c) addRenderableWidget(Button.builder(Component.literal(s.name + ": " + c.current()), b -> { c.next(); b.setMessage(Component.literal(s.name + ": " + c.current())); }).bounds(left, y, 200, 20).build());
            else if (s instanceof Setting.Color col) { colorSetting = col; picker = new ColorPicker(left, y + 12, col.value); y += 130; }
            else if (s instanceof Setting.Text tx) { EditBox e = new EditBox(font, left, y + 10, 200, 18, Component.literal(s.name)); e.setMaxLength(200); e.setValue(tx.value); e.setResponder(v -> tx.value = v); addRenderableWidget(e); textLabels.put(e, s.name); y += 12; }
            y += 24;
        }
        addRenderableWidget(Button.builder(Component.literal("Zurück"), b -> onClose()).bounds(width / 2 - 50, height - 30, 100, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float pt) {
        super.extractRenderState(g, mx, my, pt);
        gg.lellek.hud.gui.Gfx.centered(g, font, feature.name, width / 2, 12, 0xFFFFB400);
        gg.lellek.hud.gui.Gfx.centered(g, font, feature.desc, width / 2, 24, 0xFFAAAAAA);
        if (picker != null) { gg.lellek.hud.gui.Gfx.text(g, font, colorSetting.name, picker.x, picker.y - 10, 0xFFAAAAAA); picker.draw(g); }
        for (var en : textLabels.entrySet()) gg.lellek.hud.gui.Gfx.text(g, font, en.getValue(), en.getKey().getX(), en.getKey().getY() - 10, 0xFFAAAAAA);
        if (feature.id.equals("crosshair") && picker != null) { gg.lellek.hud.gui.Gfx.text(g, font, "Vorschau", width / 2 + 40, picker.y - 10, 0xFFAAAAAA); Features.drawCrosshair(g, width / 2 + 60, picker.y + 20); }
    }
    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean dbl) {
        if (picker != null && e.button() == 0 && picker.mouseClicked(e.x(), e.y())) { colorSetting.value = picker.getColor(); return true; }
        return super.mouseClicked(e, dbl);
    }
    @Override
    public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) {
        if (picker != null) { picker.mouseDragged(e.x(), e.y()); colorSetting.value = picker.getColor(); }
        return super.mouseDragged(e, dx, dy);
    }
    @Override
    public boolean mouseReleased(MouseButtonEvent e) { if (picker != null) picker.mouseReleased(); return super.mouseReleased(e); }
    @Override
    public void onClose() { LellekHud.config.save(); minecraft.gui.setScreen(parent); }
    @Override
    public boolean isPauseScreen() { return false; }
}
