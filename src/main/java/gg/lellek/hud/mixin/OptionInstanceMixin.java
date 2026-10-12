package gg.lellek.hud.mixin;

import gg.lellek.hud.LellekHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fullbright: Gamma darf über 1.0 gehen, wenn es die Gamma-Option ist und Fullbright an. */
@Mixin(OptionInstance.class)
public class OptionInstanceMixin<T> {
    @Shadow T value;

    @Inject(method = "set", at = @At("HEAD"), cancellable = true)
    private void lellekhud$gamma(T newValue, CallbackInfo ci) {
        gg.lellek.hud.Diag.gamma = true;
        Minecraft mc = Minecraft.getInstance();
        if (gg.lellek.hud.feature.Mods.FULLBRIGHT.enabled && mc.options != null && (Object) this == mc.options.gamma() && newValue instanceof Double d && d > 1.0) {
            value = newValue;
            ci.cancel();
        }
    }
}
