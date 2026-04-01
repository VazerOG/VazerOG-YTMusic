package app.template.extension.music.patches;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.EditText;
import android.widget.TextView;
import android.text.InputType;
import android.view.ViewTreeObserver;
import android.graphics.Rect;

import java.lang.reflect.Method;

/**
 * Standalone settings Activity for VazerOG crossfade configuration.
 *
 * Launched from the YT Music settings_headers via an explicit intent.
 * Persists values to SharedPreferences ("vazerog_prefs") and pushes
 * them into CrossfadeManager's volatile fields for immediate effect.
 */
public class VazerOGSettingsActivity extends Activity {

    private static final String PREFS_NAME = "vazerog_prefs";
    private static final String KEY_ENABLED = "crossfade_enabled";
    private static final String KEY_DURATION = "crossfade_duration_sec";
    private static final String KEY_SESSION_CONTROL = "session_control_enabled";
    private static final String KEY_ADVANCED_MODE = "crossfade_advanced_mode";
    private static final String KEY_DURATION_MS = "crossfade_duration_ms";
    private static final String KEY_LONG_PRESS_MS = "long_press_duration_ms";

    private static final String PATCH_VERSION = "1.1.0-dev.45";

    private static final int BG_COLOR = 0xFF121212;
    private static final int SURFACE_COLOR = 0xFF1E1E1E;
    private static final int TEXT_PRIMARY = 0xFFE0E0E0;
    private static final int TEXT_SECONDARY = 0xFF9E9E9E;
    private static final int ACCENT_COLOR = 0xFFBB86FC;
    private static final int DIVIDER_COLOR = 0xFF2C2C2C;

