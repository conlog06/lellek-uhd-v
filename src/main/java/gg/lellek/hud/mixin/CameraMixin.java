package gg.lellek.hud.mixin;

import gg.lellek.hud.feature.Perspective;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/** Perspektive: Kamerarotation aus Perspective statt aus dem Spieler. */
@Mixin(Camera.class)
public class CameraMixin {
    @ModifyArgs(method = { "alignWithEntity", "setup" }, require = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V"))
    private void lellekhud$rotation(Args args) {
        gg.lellek.hud.Diag.camera = true;
        if (Perspective.active) { args.set(0, Perspective.camYaw); args.set(1, Perspective.camPitch); }
    }
}
