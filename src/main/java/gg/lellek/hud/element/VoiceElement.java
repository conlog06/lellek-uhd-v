package gg.lellek.hud.element;

import gg.lellek.hud.feature.Mods;
import gg.lellek.hud.voice.VoiceClient;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;

/** Voice-Status: Mikro an/aus, wer gerade spricht, wie viele in Hörweite. */
public class VoiceElement extends HudElement {
    public VoiceElement() { super("voice", "Voice-Chat", BL, 4, 92, false); }
    public int getWidth() { return 110; }
    public int getHeight() { return 14 + Math.max(0, Math.min(5, VoiceClient.get().speaking().size())) * 10; }
    protected void render(GuiGraphicsExtractor g) {
        VoiceClient v = VoiceClient.get();
        bg(g, getWidth(), getHeight());
        if (!Mods.VOICE.enabled) { text(g, "Voice aus", PAD, PAD, 0xFF888888); return; }
        if (!v.isConnected()) { text(g, "Voice: verbindet…", PAD, PAD, 0xFF888888); return; }
        g.fill(PAD, PAD + 1, PAD + 6, PAD + 7, v.talking ? 0xFF7BD88F : 0xFF555555);
        text(g, v.talking ? "Du sprichst" : "B halten · " + v.nearby() + " in der Nähe", PAD + 10, PAD, v.talking ? color : WHITE);
        List<String> sp = v.speaking(); int y = 14;
        for (int i = 0; i < Math.min(5, sp.size()); i++) { g.fill(PAD, y + 1, PAD + 6, y + 7, 0xFF7BD88F); text(g, sp.get(i), PAD + 10, y, WHITE); y += 10; }
    }
}
