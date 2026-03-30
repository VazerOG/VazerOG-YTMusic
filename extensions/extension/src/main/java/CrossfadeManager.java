import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Player-swap crossfade manager for YouTube Music.
 *
 * Strategy: when a skip-next is detected (stopVideo reason=5), we
 * preserve the OLD ExoPlayer (which keeps playing the outgoing track)
 * and create a NEW ExoPlayer via YT Music's own factory method
 * (atih.a) so it has full DRM / DataSource configuration.  We swap
 * athu.h to the new player so the subsequent loadVideo flow uses it.
 * Once the new track reaches STATE_READY we run an equal-power
 * crossfade, then release the old player.
 *
 * Obfuscated name mapping (JADX → real):
 *
 *   atad.c              → field "c"  (atxb player interface)
 *   atux.a              → field "a"  (atxb delegate)
 *   athu.h              → field "h"  (ExoPlayer instance)
 *   athu.j              → field "j"  (atgd session)
 *   athu.i              → field "i"  (atis / cqf LoadControl)
 *   atgd.a              → field "a"  (atih player factory)
 *   atih.a(athu,cqf,int) → method "a" (build ExoPlayer)
 *   bxk.I(float)        → method "I" (setVolume)
 *   bxk.r()             → method "r" (getPlaybackState)
 *   ExoPlayer.P()       → method "P" (release)
 */
@SuppressWarnings("unused")
public class CrossfadeManager {

    private static final String TAG = "VazerOG_Crossfade";
    private static final String PREFS_NAME = "vazerog_prefs";
    private static final String KEY_ENABLED = "crossfade_enabled";
    private static final String KEY_DURATION = "crossfade_duration_sec";

    private static volatile int crossfadeDurationMs = 3000;
    private static volatile boolean enabled = true;
    private static volatile boolean settingsLoaded = false;

    private static volatile boolean crossfadeInProgress = false;
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static final int TICK_MS = 50;
    private static final int READY_POLL_MS = 100;
    private static final int READY_TIMEOUT_MS = 10000;
    private static final int STATE_READY = 3;
    private static final int REASON_DIRECTOR_RESET = 5;

    private static volatile Object oldPlayer = null;

    // ------------------------------------------------------------------ //
    //  Public hook: stopVideo (manual skip-next)                          //
    // ------------------------------------------------------------------ //

    public static void onBeforeStopVideo(Object atadInstance, int reason) {
        Log.d(TAG, "onBeforeStopVideo called, reason=" + reason);
        loadSettingsIfNeeded();

        if (!enabled || crossfadeDurationMs <= 0) {
            Log.d(TAG, "Crossfade disabled, skipping");
            return;
        }

        if (reason != REASON_DIRECTOR_RESET) {
            Log.d(TAG, "Not a skip-next stop (reason=" + reason + "), skipping");
            return;
        }

        if (crossfadeInProgress) {
            Log.d(TAG, "Crossfade already in progress, skipping");
            return;
        }

        try {
            Object athu = getAthuFromAtad(atadInstance);
            if (athu == null) {
                Log.e(TAG, "Could not find athu from atad");
                return;
            }

            Object currentExo = getFieldValue(athu, "h");
            if (currentExo == null) {
                Log.e(TAG, "athu.h (ExoPlayer) is null");
                return;
            }

            int currentState = callIntMethod(currentExo, "r");
            Log.d(TAG, "Current player state=" + currentState
                    + " class=" + currentExo.getClass().getName());

            Object atgd = getFieldValue(athu, "j");
            if (atgd == null) {
                Log.e(TAG, "athu.j (atgd session) is null");
                return;
            }

            Object atih = getFieldValue(atgd, "a");
            if (atih == null) {
                Log.e(TAG, "atgd.a (atih factory) is null");
                return;
            }

            Object cqf = getFieldValue(athu, "i");
            if (cqf == null) {
                Log.e(TAG, "athu.i (cqf/atis) is null");
                return;
            }

            Object crz = getFieldValue(athu, "c");
            if (crz == null) {
                Log.e(TAG, "athu.c (crz/cup) is null");
                return;
            }

            Object dll = getFieldValue(athu, "w");
            if (dll == null) {
                Log.e(TAG, "athu.w (atjx/dll) is null");
                return;
            }

            Object oldCrzPlayer = tryGetField(crz, "g");
            Object oldDllCallback = tryGetField(dll, "h");
            Log.d(TAG, "Clearing shared state: crz.g=" + (oldCrzPlayer != null) + " dll.h=" + (oldDllCallback != null));
            setFieldValue(crz, "g", null);
            setFieldValue(dll, "h", null);

            Log.d(TAG, "Creating new ExoPlayer via atih.a(athu, cqf, 0)");
            Object newExo = createPlayerViaFactory(atih, athu, cqf);
            if (newExo == null) {
                Log.e(TAG, "Factory returned null, restoring shared state");
                setFieldValue(crz, "g", oldCrzPlayer);
                setFieldValue(dll, "h", oldDllCallback);
                return;
            }
            Log.d(TAG, "New player created: " + newExo.getClass().getName());

            findMethod(newExo, "I", float.class).invoke(newExo, 0.0f);

            setFieldValue(athu, "h", newExo);
            Log.d(TAG, "Swapped athu.h → new player");

            releaseOld();
            oldPlayer = currentExo;
            crossfadeInProgress = true;

            Log.d(TAG, "Old player preserved (keeps playing), polling for new track ready");
            pollForNewTrackReady(newExo, currentExo);

        } catch (Exception e) {
            Log.e(TAG, "onBeforeStopVideo error", e);
            releaseOld();
            crossfadeInProgress = false;
        }
    }

