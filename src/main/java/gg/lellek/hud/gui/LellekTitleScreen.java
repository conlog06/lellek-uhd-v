package gg.lellek.hud.gui;

import gg.lellek.hud.LellekHud;
import gg.lellek.hud.feature.Mods;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;

/**
 * Eigener Titelbildschirm im LellekClient-Look – ohne Mixin: Sobald Vanilla sein Hauptmenü öffnet,
 * tauschen wir es im nächsten Tick gegen diesen Screen. Buttons führen zu den Vanilla-Screens.
 */
public class LellekTitleScreen extends Screen {
    private static boolean pending;
    public LellekTitleScreen() { super(Component.literal("LellekClient")); }

    public static void install() {
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> { if (Mods.MENU.enabled && screen.getClass() == TitleScreen.class) pending = true; });
    }
    /** Im Client-Tick aufrufen */
    public static void tick(Minecraft mc) {
        if (!pending) return; pending = false;
        if (Mods.MENU.enabled && LellekHud.screen(mc) instanceof TitleScreen) mc.gui.setScreen(new LellekTitleScreen());
    }

    @Override
    protected void init() {
        int cx = width / 2, y = height / 4 + 48;
        addRenderableWidget(Button.builder(Component.literal("Einzelspieler"), b -> minecraft.gui.setScreen(new SelectWorldScreen(this))).bounds(cx - 100, y, 200, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Mehrspieler"), b -> minecraft.gui.setScreen(new JoinMultiplayerScreen(this))).bounds(cx - 100, y + 24, 200, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Mod-Menü"), b -> minecraft.gui.setScreen(new ModMenuScreen(this))).bounds(cx - 100, y + 48, 98, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Cosmetics"), b -> minecraft.gui.setScreen(new CosmeticsScreen(this))).bounds(cx + 2, y + 48, 98, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Optionen…"), b -> minecraft.gui.setScreen(new OptionsScreen(this, minecraft.options))).bounds(cx - 100, y + 84, 98, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Spiel beenden"), b -> minecraft.stop()).bounds(cx + 2, y + 84, 98, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Vanilla-Menü"), b -> { Mods.MENU.enabled = false; LellekHud.config.save(); minecraft.gui.setScreen(new TitleScreen()); }).bounds(width - 92, height - 24, 88, 18).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float pt) {
        LellekMenuBackground.draw(g);
        LellekMenuBackground.drawLogo(g, width);
        super.extractRenderState(g, mx, my, pt);
        String user = minecraft.getUser().getName();
        Gfx.text(g, font, "Angemeldet als " + user, width - font.width("Angemeldet als " + user) - 4, 4, 0xFFB0A38E);
    }
    @Override
    public boolean shouldCloseOnEsc() { return false; }
}
