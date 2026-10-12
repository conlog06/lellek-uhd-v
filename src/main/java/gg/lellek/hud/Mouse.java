package gg.lellek.hud;

import com.mojang.blaze3d.platform.InputConstants;

/** Maustasten-Nummern aus Minecraft selbst – seit 26.3 (SDL) ist links = 1, rechts = 3 statt 0/1. Nie hart codieren! */
public final class Mouse {
    public static final int LEFT = InputConstants.MOUSE_BUTTON_LEFT, RIGHT = InputConstants.MOUSE_BUTTON_RIGHT;
    private Mouse() {}
}
