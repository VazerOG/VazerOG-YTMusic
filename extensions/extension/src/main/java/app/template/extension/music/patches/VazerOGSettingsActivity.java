package app.template.extension.music.patches;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

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

    private static final int BG_COLOR = 0xFF121212;
    private static final int SURFACE_COLOR = 0xFF1E1E1E;
    private static final int TEXT_PRIMARY = 0xFFE0E0E0;
    private static final int TEXT_SECONDARY = 0xFF9E9E9E;
    private static final int ACCENT_COLOR = 0xFFBB86FC;
    private static final int DIVIDER_COLOR = 0xFF2C2C2C;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setTitle("VazerOG");
        }

        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(BG_COLOR);
        scrollView.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(0, dp(8), 0, dp(24));

        root.addView(buildSectionHeader("Player"));
        root.addView(buildCrossfadeToggle());
        root.addView(buildDivider());
        root.addView(buildDurationSlider());
        root.addView(buildDivider());
        root.addView(buildVideoModeNote());
        root.addView(buildDivider());
        root.addView(buildInfoCard());

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
