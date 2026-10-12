package gg.lellek.hud.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import gg.lellek.hud.cosmetic.CosmeticLayer;
import gg.lellek.hud.emote.EmoteState;
import gg.lellek.hud.emote.Pose;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Ganzkörper-Bewegung der Emotes (hoch/runter, drehen, Saltos, liegen) – Drehpunkt in Körpermitte (0,9 Blöcke). */
@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {
    private static final Pose lellekhud$pose = new Pose();

    @Inject(method = "setupRotations(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;FF)V", at = @At("TAIL"), require = 0)
    private void lellekhud$root(AvatarRenderState state, PoseStack pose, float bodyRot, float scale, CallbackInfo ci) {
        gg.lellek.hud.Diag.emoteRoot = true;
        EmoteState.Playing pl = EmoteState.forUuid(CosmeticLayer.uuidOf(state));
        if (pl == null) return;
        float t = pl.t(), w = pl.emote.weight(t);
        Pose p = lellekhud$pose.reset(); pl.emote.pose(t, p);
        if (p.rootX == 0 && p.rootY == 0 && p.rootZ == 0 && p.yaw == 0 && p.pitch == 0 && p.roll == 0) return;
        pose.translate(-p.rootX * w, p.rootY * w, -p.rootZ * w);
        if (p.yaw != 0) pose.mulPose(new org.joml.Matrix4f().rotation(Axis.YP.rotationDegrees(-p.yaw * w)));
        if (p.pitch != 0 || p.roll != 0) {
            pose.translate(0, 0.9f, 0);
            if (p.pitch != 0) pose.mulPose(new org.joml.Matrix4f().rotation(Axis.XP.rotationDegrees(-p.pitch * w)));
            if (p.roll != 0) pose.mulPose(new org.joml.Matrix4f().rotation(Axis.ZP.rotationDegrees(p.roll * w)));
            pose.translate(0, -0.9f, 0);
        }
    }
}
