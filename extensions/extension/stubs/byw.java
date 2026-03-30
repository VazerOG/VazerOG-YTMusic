/**
 * Compile-time stub for the obfuscated AudioFormat class (byw).
 *
 * Field names are the REAL obfuscated names from the APK's DEX.
 *
 * Mapping (JADX name → real name):
 *   f96375a → a  (NOT_SET constant)
 *   f96376b → b  (sampleRate)
 *   f96377c → c  (channelCount)
 *   f96378d → d  (encoding)
 *   f96379e → e  (bytesPerFrame)
 */
public final class byw {
    public static final byw a = new byw(-1, -1, -1);

    public final int b;
    public final int c;
    public final int d;
    public final int e;

    public byw(int sampleRate, int channelCount, int encoding) {
        this.b = sampleRate;
        this.c = channelCount;
        this.d = encoding;
        this.e = -1;
    }
}
