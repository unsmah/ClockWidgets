package com.example.clockwidgets;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

/** Accent colours and background styles offered by the configuration screen. */
public final class ClockPalette {

    /** Selectable accent colours (time text colour). */
    public static final int[] ACCENT_RES = {
            R.color.accent_blue,
            R.color.accent_cyan,
            R.color.accent_green,
            R.color.accent_amber,
            R.color.accent_pink,
            R.color.accent_white
    };

    /** Labels of {@link #ACCENT_RES}, same order. */
    public static final int[] ACCENT_LABEL_RES = {
            R.string.accent_blue,
            R.string.accent_cyan,
            R.string.accent_green,
            R.string.accent_amber,
            R.string.accent_pink,
            R.string.accent_white
    };

    /** Selectable background styles, same order as {@link #BACKGROUND_LABEL_RES}. */
    public static final int[] BACKGROUND_STYLES = {
            ClockConfig.BG_DARK,
            ClockConfig.BG_LIGHT,
            ClockConfig.BG_TRANSPARENT
    };

    /** Labels of {@link #BACKGROUND_STYLES}, same order. */
    public static final int[] BACKGROUND_LABEL_RES = {
            R.string.cfg_bg_dark,
            R.string.cfg_bg_light,
            R.string.cfg_bg_transparent
    };

    private ClockPalette() {
    }

    public static int labelForAccent(Context context, int color) {
        for (int i = 0; i < ACCENT_RES.length; i++) {
            if (context.getColor(ACCENT_RES[i]) == color) {
                return ACCENT_LABEL_RES[i];
            }
        }
        return ACCENT_LABEL_RES[0];
    }

    public static int labelForBackground(int backgroundStyle) {
        for (int i = 0; i < BACKGROUND_STYLES.length; i++) {
            if (BACKGROUND_STYLES[i] == backgroundStyle) {
                return BACKGROUND_LABEL_RES[i];
            }
        }
        return BACKGROUND_LABEL_RES[0];
    }

    /** Draws the round colour swatch used in the config screen. */
    public static void styleAccentSwatch(Context context, View swatch, int color, boolean selected) {
        float density = context.getResources().getDisplayMetrics().density;
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(color);
        shape.setStroke((int) ((selected ? 3 : 1) * density),
                context.getColor(selected ? R.color.accent_selected_border : R.color.card_border));
        swatch.setBackground(shape);
    }
}