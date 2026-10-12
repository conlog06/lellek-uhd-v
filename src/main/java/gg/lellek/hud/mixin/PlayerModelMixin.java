package gg.lellek.hud.mixin;

import gg.lellek.hud.cosmetic.CosmeticLayer;
import gg.lellek.hud.emote.EmoteState;
import gg.lellek.hud.emote.Pose;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Emote-Pose nach der normalen Animation aufsetzen (require=0: fehlt das Ziel, gibt es nur keine Emotes). */
@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin {
    private final Pose lellekhud$pose = new Pose();

    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void lellekhud$emote(AvatarRenderState state, CallbackInfo ci) {
        gg.lellek.hud.Diag.playerModel = true;
        EmoteState.Playing pl = EmoteState.forUuid(CosmeticLayer.uuidOf(state));
        if (pl == null) return;
        HumanoidModel<?> m = (HumanoidModel<?>) (Object) this;
        float t = pl.t(), w = pl.emote.weight(t), r = (float) Math.PI / 180f;
        Pose p = lellekhud$pose.reset(); pl.emote.pose(t, p);
        m.head.xRot = mix(m.head.xRot, p.headX * r, w); m.head.yRot = mix(m.head.yRot, p.headY * r, w);
        m.body.xRot = mix(m.body.xRot, p.bodyX * r, w); m.body.yRot = mix(m.body.yRot, p.bodyY * r, w);
        m.rightArm.xRot = mix(m.rightArm.xRot, p.rArmX * r, w); m.rightArm.yRot = mix(m.rightArm.yRot, p.rArmY * r, w); m.rightArm.zRot = mix(m.rightArm.zRot, p.rArmZ * r, w);
        m.leftArm.xRot = mix(m.leftArm.xRot, p.lArmX * r, w); m.leftArm.yRot = mix(m.leftArm.yRot, p.lArmY * r, w); m.leftArm.zRot = mix(m.leftArm.zRot, p.lArmZ * r, w);
        m.rightLeg.xRot = mix(m.rightLeg.xRot, p.rLegX * r, w); m.rightLeg.zRot = mix(m.rightLeg.zRot, p.rLegZ * r, w);
        m.leftLeg.xRot = mix(m.leftLeg.xRot, p.lLegX * r, w); m.leftLeg.zRot = mix(m.leftLeg.zRot, p.lLegZ * r, w);
        m.hat.xRot = m.head.xRot; m.hat.yRot = m.head.yRot; m.hat.zRot = m.head.zRot; m.hat.x = m.head.x; m.hat.y = m.head.y; m.hat.z = m.head.z;
        float dy = p.offsetY * w * 16f; // Modelleinheiten: 16 = 1 Block
        for (net.minecraft.client.model.geom.ModelPart part : new net.minecraft.client.model.geom.ModelPart[] { m.head, m.hat, m.body, m.rightArm, m.leftArm, m.rightLeg, m.leftLeg }) part.y += dy;
    }
    private static float mix(float a, float b, float w) { return a + (b - a) * w; }
}
