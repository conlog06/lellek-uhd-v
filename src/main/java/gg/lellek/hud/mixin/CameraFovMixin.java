package gg.lellek.hud.mixin;

import gg.lellek.hud.LellekHud;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Smooth Zoom: In 26.3 berechnet Camera.calculateFov das Sichtfeld (vorher GameRenderer.getFov).
 *  Die Hand (calculateHudFov) bleibt unverändert, nur die Welt wird vergrößert. */
@Mixin(Camera.class)
public class CameraFovMixin {
    @Inject(method = "calculateFov", at = @At("RETURN"), cancellable = true, require = 0)
    private void lellekhud$zoom(float partialTick, CallbackInfoReturnable<Float> cir) {
        gg.lellek.hud.Diag.fov = true;
        if (LellekHud.features != null && LellekHud.features.zoomActive()) cir.setReturnValue(LellekHud.features.modifyFov(cir.getReturnValue()));
    }
}
