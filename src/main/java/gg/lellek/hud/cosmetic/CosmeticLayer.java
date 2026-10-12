package gg.lellek.hud.cosmetic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import gg.lellek.hud.LellekHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Pose;

import java.util.HashMap;
import java.util.Map;

/**
 * Zeichnet das gewählte Cosmetic am Spieler. Eigener Spieler aus der Config, andere aus `others`
 * (wird später vom Cosmetics-Server gefüllt).
 */
public class CosmeticLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    public static final Map<java.util.UUID, String> others = new HashMap<>();

    @SuppressWarnings("unchecked")
    public CosmeticLayer(RenderLayerParent<?, ?> parent) { super((RenderLayerParent<AvatarRenderState, PlayerModel>) parent); }

    private static Cosmetic forState(AvatarRenderState s) {
        Minecraft mc = Minecraft.getInstance();
        java.util.UUID uuid = uuidOf(s);
        if (mc.player != null && mc.player.getUUID().equals(uuid)) return Cosmetics.byId(LellekHud.config.cosmetic);
        String id = uuid == null ? null : others.get(uuid);
        return id == null ? null : Cosmetics.byId(id);
    }

    /** UUID aus dem Render-State: über die Spielerliste per Namensschild. In 26.x hat der State evtl. ein id-Feld – dann hier vereinfachen. */
    public static java.util.UUID uuidOf(AvatarRenderState s) {
        Minecraft mc = Minecraft.getInstance(); if (s.nameTag == null || mc.getConnection() == null) return null;
        String name = s.nameTag.getString();
        for (net.minecraft.client.multiplayer.PlayerInfo i : mc.getConnection().getOnlinePlayers()) if (i.getProfile().name().equals(name)) return i.getProfile().id();
        return null;
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector out, int light, AvatarRenderState s, float yRot, float xRot) {
        Cosmetic c = forState(s);
        if (c == null || s.isInvisible) return;
        float t = s.ageInTicks / 20f;
        net.minecraft.resources.Identifier tex = c.textureFor(uuidOf(s));
        if (tex == null) return;
        pose.pushPose();
        if (s.pose == Pose.CROUCHING) { pose.translate(0, 0.2f, 0); pose.mulPose(Axis.XP.rotationDegrees(28.65f)); }
        c.render(pose, out, light, s, t, tex);
        pose.popPose();
    }
}
