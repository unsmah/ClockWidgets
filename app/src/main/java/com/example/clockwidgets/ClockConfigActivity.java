package com.example.clockwidgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Settings of one clock widget.
 *
 * <p>The activity is declared with {@code android.appwidget.action.APPWIDGET_CONFIGURE}, so the
 * launcher opens it right after the widget is dropped on the home screen: it must return
 * {@code RESULT_OK} together with the app widget id for the widget to be kept. Placed widgets
 * open the same screen when the user taps them.</p>
 */
public class ClockConfigActivity extends AppCompatActivity {

    /** Used as a fallback when the widget info is no longer available. */
    public static final String EXTRA_WIDGET_KIND = "com.example.clockwidgets.extra.WIDGET_KIND";

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private int kind = ClockRemoteViews.KIND_DIGITAL;
    private ClockConfig config;

    private FrameLayout previewHost;
    private RadioGroup formatGroup;
    private RadioGroup backgroundGroup;
    private CheckBox secondsCheck;
    private CheckBox dateCheck;
    private LinearLayout accentRow;
    private final List<View> accentSwatches = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If the user leaves this screen while adding a widget, the widget must not be created.
        setResult(RESULT_CANCELED);
        setContentView(R.layout.activity_clock_config);

        Intent intent = getIntent();
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        kind = intent.getIntExtra(EXTRA_WIDGET_KIND, ClockRemoteViews.KIND_DIGITAL);

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Toast.makeText(this, R.string.cfg_missing_id, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        AppWidgetProviderInfo info = AppWidgetManager.getInstance(this).getAppWidgetInfo(appWidgetId);
        if (info != null && info.provider != null) {
            kind = ClockRemoteViews.kindFor(info.provider);
        }

        config = ClockPrefs.load(this, appWidgetId, kind);

        previewHost = findViewById(R.id.config_preview);
        formatGroup = findViewById(R.id.format_group);
        backgroundGroup = findViewById(R.id.background_group);
        secondsCheck = findViewById(R.id.check_seconds);
        dateCheck = findViewById(R.id.check_date);
        accentRow = findViewById(R.id.accent_row);

        // The analog designs have no digital text, so those options would do nothing there.
        if (!ClockRemoteViews.hasDigitalTime(kind)) {
            findViewById(R.id.format_label).setVisibility(View.GONE);
            formatGroup.setVisibility(View.GONE);
            secondsCheck.setVisibility(View.GONE);
            findViewById(R.id.accent_label).setVisibility(View.GONE);
            accentRow.setVisibility(View.GONE);
        }

        ((RadioButton) findViewById(config.use24Hour ? R.id.radio_24 : R.id.radio_12)).setChecked(true);
        ((RadioButton) findViewById(backgroundRadioFor(config.backgroundStyle))).setChecked(true);
        secondsCheck.setChecked(config.showSeconds);
        dateCheck.setChecked(config.showDate);

        buildAccentSwatches();

        formatGroup.setOnCheckedChangeListener((group, checkedId) -> {
            config.use24Hour = checkedId == R.id.radio_24;
            renderPreview();
        });
        backgroundGroup.setOnCheckedChangeListener((group, checkedId) -> {
            config.backgroundStyle = backgroundStyleFor(checkedId);
            renderPreview();
        });
        secondsCheck.setOnCheckedChangeListener((button, checked) -> {
            config.showSeconds = checked;
            renderPreview();
        });
        dateCheck.setOnCheckedChangeListener((button, checked) -> {
            config.showDate = checked;
            renderPreview();
        });

        findViewById(R.id.btn_save).setOnClickListener(view -> save());
        findViewById(R.id.btn_cancel).setOnClickListener(view -> finish());

        renderPreview();
    }

    private static int backgroundRadioFor(int backgroundStyle) {
        switch (backgroundStyle) {
            case ClockConfig.BG_LIGHT:
                return R.id.radio_bg_light;
            case ClockConfig.BG_TRANSPARENT:
                return R.id.radio_bg_transparent;
            default:
                return R.id.radio_bg_dark;
        }
    }

    private static int backgroundStyleFor(int radioId) {
        if (radioId == R.id.radio_bg_light) {
            return ClockConfig.BG_LIGHT;
        }
        if (radioId == R.id.radio_bg_transparent) {
            return ClockConfig.BG_TRANSPARENT;
        }
        return ClockConfig.BG_DARK;
    }

    private void buildAccentSwatches() {
        accentRow.removeAllViews();
        accentSwatches.clear();

        float density = getResources().getDisplayMetrics().density;
        int size = Math.round(30 * density);
        int gap = Math.round(10 * density);

        for (int i = 0; i < ClockPalette.ACCENT_RES.length; i++) {
            final int color = getColor(ClockPalette.ACCENT_RES[i]);
            View swatch = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(0, 0, gap, 0);
            params.gravity = Gravity.CENTER_VERTICAL;
            swatch.setLayoutParams(params);
            swatch.setContentDescription(getString(ClockPalette.ACCENT_LABEL_RES[i]));
            swatch.setOnClickListener(view -> {
                config.accentColor = color;
                updateAccentSwatches();
                renderPreview();
            });
            accentRow.addView(swatch);
            accentSwatches.add(swatch);
        }
        updateAccentSwatches();
    }

    private void updateAccentSwatches() {
        for (int i = 0; i < accentSwatches.size(); i++) {
            int color = getColor(ClockPalette.ACCENT_RES[i]);
            ClockPalette.styleAccentSwatch(this, accentSwatches.get(i), color,
                    color == config.accentColor);
        }
    }

    /** Shows the real widget layout inside the screen, using the current settings. */
    private void renderPreview() {
        if (previewHost == null || config == null) {
            return;
        }
        previewHost.removeAllViews();
        RemoteViews views = ClockRemoteViews.build(this, appWidgetId, kind, config, false);
        previewHost.addView(views.apply(this, previewHost));
    }

    private void save() {
        ClockPrefs.save(this, appWidgetId, config);
        AppWidgetManager.getInstance(this).updateAppWidget(appWidgetId,
                ClockRemoteViews.build(this, appWidgetId, kind, config));

        Intent result = new Intent();
        result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, result);

        Toast.makeText(this, R.string.cfg_saved, Toast.LENGTH_SHORT).show();
        finish();
    }
}