    private SharedPreferences prefs;
    private View durationSliderView;
    private View durationMsView;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setTitle("VazerOG");
            getActionBar().setSubtitle("v" + PATCH_VERSION);
        }

        scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(BG_COLOR);
        scrollView.setFillViewport(true);
        scrollView.setClipToPadding(false);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(0, 0, 0, dp(300));

        root.addView(buildLogo());
        root.addView(buildSectionHeader("Player"));
        root.addView(buildCrossfadeToggle());
        root.addView(buildDivider());
        root.addView(buildSessionControlToggle());
        root.addView(buildDivider());
        root.addView(buildLongPressSlider());
        root.addView(buildDivider());
        durationSliderView = buildDurationSlider();
        root.addView(durationSliderView);
        root.addView(buildDivider());
        root.addView(buildAdvancedModeToggle());
        root.addView(buildDivider());
        durationMsView = buildDurationMsInput();
        root.addView(durationMsView);
        root.addView(buildDivider());
        root.addView(buildVideoModeNote());
        root.addView(buildDivider());
        root.addView(buildInfoCard());
        root.addView(buildVersionFooter());

        boolean advanced = prefs.getBoolean(KEY_ADVANCED_MODE, false);
        durationSliderView.setVisibility(advanced ? View.GONE : View.VISIBLE);
        durationMsView.setVisibility(advanced ? View.VISIBLE : View.GONE);

        scrollView.addView(root);
        setContentView(scrollView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private View buildLogo() {
        FrameLayout wrapper = new FrameLayout(this);
        wrapper.setClipChildren(false);
        wrapper.setClipToPadding(false);
        wrapper.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        ImageView logo = new ImageView(this);
        int resId = getResources().getIdentifier("vazerog_logo", "drawable", getPackageName());
        if (resId != 0) {
            logo.setImageResource(resId);
        }
        logo.setAdjustViewBounds(true);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        int maxW = dp(480);
        int screenW = getResources().getDisplayMetrics().widthPixels;
        if (screenW > maxW) {
            lp.width = maxW;
        }
        logo.setLayoutParams(lp);

        wrapper.addView(logo);
        return wrapper;
    }

    private View buildSectionHeader(String text) {
        TextView header = new TextView(this);
        header.setText(text);
        header.setTextColor(ACCENT_COLOR);
        header.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        header.setTypeface(Typeface.DEFAULT_BOLD);
        header.setPadding(dp(20), dp(20), dp(20), dp(8));
        return header;
    }

    private View buildCrossfadeToggle() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(20), dp(16), dp(20), dp(16));

        LinearLayout labels = new LinearLayout(this);
        labels.setOrientation(LinearLayout.VERTICAL);
        labels.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView title = new TextView(this);
        title.setText("Enable crossfade");
        title.setTextColor(TEXT_PRIMARY);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        labels.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Smoothly blend the end of each track into the beginning of the next");
        subtitle.setTextColor(TEXT_SECONDARY);
        subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        subtitle.setPadding(0, dp(4), dp(12), 0);
        labels.addView(subtitle);

        row.addView(labels);

        Switch toggle = new Switch(this);
        boolean currentEnabled = prefs.getBoolean(KEY_ENABLED, true);
        toggle.setChecked(currentEnabled);
        toggle.setOnCheckedChangeListener((v, isChecked) -> {
            prefs.edit().putBoolean(KEY_ENABLED, isChecked).apply();
            updateProcessorField("sEnabled", isChecked);
        });
        row.addView(toggle);

        return row;
    }

    private View buildSessionControlToggle() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(20), dp(16), dp(20), dp(8));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout labels = new LinearLayout(this);
        labels.setOrientation(LinearLayout.VERTICAL);
        labels.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView title = new TextView(this);
        title.setText("Session control");
        title.setTextColor(TEXT_PRIMARY);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        labels.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Quickly pause or resume crossfade during a listening session");
        subtitle.setTextColor(TEXT_SECONDARY);
        subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        subtitle.setPadding(0, dp(4), dp(12), 0);
        labels.addView(subtitle);

        row.addView(labels);

        Switch toggle = new Switch(this);
        boolean current = prefs.getBoolean(KEY_SESSION_CONTROL, true);
        toggle.setChecked(current);
        toggle.setOnCheckedChangeListener((v, isChecked) -> {
            prefs.edit().putBoolean(KEY_SESSION_CONTROL, isChecked).apply();
            updateProcessorField("sSessionControl", isChecked);
        });
        row.addView(toggle);

        container.addView(row);

        TextView hint = new TextView(this);
        hint.setText("Long-press the shuffle button in the player to toggle crossfade on or off.");
        hint.setTextColor(TEXT_SECONDARY);
        hint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        hint.setPadding(0, dp(6), 0, dp(8));
        container.addView(hint);

        return container;
    }

    private View buildLongPressSlider() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(20), dp(16), dp(20), dp(16));

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = new TextView(this);
        title.setText("Long-press duration");
        title.setTextColor(TEXT_PRIMARY);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        title.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        titleRow.addView(title);

        int currentMs = prefs.getInt(KEY_LONG_PRESS_MS, 800);

        TextView valueLabel = new TextView(this);
        valueLabel.setText(currentMs + "ms");
        valueLabel.setTextColor(ACCENT_COLOR);
        valueLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        valueLabel.setTypeface(Typeface.DEFAULT_BOLD);
        titleRow.addView(valueLabel);

        container.addView(titleRow);

        TextView subtitle = new TextView(this);
        subtitle.setText("How long to hold the shuffle button to toggle crossfade (300–2000 ms)");
        subtitle.setTextColor(TEXT_SECONDARY);
        subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        subtitle.setPadding(0, dp(4), 0, dp(12));
        container.addView(subtitle);

        LinearLayout rangeRow = new LinearLayout(this);
        rangeRow.setOrientation(LinearLayout.HORIZONTAL);
        rangeRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView minLabel = new TextView(this);
        minLabel.setText("300");
        minLabel.setTextColor(TEXT_SECONDARY);
        minLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        rangeRow.addView(minLabel);

        SeekBar seekBar = new SeekBar(this);
        seekBar.setMax(17);
        seekBar.setProgress((currentMs - 300) / 100);
        seekBar.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        seekBar.setPadding(dp(8), 0, dp(8), 0);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                int ms = 300 + progress * 100;
                valueLabel.setText(ms + "ms");
            }

            @Override
            public void onStartTrackingTouch(SeekBar bar) {}

            @Override
            public void onStopTrackingTouch(SeekBar bar) {
                int ms = 300 + bar.getProgress() * 100;
                prefs.edit().putInt(KEY_LONG_PRESS_MS, ms).apply();
                invokeProcessorMethod("setLongPressThreshold", int.class, ms);
            }
        });
        rangeRow.addView(seekBar);

        TextView maxLabel = new TextView(this);
        maxLabel.setText("2000");
        maxLabel.setTextColor(TEXT_SECONDARY);
        maxLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        rangeRow.addView(maxLabel);

        container.addView(rangeRow);
        return container;
    }

    private View buildDurationSlider() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(20), dp(16), dp(20), dp(16));

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = new TextView(this);
        title.setText("Crossfade duration");
        title.setTextColor(TEXT_PRIMARY);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        title.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        titleRow.addView(title);

        int currentDuration = prefs.getInt(KEY_DURATION, 3);

        TextView valueLabel = new TextView(this);
        valueLabel.setText(currentDuration + "s");
        valueLabel.setTextColor(ACCENT_COLOR);
        valueLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        valueLabel.setTypeface(Typeface.DEFAULT_BOLD);
        titleRow.addView(valueLabel);

        container.addView(titleRow);

        TextView subtitle = new TextView(this);
        subtitle.setText("How long the crossfade effect lasts (1–12 seconds)");
        subtitle.setTextColor(TEXT_SECONDARY);
        subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        subtitle.setPadding(0, dp(4), 0, dp(12));
        container.addView(subtitle);

        LinearLayout rangeRow = new LinearLayout(this);
        rangeRow.setOrientation(LinearLayout.HORIZONTAL);
        rangeRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView minLabel = new TextView(this);
        minLabel.setText("1s");
        minLabel.setTextColor(TEXT_SECONDARY);
        minLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        rangeRow.addView(minLabel);

        SeekBar seekBar = new SeekBar(this);
        seekBar.setMax(11);
        seekBar.setProgress(currentDuration - 1);
        seekBar.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        seekBar.setPadding(dp(8), 0, dp(8), 0);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                valueLabel.setText((progress + 1) + "s");
            }

            @Override
            public void onStartTrackingTouch(SeekBar bar) {}

            @Override
            public void onStopTrackingTouch(SeekBar bar) {
                int duration = bar.getProgress() + 1;
                prefs.edit().putInt(KEY_DURATION, duration).apply();
                invokeProcessorMethod("setCrossfadeDuration", int.class, duration * 1000);
            }
        });
        rangeRow.addView(seekBar);

        TextView maxLabel = new TextView(this);
        maxLabel.setText("12s");
        maxLabel.setTextColor(TEXT_SECONDARY);
        maxLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        rangeRow.addView(maxLabel);

        container.addView(rangeRow);
        return container;
    }

    private View buildAdvancedModeToggle() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(20), dp(16), dp(20), dp(16));

        LinearLayout labels = new LinearLayout(this);
        labels.setOrientation(LinearLayout.VERTICAL);
        labels.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView title = new TextView(this);
        title.setText("Advanced duration");
        title.setTextColor(TEXT_PRIMARY);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        labels.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Specify crossfade duration in milliseconds for precise control");
        subtitle.setTextColor(TEXT_SECONDARY);
        subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        subtitle.setPadding(0, dp(4), dp(12), 0);
        labels.addView(subtitle);

        row.addView(labels);

        Switch toggle = new Switch(this);
        boolean current = prefs.getBoolean(KEY_ADVANCED_MODE, false);
        toggle.setChecked(current);
        toggle.setOnCheckedChangeListener((v, isChecked) -> {
            prefs.edit().putBoolean(KEY_ADVANCED_MODE, isChecked).apply();
            durationSliderView.setVisibility(isChecked ? View.GONE : View.VISIBLE);
            durationMsView.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            invokeProcessorMethod("setAdvancedMode", boolean.class, isChecked);
            if (isChecked) {
                int ms = prefs.getInt(KEY_DURATION_MS, 3000);
                invokeProcessorMethod("setCrossfadeDuration", int.class, ms);
            } else {
                int sec = prefs.getInt(KEY_DURATION, 3);
                invokeProcessorMethod("setCrossfadeDuration", int.class, sec * 1000);
            }
        });
        row.addView(toggle);

        return row;
    }

    private View buildDurationMsInput() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(20), dp(16), dp(20), dp(16));

        TextView title = new TextView(this);
        title.setText("Duration (milliseconds)");
        title.setTextColor(TEXT_PRIMARY);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        container.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Range: 500–30000 ms");
        subtitle.setTextColor(TEXT_SECONDARY);
        subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        subtitle.setPadding(0, dp(4), 0, dp(12));
        container.addView(subtitle);

        int currentMs = prefs.getInt(KEY_DURATION_MS, 3000);

        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(currentMs));
        input.setTextColor(TEXT_PRIMARY);
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        input.setHint("3000");
        input.setHintTextColor(DIVIDER_COLOR);

        GradientDrawable inputBg = new GradientDrawable();
        inputBg.setColor(SURFACE_COLOR);
        inputBg.setCornerRadius(dp(8));
        inputBg.setStroke(dp(1), ACCENT_COLOR);
        input.setBackground(inputBg);
        input.setPadding(dp(12), dp(10), dp(12), dp(10));

        input.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                scrollView.getViewTreeObserver().addOnGlobalLayoutListener(
                        new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                Rect r = new Rect();
                                scrollView.getWindowVisibleDisplayFrame(r);
                                int screenHeight = scrollView.getRootView().getHeight();
                                int keypadHeight = screenHeight - r.bottom;
                                if (keypadHeight > screenHeight * 0.15) {
                                    int[] loc = new int[2];
                                    input.getLocationInWindow(loc);
                                    int inputBottom = loc[1] + input.getHeight();
                                    int visibleBottom = r.bottom;
                                    if (inputBottom > visibleBottom) {
                                        scrollView.smoothScrollBy(0, inputBottom - visibleBottom + dp(24));
                                    }
                                }
                            }
                        });
            } else {
                applyMsDuration(input);
            }
        });

        input.setOnEditorActionListener((v, actionId, event) -> {
            applyMsDuration(input);
            return false;
        });

        container.addView(input);
        return container;
    }

    private void applyMsDuration(EditText input) {
        try {
            int ms = Integer.parseInt(input.getText().toString().trim());
            ms = Math.max(500, Math.min(30000, ms));
            input.setText(String.valueOf(ms));
            prefs.edit().putInt(KEY_DURATION_MS, ms).apply();
            invokeProcessorMethod("setCrossfadeDuration", int.class, ms);
        } catch (NumberFormatException ignored) {
            input.setText(String.valueOf(prefs.getInt(KEY_DURATION_MS, 3000)));
        }
    }

    private View buildVideoModeNote() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(20), dp(16), dp(20), dp(16));

        TextView title = new TextView(this);
        title.setText("Music videos disabled");
        title.setTextColor(TEXT_PRIMARY);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        row.addView(title);

        TextView subtitle = new TextView(this);
        boolean crossfadeOn = prefs.getBoolean(KEY_ENABLED, true);
        subtitle.setText(crossfadeOn
                ? "Video playback is automatically disabled while crossfade is active. "
                  + "Disable crossfade to restore video mode."
                : "Video playback will be disabled when crossfade is enabled.");
        subtitle.setTextColor(TEXT_SECONDARY);
        subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        subtitle.setPadding(0, dp(4), 0, 0);
        row.addView(subtitle);

        return row;
    }



    private View buildInfoCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(SURFACE_COLOR);
        bg.setCornerRadius(dp(12));
        card.setBackground(bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(dp(20), dp(16), dp(20), 0);
        card.setLayoutParams(params);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));

        TextView info = new TextView(this);
        info.setText("Crossfade captures the tail of the outgoing track and mixes " +
                "it with the head of the incoming track using equal-power curves. " +
                "Changes apply on the next track transition.");
        info.setTextColor(TEXT_SECONDARY);
        info.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        info.setLineSpacing(dp(2), 1f);
        card.addView(info);

        return card;
    }

    private View buildVersionFooter() {
        TextView version = new TextView(this);
        version.setText("VazerOG Patches v" + PATCH_VERSION);
        version.setTextColor(DIVIDER_COLOR);
        version.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        version.setGravity(Gravity.CENTER);
        version.setPadding(dp(20), dp(16), dp(20), dp(8));
        return version;
    }

    private View buildDivider() {
        View divider = new View(this);
        divider.setBackgroundColor(DIVIDER_COLOR);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(1));
        params.setMargins(dp(20), 0, dp(20), 0);
        divider.setLayoutParams(params);
        return divider;
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value,
                getResources().getDisplayMetrics());
    }

    private static void updateProcessorField(String fieldName, Object value) {
        try {
            Class<?> clazz = Class.forName("CrossfadeManager");
            if ("sEnabled".equals(fieldName)) {
                clazz.getMethod("setEnabled", boolean.class)
                     .invoke(null, value);
            } else if ("sSessionControl".equals(fieldName)) {
                clazz.getMethod("setSessionControlEnabled", boolean.class)
                     .invoke(null, value);
            }
        } catch (Exception ignored) {
        }
    }

    private static void invokeProcessorMethod(String name, Class<?> paramType, Object arg) {
        try {
            Class<?> clazz = Class.forName("CrossfadeManager");
            clazz.getMethod(name, paramType).invoke(null, arg);
        } catch (Exception ignored) {
        }
    }
}
