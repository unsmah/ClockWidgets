package com.example.clockwidgets;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Main screen: a live preview of each clock widget, a one-tap "add to home screen" action and
 * the list of clocks that are currently placed on the home screen.
 */
public class MainActivity extends AppCompatActivity {

    private static final int[] KINDS = {
            ClockRemoteViews.KIND_DIGITAL,
            ClockRemoteViews.KIND_ANALOG,
            ClockRemoteViews.KIND_MINIMAL
    };

    private static final int[] PREVIEW_HOST_IDS = {
            R.id.preview_digital,
            R.id.preview_analog,
            R.id.preview_minimal
    };

    private static final int[] ADD_BUTTON_IDS = {
            R.id.add_digital,
            R.id.add_analog,
            R.id.add_minimal
    };

    private final Handler handler = new Handler(Looper.getMainLooper());
    private LinearLayout placedContainer;
    private TextView placedEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        placedContainer = findViewById(R.id.placed_container);
        placedEmpty = findViewById(R.id.placed_empty);

        for (int i = 0; i < KINDS.length; i++) {
            final int kind = KINDS[i];
            findViewById(ADD_BUTTON_IDS[i]).setOnClickListener(view -> requestWidget(kind));
            renderPreview(PREVIEW_HOST_IDS[i], kind);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showPlacedWidgets();
    }

    /** Renders the real widget layout with its default settings inside the card. */
    private void renderPreview(int hostId, int kind) {
        FrameLayout host = findViewById(hostId);
        host.removeAllViews();
        ClockConfig config = ClockConfig.defaults(this, kind);
        RemoteViews views = ClockRemoteViews.build(this, AppWidgetManager.INVALID_APPWIDGET_ID,
                kind, config, false);
        host.addView(views.apply(this, host));
    }

    /** Asks the launcher to place the widget on the home screen (Android 8.0 and newer). */
    private void requestWidget(int kind) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && appWidgetManager.isRequestPinAppWidgetSupported()) {
            ComponentName provider = new ComponentName(this, ClockRemoteViews.providerClass(kind));
            appWidgetManager.requestPinAppWidget(provider, null, null);
            Toast.makeText(this,
                    getString(R.string.pin_requested, getString(ClockRemoteViews.nameResFor(kind))),
                    Toast.LENGTH_LONG).show();
            // The launcher adds the widget asynchronously, refresh the list a moment later.
            handler.postDelayed(this::showPlacedWidgets, 1500);
        } else {
            Toast.makeText(this, R.string.pin_unsupported, Toast.LENGTH_LONG).show();
        }
    }

    /** Lists the clocks that are placed on the home screen right now. */
    private void showPlacedWidgets() {
        if (placedContainer == null || isFinishing()) {
            return;
        }
        placedContainer.removeAllViews();

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        int placed = 0;

        for (int kind : KINDS) {
            ComponentName provider = new ComponentName(this, ClockRemoteViews.providerClass(kind));
            for (int appWidgetId : appWidgetManager.getAppWidgetIds(provider)) {
                placed++;
                ClockConfig config = ClockPrefs.load(this, appWidgetId, kind);

                View row = inflater.inflate(R.layout.row_widget, placedContainer, false);
                TextView title = row.findViewById(R.id.row_title);
                TextView summary = row.findViewById(R.id.row_summary);

                title.setText(ClockRemoteViews.nameResFor(kind));
                summary.setText(getString(R.string.row_summary_format,
                        getString(config.use24Hour ? R.string.cfg_format_24 : R.string.cfg_format_12),
                        getString(ClockPalette.labelForAccent(this, config.accentColor)),
                        getString(ClockPalette.labelForBackground(config.backgroundStyle))));

                final int widgetId = appWidgetId;
                final int widgetKind = kind;
                row.findViewById(R.id.row_configure)
                        .setOnClickListener(view -> openConfig(widgetId, widgetKind));
                placedContainer.addView(row);
            }
        }

        placedContainer.setVisibility(placed == 0 ? View.GONE : View.VISIBLE);
        placedEmpty.setText(placed == 0
                ? getText(R.string.placed_empty)
                : getString(R.string.placed_count, placed));
    }

    private void openConfig(int appWidgetId, int kind) {
        Intent intent = new Intent(this, ClockConfigActivity.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putExtra(ClockConfigActivity.EXTRA_WIDGET_KIND, kind);
        startActivity(intent);
    }
}