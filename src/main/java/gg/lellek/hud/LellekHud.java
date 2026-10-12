package gg.lellek.hud;

import com.mojang.blaze3d.platform.InputConstants;
import gg.lellek.hud.cosmetic.CosmeticLayer;
import gg.lellek.hud.feature.Features;
import gg.lellek.hud.gui.CosmeticWheelScreen;
import gg.lellek.hud.gui.HudEditorScreen;
import gg.lellek.hud.gui.ModMenuScreen;
import gg.lellek.hud.gui.WaypointsScreen;
import gg.lellek.hud.gui.FullMapScreen;
import gg.lellek.hud.gui.EmotesScreen;
import gg.lellek.hud.emote.EmoteState;
import gg.lellek.hud.gui.WaypointEditScreen;
import gg.lellek.hud.waypoint.Waypoint;
import gg.lellek.hud.waypoint.Waypoints;
import gg.lellek.hud.waypoint.WaypointRenderer;
import gg.lellek.hud.feature.Perspective;
import gg.lellek.hud.feature.DamageIndicators;
import gg.lellek.hud.feature.Extras;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.world.InteractionResult;
import gg.lellek.hud.spotify.SpotifyClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

/**
 * LellekHUD – das HUD des LellekClient für Minecraft 26.3 (Fabric, Mojang-Namen).
 * Rechte Shift-Taste = HUD-Editor, X = Cosmetics-Rad, C = Zoom.
 */
public class LellekHud implements ClientModInitializer {
    public static final String MODID = "lellekhud";
    public static final String CLIENT_NAME = "LellekClient";
    public static final String MC_VERSION = "26.3";
    public static final String VERSION = "1.4.1";
    public static final Identifier LOGO = Identifier.fromNamespaceAndPath(MODID, "textures/gui/logo.png");

    public static HudConfig config;
    /** Aktueller Screen (mc.screen gibt es seit 26.1 nicht mehr) */
    public static net.minecraft.client.gui.screens.Screen screen(Minecraft mc) { return mc.gui.screen(); }
    public static void log(String m) { org.slf4j.LoggerFactory.getLogger("LellekHUD").info(m); }
    public static Features features;
    public static KeyMapping editorKey, menuKey, zoomKey, cosmeticKey, sneakKey, perspectiveKey, waypointKey, waypointListKey, voiceKey, emoteKey, stopwatchKey, spotifyPlayKey, spotifyNextKey, spotifyPrevKey;
    private static final DamageIndicators damage = new DamageIndicators(); private static final Extras extras = new Extras();
    private int titleTick;

