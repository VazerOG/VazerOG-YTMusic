import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Compile-time stub for the obfuscated BaseAudioProcessor class (bza).
 *
 * Method/field names are the REAL obfuscated names from the APK's DEX.
 *
 * Mapping (JADX name → real name):
 *   Fields:
 *     f96788b → b  (pendingInputFormat)
 *     f96789c → c  (pendingOutputFormat)
 *     f96790d → d  (outputBuffer)
 *     f96791e → e  (inputFormat)
 *     f96792f → f  (outputFormat)
 *     f96793g → g  (buffer)
 *     f96794h → h  (inputEnded)
 *   Methods:
 *     mo3044b → b  (configure - public final, from interface)
 *     mo3045c → c  (getOutput)
 *     mo3046d → d  (?)
 *     mo3047e → e  (queueEndOfStream)
 *     mo3049g → g  (reset)
 *     mo3050h → h  (isActive)
 *     mo3051i → i  (isEnded)
 *     mo3052j → j  (flush)
 *     mo3062k → k  (onConfigure - protected, subclasses override this)
 *     m31773l → l  (replaceOutputBuffer)
 *     mo3063m → m  (onFlush)
 *     mo31774n → n  (onQueueEndOfStream)
 *     mo31775o → o  (onReset)
 *     mo3043a → a  (getMediaDuration)
 */
public abstract class bza implements byz {
    protected byw b;
    protected byw c;
    public ByteBuffer d;
    private byw e;
    private byw f;
    private ByteBuffer g;
    private boolean h;

    public bza() {
        ByteBuffer byteBuffer = byz.a;
        this.g = byteBuffer;
        this.d = byteBuffer;
        byw bywVar = byw.a;
        this.e = bywVar;
        this.f = bywVar;
        this.b = bywVar;
        this.c = bywVar;
    }

    @Override
    public final byw b(byw bywVar) {
        this.e = bywVar;
        this.f = k(bywVar);
        return h() ? this.f : byw.a;
    }

    @Override
    public ByteBuffer c() {
        ByteBuffer byteBuffer = this.d;
        this.d = byz.a;
        return byteBuffer;
    }

    @Override
    public final void d() {
        j();
    }

    @Override
    public final void e() {
        this.h = true;
        n();
    }

    @Override
    public final void g() {
        ByteBuffer byteBuffer = byz.a;
        this.d = byteBuffer;
        this.h = false;
        this.g = byteBuffer;
        byw bywVar = byw.a;
        this.e = bywVar;
        this.f = bywVar;
        this.b = bywVar;
        this.c = bywVar;
        o();
    }

    @Override
    public boolean h() {
        return this.f != byw.a;
    }

    @Override
    public boolean i() {
        return this.h && this.d == byz.a;
    }

    @Override
    public final void j() {
        this.d = byz.a;
        this.h = false;
        this.b = this.e;
        this.c = this.f;
        m();
    }

    protected byw k(byw bywVar) {
        throw null;
    }

    protected final ByteBuffer l(int i) {
        if (this.g.capacity() < i) {
            this.g = ByteBuffer.allocateDirect(i).order(ByteOrder.nativeOrder());
        } else {
            this.g.clear();
        }
        ByteBuffer byteBuffer = this.g;
        this.d = byteBuffer;
        return byteBuffer;
    }

    protected void m() {
    }

    protected void n() {
    }

    protected void o() {
    }

    @Override
    public long a(long j) {
        return j;
    }
}
