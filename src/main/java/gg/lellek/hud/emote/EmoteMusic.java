package gg.lellek.hud.emote;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

/**
 * Kleine Notenblock-Musik zu einem Emote. Muster in Achteln:
 * Melodie/Bass: Halbtöne 0–24 (Notenblock-Tonleiter, 12 = Mitte) oder "." für Pause.
 * Schlagzeug: k = Bassdrum, s = Snare, h = Hi-Hat, . = Pause.
 */
public class EmoteMusic {
    final float bpm; final Holder<SoundEvent> lead, bass; final int[] melody, bassline; final char[] drums; final int length;

    public EmoteMusic(float bpm, Holder<SoundEvent> lead, String melody, Holder<SoundEvent> bass, String bassline, String drums) {
        this.bpm = bpm; this.lead = lead; this.bass = bass;
        this.melody = parse(melody); this.bassline = parse(bassline); this.drums = drums == null ? new char[0] : drums.replace(" ", "").toCharArray();
        this.length = Math.max(Math.max(this.melody.length, this.bassline.length), this.drums.length);
    }
    private static int[] parse(String s) {
        if (s == null || s.isBlank()) return new int[0];
        String[] p = s.trim().split("\\s+"); int[] r = new int[p.length];
        for (int i = 0; i < p.length; i++) r[i] = p[i].equals(".") ? -1 : Integer.parseInt(p[i]);
        return r;
    }
    public int stepAt(float t) { return (int) Math.floor(t * bpm / 60f * 2f); }

    public void play(int step, float volume) {
        if (length == 0 || volume <= 0.01f) return;
        if (melody.length > 0) { int n = melody[step % melody.length]; if (n >= 0 && lead != null) note(lead, n, volume); }
        if (bassline.length > 0) { int n = bassline[step % bassline.length]; if (n >= 0 && bass != null) note(bass, n, volume * 0.9f); }
        if (drums.length > 0) switch (drums[step % drums.length]) {
            case 'k' -> note(SoundEvents.NOTE_BLOCK_BASEDRUM, 12, volume * 0.8f);
            case 's' -> note(SoundEvents.NOTE_BLOCK_SNARE, 12, volume * 0.6f);
            case 'h' -> note(SoundEvents.NOTE_BLOCK_HAT, 14, volume * 0.35f);
            default -> {}
        }
    }
    private static void note(Holder<SoundEvent> s, int semis, float vol) {
        float pitch = (float) Math.pow(2, (semis - 12) / 12.0);
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(s.value(), pitch, vol));
    }

    // ---- Fertige Songs ----
    static final Holder<SoundEvent> HARP = SoundEvents.NOTE_BLOCK_HARP, BASS = SoundEvents.NOTE_BLOCK_BASS, BIT = SoundEvents.NOTE_BLOCK_BIT, BELL = SoundEvents.NOTE_BLOCK_BELL,
            FLUTE = SoundEvents.NOTE_BLOCK_FLUTE, GUITAR = SoundEvents.NOTE_BLOCK_GUITAR, XYLO = SoundEvents.NOTE_BLOCK_XYLOPHONE, PLING = SoundEvents.NOTE_BLOCK_PLING,
            BANJO = SoundEvents.NOTE_BLOCK_BANJO, CHIME = SoundEvents.NOTE_BLOCK_CHIME, IRONX = SoundEvents.NOTE_BLOCK_IRON_XYLOPHONE, COWBELL = SoundEvents.NOTE_BLOCK_COW_BELL, DIDGE = SoundEvents.NOTE_BLOCK_DIDGERIDOO;

    public static final EmoteMusic POP = new EmoteMusic(118, PLING, "12 . 16 . 19 . 16 . 14 . 17 . 21 . 17 .", BASS, "0 . . . 0 . . . 2 . . . 2 . . .", "k.h.s.h.k.h.s.h.");
    public static final EmoteMusic DISCO = new EmoteMusic(122, BIT, "19 19 . 17 . 15 . 17 19 . 22 . 19 . 17 .", BASS, "3 . 15 . 3 . 15 . 5 . 17 . 5 . 17 .", "khshkhshkhshkhsh");
    public static final EmoteMusic FUNK = new EmoteMusic(105, GUITAR, "12 . . 15 . 12 17 . 12 . . 15 . 19 17 .", BASS, "0 . 0 12 . 0 . 10 0 . 0 12 . 7 . 10", "k.h.s.hkk.h.s.h.");
    public static final EmoteMusic LATIN = new EmoteMusic(128, BANJO, "14 . 17 14 . 21 . 19 17 . 14 . 17 19 21 .", BASS, "2 . . 9 . . 2 . 7 . . 14 . . 7 .", "k.hsk.hsk.hsk.hs");
    public static final EmoteMusic ELECTRO = new EmoteMusic(132, BIT, "24 . 19 . 24 . 22 . 19 . 17 . 19 . 15 .", BASS, "0 0 . 0 0 . 0 . 3 3 . 3 3 . 5 .", "k.k.k.k.k.k.k.k.");
    public static final EmoteMusic WALTZ = new EmoteMusic(150, BELL, "12 16 19 17 16 14 12 16 19 21 19 17", HARP, "0 . . 5 . . 7 . . 0 . .", "");
    public static final EmoteMusic CHIPTUNE = new EmoteMusic(140, BIT, "12 14 16 19 16 14 12 . 19 21 24 21 19 16 12 .", BASS, "0 . 0 . 5 . 5 . 7 . 7 . 5 . 0 .", "k.s.k.s.k.s.k.ss");
    public static final EmoteMusic CANCAN = new EmoteMusic(150, FLUTE, "12 12 14 12 17 16 . . 12 12 14 12 19 17 . .", BASS, "0 . 7 . 0 . 7 . 0 . 7 . 5 . 7 .", "k.s.k.s.k.s.k.s.");
    public static final EmoteMusic CHICKEN = new EmoteMusic(126, XYLO, "12 12 14 14 9 9 12 . 12 12 14 14 9 9 12 .", BASS, "0 . 7 . 0 . 7 . 0 . 7 . 0 . 7 .", "k.s.k.s.k.s.k.s.");
    public static final EmoteMusic EPIC = new EmoteMusic(96, IRONX, "12 . 15 . 19 . 24 . 22 . 19 . 15 . 17 .", DIDGE, "0 . . . . . . . 3 . . . 5 . . .", "k...s...k.k.s...");
    public static final EmoteMusic MAGIC = new EmoteMusic(110, CHIME, "24 21 19 16 24 21 19 16", null, "", "");
    public static final EmoteMusic SAD = new EmoteMusic(70, HARP, "12 . 11 . 9 . 7 . 9 . 7 . 5 . 4 .", null, "", "");
    public static final EmoteMusic COWBELLS = new EmoteMusic(120, COWBELL, "12 . 12 12 . 12 . 12", BASS, "0 . . 0 . . 0 .", "k.s.k.s.");
    public static final EmoteMusic CLAPS = new EmoteMusic(120, null, "", null, "", "s.s.s.s.");
    public static final EmoteMusic DRUMS = new EmoteMusic(140, null, "", null, "", "kshskshhkkshskss");
    public static final EmoteMusic AIRGUITAR = new EmoteMusic(140, GUITAR, "12 12 15 12 17 12 15 12 10 10 12 10 15 10 12 10", BASS, "0 . 0 . 0 . 0 . 10 . 10 . 10 . 10 .", "k.skk.s.k.skk.ss");
    public static final EmoteMusic FANFARE = new EmoteMusic(120, PLING, "12 . 12 12 16 . . . 19 . 16 19 24 . . .", null, "", "k...s...k...s...");
}
