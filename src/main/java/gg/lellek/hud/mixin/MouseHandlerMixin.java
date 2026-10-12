package gg.lellek.hud.mixin;

import gg.lellek.hud.ClickTracker;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** CPS zählen + Mausrad beim Zoomen abfangen.
 *  CPS – Signatur laut 26.2: onButton(long window, MouseButtonInfo info, int action). */
@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Inject(method = "onButton", at = @At("HEAD"), require = 0)
    private void lellekhud$press(long window, MouseButtonInfo info, int action, CallbackInfo ci) {
        gg.lellek.hud.Diag.mouse = true;
        if (action == com.mojang.blaze3d.platform.InputConstants.PRESS) ClickTracker.press(info.button());
    }

    /** Mausrad während Zoom: Zoom-Stufe statt Hotbar-Slot ändern. */
    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true, require = 0)
    private void lellekhud$scroll(long window, double xOffset, double yOffset, CallbackInfo ci) {
        gg.lellek.hud.Diag.scroll = true;
        if (gg.lellek.hud.LellekHud.features != null && gg.lellek.hud.LellekHud.features.onScroll(yOffset)) ci.cancel();
    }
}
