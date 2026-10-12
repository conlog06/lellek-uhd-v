package gg.lellek.hud.voice;

/** G.711 µ-law – 16-bit PCM ↔ 8-bit, ohne Bibliotheken. */
public final class ULaw {
    private static final int BIAS = 0x84, CLIP = 32635;
    private static final short[] DECODE = new short[256];
    static { for (int i = 0; i < 256; i++) { int u = ~i & 0xFF, sign = u & 0x80, exp = (u >> 4) & 7, mant = u & 0xF; int s = ((mant << 3) + BIAS) << exp; s -= BIAS; DECODE[i] = (short) (sign != 0 ? -s : s); } }
    public static byte encode(short pcm) {
        int sign = (pcm >> 8) & 0x80; if (sign != 0) pcm = (short) -pcm; if (pcm > CLIP) pcm = CLIP;
        int s = pcm + BIAS, exp = 7; for (int m = 0x4000; (s & m) == 0 && exp > 0; exp--, m >>= 1) {}
        int mant = (s >> (exp + 3)) & 0xF;
        return (byte) ~(sign | (exp << 4) | mant);
    }
    public static short decode(byte u) { return DECODE[u & 0xFF]; }
}
