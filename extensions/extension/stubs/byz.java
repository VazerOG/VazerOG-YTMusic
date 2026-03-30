import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Compile-time stub for the obfuscated AudioProcessor interface (byz).
 *
 * Method names are the REAL obfuscated names from the APK's DEX,
 * not the JADX-decompiled names (which prepend mo[number]).
 *
 * Mapping (JADX name → real name):
 *   mo3043a → a  (getMediaDuration)
 *   mo3044b → b  (configure)
 *   mo3045c → c  (getOutput)
 *   mo3046d → d  (?)
 *   mo3047e → e  (queueEndOfStream)
 *   mo3048f → f  (queueInput)
 *   mo3049g → g  (reset)
 *   mo3050h → h  (isActive)
 *   mo3051i → i  (isEnded)
 *   mo3052j → j  (flush)
 *   f96700a → a  (EMPTY_BUFFER)
 */
public interface byz {
    public static final ByteBuffer a = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder());

    long a(long j);
    byw b(byw bywVar);
    ByteBuffer c();
    void d();
    void e();
    void f(ByteBuffer byteBuffer);
    void g();
    boolean h();
    boolean i();
    void j();
}