    // ------------------------------------------------------------------ //
    //  Public hook: playNextInQueue (gapless auto-advance)                //
    // ------------------------------------------------------------------ //

    public static void onBeforePlayNext(Object athuInstance) {
        Log.d(TAG, "onBeforePlayNext called");
        loadSettingsIfNeeded();

        if (!enabled || crossfadeDurationMs <= 0 || crossfadeInProgress) {
            return;
        }

        try {
            Object currentExo = getFieldValue(athuInstance, "h");
            if (currentExo == null) return;

            int currentState = callIntMethod(currentExo, "r");
            Log.d(TAG, "PlayNext: current player state=" + currentState);

            Object atgd = getFieldValue(athuInstance, "j");
            if (atgd == null) return;
            Object atih = getFieldValue(atgd, "a");
            if (atih == null) return;
            Object cqf = getFieldValue(athuInstance, "i");
            if (cqf == null) return;

            Object crz = getFieldValue(athuInstance, "c");
            if (crz == null) return;
            Object dll = getFieldValue(athuInstance, "w");
            if (dll == null) return;
            setFieldValue(crz, "g", null);
            setFieldValue(dll, "h", null);

            Object newExo = createPlayerViaFactory(atih, athuInstance, cqf);
            if (newExo == null) return;

            findMethod(newExo, "I", float.class).invoke(newExo, 0.0f);

            setFieldValue(athuInstance, "h", newExo);
            Log.d(TAG, "PlayNext: swapped athu.h → new player");

            releaseOld();
            oldPlayer = currentExo;
            crossfadeInProgress = true;

            pollForNewTrackReady(newExo, currentExo);

        } catch (Exception e) {
            Log.e(TAG, "onBeforePlayNext error", e);
            releaseOld();
            crossfadeInProgress = false;
        }
    }

    // ------------------------------------------------------------------ //
    //  Poller: waits for new track to reach STATE_READY                   //
    // ------------------------------------------------------------------ //

