package gg.lellek.hud.emote;

/** Gelenkwinkel (Grad) und Körperversatz für eine Emote-Pose. */
public class Pose {
    public float headX, headY, bodyX, bodyY, rArmX, rArmY, rArmZ, lArmX, lArmY, lArmZ, rLegX, rLegZ, lLegX, lLegZ, offsetY;
    public Pose reset() { headX = headY = bodyX = bodyY = rArmX = rArmY = rArmZ = lArmX = lArmY = lArmZ = rLegX = rLegZ = lLegX = lLegZ = offsetY = 0; return this; }
}