    @Override
    public void onInitializeClient() {
        config = HudConfig.load();
        Waypoints.init();
        editorKey = key("HUD-Editor öffnen", InputConstants.KEY_RSHIFT);
        menuKey = key("Mod-Menü", InputConstants.KEY_RCONTROL);
        zoomKey = key("Zoom (halten)", InputConstants.KEY_C);
        sneakKey = key("ToggleSneak", InputConstants.KEY_V);
        perspectiveKey = key("Perspektive (halten)", InputConstants.KEY_F6);
        waypointKey = key("Wegpunkt setzen", InputConstants.KEY_N);
        waypointListKey = key("Karte öffnen", InputConstants.KEY_M);
        voiceKey = key("Voice: Sprechen (halten)", InputConstants.KEY_B);
        emoteKey = key("Emotes", InputConstants.KEY_G);
        stopwatchKey = key("Stoppuhr Start/Stopp", InputConstants.KEY_END);
        cosmeticKey = key("Cosmetics-Rad", InputConstants.KEY_X);
        spotifyPlayKey = key("Spotify Play/Pause", InputConstants.KEY_NUMPAD5);
        spotifyNextKey = key("Spotify Nächster Titel", InputConstants.KEY_NUMPAD6);
        spotifyPrevKey = key("Spotify Vorheriger Titel", InputConstants.KEY_NUMPAD4);

        features = new Features();
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MODID, "hud"), HudRenderer::draw);
        HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, orig -> (g, d) -> {
            Minecraft mc = Minecraft.getInstance();
            if (gg.lellek.hud.feature.Mods.CROSSHAIR.enabled && mc.options.getCameraType().isFirstPerson() && !mc.getDebugOverlay().showDebugScreen())
                gg.lellek.hud.feature.Features.drawCrosshair(g, g.guiWidth() / 2, g.guiHeight() / 2);
            else callHudElement(orig, g, d);
        });
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> { if (level.isClientSide()) features.onAttack(); return InteractionResult.PASS; });
        ClientReceiveMessageEvents.MODIFY_GAME.register((message, overlay) -> overlay ? message : features.onChat(message));
        LivingEntityRenderLayerRegistrationCallback.EVENT.register((type, renderer, helper, ctx) -> {
            if (type == net.minecraft.world.entity.EntityTypes.PLAYER) helper.register(new CosmeticLayer(renderer));
        });
        SpotifyClient.get().start();
        gg.lellek.hud.gui.PauseHub.install();
        gg.lellek.hud.gui.LellekTitleScreen.install();
        WaypointRenderer.install(); damage.install(); extras.install(); gg.lellek.hud.feature.Features26.install(); gg.lellek.hud.feature.Extras26.install();
        if (gg.lellek.hud.feature.Mods.COSMETIC_SYNC.enabled) gg.lellek.hud.cosmetic.CosmeticSync.requestOwnCape();

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (editorKey.consumeClick()) mc.gui.setScreen(new HudEditorScreen());
            while (menuKey.consumeClick()) mc.gui.setScreen(new ModMenuScreen(null));
            while (cosmeticKey.consumeClick()) if (LellekHud.screen(mc) == null) mc.gui.setScreen(new CosmeticWheelScreen());
            while (spotifyPlayKey.consumeClick()) SpotifyClient.get().togglePlay();
            while (spotifyNextKey.consumeClick()) SpotifyClient.get().next();
            while (spotifyPrevKey.consumeClick()) SpotifyClient.get().previous();
            gg.lellek.hud.gui.LellekTitleScreen.tick(mc);
            ClickTracker.tick(mc); features.tick(mc); Perspective.tick(mc); damage.tick(mc); extras.tick(mc); gg.lellek.hud.cosmetic.CosmeticSync.tick(mc); gg.lellek.hud.voice.VoiceClient.get().tick(mc);
            while (waypointKey.consumeClick()) if (mc.player != null && LellekHud.screen(mc) == null) mc.gui.setScreen(new WaypointEditScreen(null, new Waypoint("", Waypoints.currentWorld(), mc.player.getBlockX(), mc.player.getBlockY(), mc.player.getBlockZ(), 0xFFFFB400), true));
            while (waypointListKey.consumeClick()) if (LellekHud.screen(mc) == null) mc.gui.setScreen(new FullMapScreen());
            while (emoteKey.consumeClick()) if (LellekHud.screen(mc) == null) { if (EmoteState.local() != null) EmoteState.stop(); else mc.gui.setScreen(new EmotesScreen(null)); }
            EmoteState.tick(mc);
            while (stopwatchKey.consumeClick()) gg.lellek.hud.element.StopwatchElement.toggle();
            gg.lellek.hud.element.SessionElement.tick(mc); gg.lellek.hud.map.MapStore.tick(mc); gg.lellek.hud.feature.Extras26.tick(mc);
            if (++titleTick % 40 == 0) mc.getWindow().setTitle(CLIENT_NAME + " " + MC_VERSION);
        });
    }

    private static java.lang.reflect.Method hudElementMethod;
    /** Ruft die einzige abstrakte Methode des Fabric-HudElement auf – heißt je nach Version render/extract. */
    private static void callHudElement(Object element, Object g, Object d) {
        try {
            if (hudElementMethod == null) for (java.lang.reflect.Method m : net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement.class.getMethods()) if (java.lang.reflect.Modifier.isAbstract(m.getModifiers()) && m.getParameterCount() == 2) { hudElementMethod = m; break; }
            if (hudElementMethod != null) hudElementMethod.invoke(element, g, d);
        } catch (Exception ignored) {}
    }

    private static KeyMapping key(String name, int code) {
        return KeyMappingHelper.registerKeyMapping(new KeyMapping(name, InputConstants.Type.KEYBOARD, code, KeyMapping.Category.MISC));
    }
}
