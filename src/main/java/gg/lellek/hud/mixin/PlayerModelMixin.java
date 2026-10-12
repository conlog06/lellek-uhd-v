package gg.lellek.hud.mixin;

import gg.lellek.hud.cosmetic.CosmeticLayer;
import gg.lellek.hud.emote.EmoteState;
import gg.lellek.hud.emote.Pose;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Emote-Pose nach der normalen Animation. Der Oberkörper kippt um die Hüfte (nicht um den Hals),
 * Kopf und Arme wandern mit. Hut/Ärmel/Jacke sind in 26.x Kinder ihrer Körperteile und laufen automatisch mit.
 */
@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin {
    private final Pose lellekhud$pose = new Pose();

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At("TAIL"), require = 0)
    private void lellekhud$emote(AvatarRenderState state, CallbackInfo ci) {
        gg.lellek.hud.Diag.playerModel = true;
        EmoteState.Playing pl = EmoteState.forUuid(CosmeticLayer.uuidOf(state));
        if (pl == null) return;
        HumanoidModel<?> m = (HumanoidModel<?>) (Object) this;
        float t = pl.t(), w = pl.emote.weight(t), r = (float) Math.PI / 180f;
        Pose p = lellekhud$pose.reset(); pl.emote.pose(t, p);

        float th = p.bx * r * w, ph = p.by * r * w, rz = p.bz * r * w;
        float ct = (float) Math.cos(th), st = (float) Math.sin(th), cp = (float) Math.cos(ph), sp = (float) Math.sin(ph);
        // Rumpf: Drehpunkt Hals, gekippt um die Hüfte (y = 12)
        m.body.xRot = th; m.body.yRot = ph; m.body.zRot = rz;
        m.body.y = 12 - 12 * ct; m.body.z = -12 * st; m.body.x = 0;
        m.head.y = m.body.y; m.head.z = m.body.z; m.head.x = 0;
        m.head.xRot = mix(m.head.xRot, th + p.hx * r, w); m.head.yRot = mix(m.head.yRot, ph + p.hy * r, w); m.head.zRot = mix(m.head.zRot, rz + p.hz * r, w);
        // Schultern: (±5, 2 unter dem Hals) – erst Rumpfdrehung, dann Vorbeugen
        shoulder(m.rightArm, -5, ct, st, cp, sp); shoulder(m.leftArm, 5, ct, st, cp, sp);
        m.rightArm.xRot = mix(m.rightArm.xRot, th + p.rax * r, w); m.rightArm.yRot = mix(m.rightArm.yRot, ph + p.ray * r, w); m.rightArm.zRot = mix(m.rightArm.zRot, rz + p.raz * r, w);
        m.leftArm.xRot = mix(m.leftArm.xRot, th + p.lax * r, w); m.leftArm.yRot = mix(m.leftArm.yRot, ph + p.lay * r, w); m.leftArm.zRot = mix(m.leftArm.zRot, rz + p.laz * r, w);
        m.rightLeg.xRot = mix(m.rightLeg.xRot, p.rlx * r, w); m.rightLeg.yRot = mix(m.rightLeg.yRot, p.rly * r, w); m.rightLeg.zRot = mix(m.rightLeg.zRot, p.rlz * r, w);
        m.leftLeg.xRot = mix(m.leftLeg.xRot, p.llx * r, w); m.leftLeg.yRot = mix(m.leftLeg.yRot, p.lly * r, w); m.leftLeg.zRot = mix(m.leftLeg.zRot, p.llz * r, w);
        m.rightLeg.y = 12; m.leftLeg.y = 12; m.rightLeg.z = 0; m.leftLeg.z = 0;
    }
    private static void shoulder(ModelPart arm, float sx, float ct, float st, float cp, float sp) {
        float x = sx * cp, z = -sx * sp, y = -10;        // relativ zur Hüfte nach der Rumpfdrehung
        float y2 = y * ct - z * st, z2 = y * st + z * ct; // Vorbeugen um die X-Achse
        arm.x = x; arm.y = 12 + y2; arm.z = z2;
    }
    private static float mix(float a, float b, float w) { return a + (b - a) * w; }
}
