package gg.lellek.hud;

import net.minecraft.client.Minecraft;

import java.util.ArrayDeque;
import java.util.Deque;

/** Zählt Klicks der letzten Sekunde. Quelle: MouseHandlerMixin – oder, wenn der in dieser Version nicht greift, die Tastenzustände pro Tick. */
public class ClickTracker {
    private static final Deque<Long> left = new ArrayDeque<>(), right = new ArrayDeque<>();
    private static boolean mixinActive, wasLeft, wasRight;
    public static void press(int button) { mixinActive = true; long now = System.currentTimeMillis(); if (button == 0) left.addLast(now); if (button == 1) right.addLast(now); }
    public static void tick(Minecraft mc) {
        if (mixinActive || mc.player == null) return;
        boolean l = mc.options.keyAttack.isDown(), r = mc.options.keyUse.isDown(); long now = System.currentTimeMillis();
        if (l && !wasLeft) left.addLast(now); if (r && !wasRight) right.addLast(now);
        wasLeft = l; wasRight = r;
    }
    private static int count(Deque<Long> q) { long limit = System.currentTimeMillis() - 1000L; while (!q.isEmpty() && q.peekFirst() < limit) q.pollFirst(); return q.size(); }
    public static int leftCps() { return count(left); }
    public static int rightCps() { return count(right); }
}