    private static void pollForNewTrackReady(final Object newPlayer, final Object outPlayer) {
        final long deadline = System.currentTimeMillis() + READY_TIMEOUT_MS;

        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    int state = callIntMethod(newPlayer, "r");
                    if (state == STATE_READY) {
                        Log.d(TAG, "New track READY — starting crossfade");
                        animateCrossfade(outPlayer, newPlayer);
                        return;
                    }

                    if (state == 4) {
                        Log.e(TAG, "New player ended unexpectedly, aborting crossfade");
                        releaseOld();
                        crossfadeInProgress = false;
                        try {
                            findMethod(newPlayer, "I", float.class)
                                    .invoke(newPlayer, 1.0f);
                        } catch (Exception ignored) {}
                        return;
                    }

                    Log.d(TAG, "Poll: new player state=" + state);

                    if (System.currentTimeMillis() > deadline) {
                        Log.e(TAG, "Timeout waiting for new track");
                        releaseOld();
                        crossfadeInProgress = false;
                        try {
                            findMethod(newPlayer, "I", float.class)
                                    .invoke(newPlayer, 1.0f);
                        } catch (Exception ignored) {}
                        return;
                    }

                    mainHandler.postDelayed(this, READY_POLL_MS);
                } catch (Exception e) {
                    Log.e(TAG, "Poll error", e);
                    releaseOld();
                    crossfadeInProgress = false;
                }
            }
        }, READY_POLL_MS);
    }

    // ------------------------------------------------------------------ //
    //  Volume animation (equal-power curve)                               //
    // ------------------------------------------------------------------ //

    private static void animateCrossfade(final Object outPlayer, final Object inPlayer) {
        if (outPlayer == null) {
            Log.w(TAG, "No old player to crossfade from");
            crossfadeInProgress = false;
            try {
                findMethod(inPlayer, "I", float.class).invoke(inPlayer, 1.0f);
            } catch (Exception ignored) {}
            return;
        }

        final long startTime = System.currentTimeMillis();
        final long duration = crossfadeDurationMs;

        Log.d(TAG, "Crossfade animation started, duration=" + duration + "ms");

        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - startTime;
                float t = Math.min(1.0f, (float) elapsed / duration);

                float outVol = (float) Math.cos(t * Math.PI / 2.0);
                float inVol  = (float) Math.sin(t * Math.PI / 2.0);

                try {
                    findMethod(outPlayer, "I", float.class).invoke(outPlayer, outVol);
                    findMethod(inPlayer, "I", float.class).invoke(inPlayer, inVol);
                    if (elapsed % 500 < TICK_MS) {
                        int outState = callIntMethod(outPlayer, "r");
                        int inState = callIntMethod(inPlayer, "r");
                        Log.d(TAG, String.format("t=%.2f outVol=%.2f(st=%d) inVol=%.2f(st=%d)",
                                t, outVol, outState, inVol, inState));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Volume tick error", e);
                }

                if (t < 1.0f) {
                    mainHandler.postDelayed(this, TICK_MS);
                } else {
                    Log.d(TAG, "Crossfade complete");
                    try {
                        findMethod(inPlayer, "I", float.class).invoke(inPlayer, 1.0f);
                    } catch (Exception ignored) {}
                    releaseOld();
                    crossfadeInProgress = false;
                }
            }
        });
    }

    // ------------------------------------------------------------------ //
    //  Player creation via YTM factory                                    //
    // ------------------------------------------------------------------ //

    private static Object createPlayerViaFactory(Object atih, Object athu, Object cqf) {
        try {
            Class<?> athuClass = Class.forName("athu");
            Class<?> cqfClass  = Class.forName("cqf");

            Method factoryMethod = atih.getClass().getDeclaredMethod("a",
                    athuClass, cqfClass, int.class);
            factoryMethod.setAccessible(true);

            return factoryMethod.invoke(atih, athu, cqf, 0);
        } catch (Exception e) {
            Log.e(TAG, "createPlayerViaFactory failed", e);
            return null;
        }
    }

    // ------------------------------------------------------------------ //
    //  athu traversal from atad                                           //
    // ------------------------------------------------------------------ //

    /**
     * Walks atad.c → atux.a → ... → athu to reach the innermost
     * player coordinator that holds the ExoPlayer reference at field h.
     */
    private static Object getAthuFromAtad(Object atadInstance) {
        try {
            Object atxb = getFieldValue(atadInstance, "c");
            if (atxb == null) {
                Log.e(TAG, "atad.c is null");
                return null;
            }

            int depth = 0;
            for (int i = 0; i < 10; i++) {
                Object delegate = tryGetField(atxb, "a");
                if (delegate == null || delegate == atxb) break;
                atxb = delegate;
                depth++;
            }

            Log.d(TAG, "Traversed " + depth + " delegates → " + atxb.getClass().getName());

            if (tryGetField(atxb, "h") != null && tryGetField(atxb, "j") != null) {
                return atxb;
            }

            Log.e(TAG, "Innermost class doesn't look like athu: " + atxb.getClass().getName());
            return null;
        } catch (Exception e) {
            Log.e(TAG, "getAthuFromAtad error", e);
            return null;
        }
    }

    // ------------------------------------------------------------------ //
    //  Old player lifecycle                                               //
    // ------------------------------------------------------------------ //

    private static void releaseOld() {
        Object p = oldPlayer;
        oldPlayer = null;
        if (p != null) {
            Log.d(TAG, "Releasing old player");
            // Null out ONLY the dlt field ("O") on the old player. In the
            // release sequence, cpp.P() accesses dlt at line 855 BEFORE
            // reaching crz.U() at line 866. By nulling dlt, release() NPEs
            // at line 855 (dlt.h(crz) on null dlt), which prevents the
            // destructive crz.U() from running. crz.U() would otherwise
            // asynchronously clear the shared cup's cau listener handler
            // that the new player needs for UI callbacks.
            //
            // We keep crz (field "j") INTACT because other per-player
            // async code (e.g. cpj.e()) still references it after release.
            setFieldValue(p, "O", null);  // dlt (prevents reaching crz.U())
            safeRelease(p);
        }
    }

    // ------------------------------------------------------------------ //
    //  Settings                                                           //
    // ------------------------------------------------------------------ //

    public static void setCrossfadeDuration(int durationMs) {
        crossfadeDurationMs = Math.max(500, Math.min(12000, durationMs));
    }

    public static void setEnabled(boolean flag) {
        enabled = flag;
    }

    public static int getCrossfadeDuration() {
        return crossfadeDurationMs;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Called by the bytecode hook on the audio/video toggle (nba.c).
     * Returns true if the toggle should be blocked (user is trying to
     * switch TO video while crossfade is enabled).  Switching FROM
     * video to audio is always allowed so the user can exit video mode.
     */
    public static boolean shouldBlockVideoToggle(Object nba) {
        loadSettingsIfNeeded();
        if (!enabled) return false;
        try {
            Object nlwInstance = getFieldValue(nba, "a");
            Object currentState = findMethod(nlwInstance, "a").invoke(nlwInstance);

            java.lang.reflect.Method fMethod = null;
            for (java.lang.reflect.Method m : nlwInstance.getClass().getDeclaredMethods()) {
                if (m.getName().equals("f") && m.getParameterCount() == 1
                        && m.getReturnType() == boolean.class) {
                    fMethod = m;
                    break;
                }
            }
            if (fMethod == null) return false;
            fMethod.setAccessible(true);
            boolean isAudioMode = (boolean) fMethod.invoke(null, currentState);

            if (isAudioMode) {
                showVideoBlockedToast();
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.w(TAG, "Could not check video toggle state", e);
            return false;
        }
    }

    private static void showVideoBlockedToast() {
        try {
            Context ctx = getAppContext();
            if (ctx == null) return;
            new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(ctx,
                    "Video mode is not available while crossfade is enabled",
                    Toast.LENGTH_SHORT).show()
            );
        } catch (Exception ignored) {}
    }

    private static void loadSettingsIfNeeded() {
        if (settingsLoaded) return;
        settingsLoaded = true;
        try {
            Context ctx = getAppContext();
            if (ctx == null) return;
            SharedPreferences prefs =
                    ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            enabled = prefs.getBoolean(KEY_ENABLED, true);
            int sec = prefs.getInt(KEY_DURATION, 3);
            crossfadeDurationMs = Math.max(1, Math.min(12, sec)) * 1000;

        } catch (Exception ignored) {}
    }

    // ------------------------------------------------------------------ //
    //  Reflection helpers                                                 //
    // ------------------------------------------------------------------ //

    private static Object getFieldValue(Object obj, String name) {
        try {
            Field f = findField(obj.getClass(), name);
            return f.get(obj);
        } catch (Exception e) {
            Log.e(TAG, "getFieldValue(" + name + ") on " + obj.getClass().getName() + " failed", e);
            return null;
        }
    }

    private static void setFieldValue(Object obj, String name, Object value) {
        try {
            Field f = findField(obj.getClass(), name);
            f.set(obj, value);
        } catch (Exception e) {
            Log.e(TAG, "setFieldValue(" + name + ") on " + obj.getClass().getName() + " failed", e);
        }
    }

    private static Object tryGetField(Object obj, String name) {
        Class<?> cur = obj.getClass();
        while (cur != null) {
            try {
                Field f = cur.getDeclaredField(name);
                f.setAccessible(true);
                return f.get(obj);
            } catch (NoSuchFieldException ignored) {
                cur = cur.getSuperclass();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private static Field findField(Class<?> clazz, String name) throws NoSuchFieldException {
        Class<?> cur = clazz;
        while (cur != null) {
            try {
                Field f = cur.getDeclaredField(name);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException ignored) {
                cur = cur.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name + " on " + clazz.getName());
    }

    private static Method findMethod(Object obj, String name, Class<?>... params)
            throws NoSuchMethodException {
        try {
            Method m = obj.getClass().getMethod(name, params);
            m.setAccessible(true);
            return m;
        } catch (NoSuchMethodException ignored) {}
        Class<?> cur = obj.getClass();
        while (cur != null) {
            try {
                Method m = cur.getDeclaredMethod(name, params);
                m.setAccessible(true);
                return m;
            } catch (NoSuchMethodException ignored) {
                cur = cur.getSuperclass();
            }
        }
        throw new NoSuchMethodException(name + " on " + obj.getClass().getName());
    }

    private static int callIntMethod(Object obj, String name) throws Exception {
        return (int) findMethod(obj, name).invoke(obj);
    }

    private static void safeRelease(Object player) {
        try {
            findMethod(player, "P").invoke(player);
        } catch (Exception e) {
            Log.w(TAG, "Partial release (shared resources preserved): "
                    + e.getCause().getMessage());
        }
    }

    private static Context getAppContext() {
        try {
            return (Context) Class.forName("android.app.ActivityThread")
                    .getMethod("currentApplication")
                    .invoke(null);
        } catch (Exception e) {
            return null;
        }
    }
}
