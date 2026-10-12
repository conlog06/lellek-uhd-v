package gg.lellek.hud.mixin;

import gg.lellek.hud.LellekHud;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Smooth Zoom: FOV pro Frame weich verkleinern, solange gezoomt wird (inkl. Rück-Animation). */
@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true, require = 0)
    private void lellekhud$zoom(Camera camera, float partialTick, boolean useFovSetting, CallbackInfoReturnable<Float> cir) {
        gg.lellek.hud.Diag.fov = true;
        if (LellekHud.features != null && LellekHud.features.zoomActive()) cir.setReturnValue(LellekHud.features.modifyFov(cir.getReturnValue()));
    }
}
