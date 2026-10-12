package gg.lellek.hud.gui;

import gg.lellek.hud.spotify.SpotifyClient;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;

/** ESC-Menü mit LellekClient-Kopf: Schriftzug, Mod-Menü-Button, Icon-Leiste. */
public class PauseHub {
    private static final int HEAD = 96;

    public static void install() {
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (!(screen instanceof PauseScreen)) return;
            int shift = Math.max(0, HEAD - (h / 4 + 8));
            for (AbstractWidget b : Screens.getWidgets(screen)) b.setY(b.getY() + shift);
            int cx = w / 2;
            var buttons = Screens.getWidgets(screen);
            buttons.add(Button.builder(Component.literal("MOD-MENÜ"), b -> client.gui.setScreen(new ModMenuScreen(screen))).bounds(cx - 100, 34, 200, 20).build());
            String[] icons = { "\u270e", "\u2726", "\u266a", "\u2699", "\u2691", "\u263a" };
            Runnable[] acts = { () -> client.gui.setScreen(new HudEditorScreen()), () -> client.gui.setScreen(new CosmeticsScreen(screen)), () -> SpotifyClient.get().togglePlay(), () -> client.gui.setScreen(new ModMenuScreen(screen)), () -> client.gui.setScreen(new WaypointsScreen(screen)), () -> client.gui.setScreen(new EmotesScreen(screen)) };
            String[] tips = { "HUD-Editor", "Cosmetics", "Spotify Play/Pause", "Mods", "Wegpunkte", "Emotes" };
            for (int i = 0; i < icons.length; i++) { final int k = i; buttons.add(Button.builder(Component.literal(icons[i]), b -> acts[k].run()).bounds(cx - 94 + i * 32, 60, 28, 20).tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(tips[i]))).build()); }
            ScreenEvents.afterExtract(screen).register((s, g, mx, my, pt) -> {
                g.pose().pushMatrix(); g.pose().translate(w / 2f, 12); g.pose().scale(1.6f, 1.6f);
                var font = Minecraft.getInstance().font;
                String l = "LELLEK", r = "CLIENT"; int lw = font.width(l), rw = font.width(r);
                gg.lellek.hud.gui.Gfx.text(g, font, l, -(lw + rw + 14) / 2, 0, 0xFFFFFFFF);
                gg.lellek.hud.gui.Gfx.text(g, font, "\u26a1", -(lw + rw + 14) / 2 + lw + 3, 0, 0xFFFFB400);
                gg.lellek.hud.gui.Gfx.text(g, font, r, -(lw + rw + 14) / 2 + lw + 14, 0, 0xFFFFB400);
                g.pose().popMatrix();
            });
        });
    }
}
