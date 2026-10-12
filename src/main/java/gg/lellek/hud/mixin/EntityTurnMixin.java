package gg.lellek.hud.mixin;

import gg.lellek.hud.feature.Perspective;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Perspektive: Mausbewegung geht auf die Kamera statt auf den Spieler. */
@Mixin(Entity.class)
public class EntityTurnMixin {
    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void lellekhud$turn(double yRot, double xRot, CallbackInfo ci) {
        gg.lellek.hud.Diag.turn = true;
        if ((Object) this instanceof LocalPlayer && Perspective.onTurn(yRot, xRot)) ci.cancel();
    }
}
