package gg.lellek.hud.gui;

import gg.lellek.hud.LellekHud;
import gg.lellek.hud.element.HudElement;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** HUD-Editor: ziehen = verschieben (rastet ein), Mausrad = Größe, Rechtsklick = Einstellungen, ESC = speichern. */
public class HudEditorScreen extends Screen {
    private final HudLayoutEditor editor = new HudLayoutEditor();

    public HudEditorScreen() { super(Component.literal("LellekClient HUD-Editor")); }

    @Override
    protected void init() {
        int cx = width / 2;
        addRenderableWidget(Button.builder(Component.literal("Mod-Menü"), b -> minecraft.gui.setScreen(new ModMenuScreen(this))).bounds(cx - 103, height - 28, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Zurücksetzen"), b -> LellekHud.config.reset()).bounds(cx + 3, height - 28, 100, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float pt) {
        super.extractRenderState(g, mouseX, mouseY, pt);
        editor.render(g, font, width, height, mouseX, mouseY);
        Gfx.centered(g, font, "LellekClient HUD-Editor", width / 2, 8, 0xFFFFB400);
        Gfx.centered(g, font, "Ziehen = verschieben   Mausrad = Größe   Rechtsklick = Einstellungen   ESC = speichern", width / 2, 20, 0xFFAAAAAA);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean doubleClick) {
        if (super.mouseClicked(e, doubleClick)) return true;
        HudElement settings = editor.click((int) e.x(), (int) e.y(), e.button());
        if (settings != null) { minecraft.gui.setScreen(new ElementSettingsScreen(this, settings)); return true; }
        return editor.isDragging();
    }
    @Override
    public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) { return editor.drag((int) e.x(), (int) e.y(), width, height) || super.mouseDragged(e, dx, dy); }
    @Override
    public boolean mouseScrolled(double mx, double my, double sdx, double sdy) { return editor.scroll((int) mx, (int) my, sdy, width, height) || super.mouseScrolled(mx, my, sdx, sdy); }
    @Override
    public boolean mouseReleased(MouseButtonEvent e) { editor.release(); return super.mouseReleased(e); }
    @Override
    public void onClose() { LellekHud.config.save(); super.onClose(); }
    @Override
    public boolean isPauseScreen() { return false; }
}
