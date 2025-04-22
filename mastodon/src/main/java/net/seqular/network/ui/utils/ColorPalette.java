package net.seqular.network.ui.utils;

import static net.seqular.network.GlobalUserPreferences.ThemePreference;
import static net.seqular.network.GlobalUserPreferences.trueBlackTheme;
import static net.seqular.network.api.session.AccountLocalPreferences.ColorPreference.*;

import android.content.Context;
import android.content.res.Resources;

import androidx.annotation.StyleRes;

import net.seqular.network.GlobalUserPreferences;
import net.seqular.network.R;
import net.seqular.network.api.session.AccountLocalPreferences;

import java.util.Map;

public class ColorPalette {
    public static final Map<AccountLocalPreferences.ColorPreference, ColorPalette> palettes = Map.of(
            MATERIAL3, new ColorPalette(R.style.ColorPalette_Material3)
                    .dark(R.style.ColorPalette_Material3_Dark, R.style.ColorPalette_Material3_AutoLightDark),
            PINK, new ColorPalette(R.style.ColorPalette_Pink),
            PURPLE, new ColorPalette(R.style.ColorPalette_Purple),
            GREEN, new ColorPalette(R.style.ColorPalette_Green),
            BLUE, new ColorPalette(R.style.ColorPalette_Blue),
            BROWN, new ColorPalette(R.style.ColorPalette_Brown),
            RED, new ColorPalette(R.style.ColorPalette_Red),
            YELLOW, new ColorPalette(R.style.ColorPalette_Yellow),
			NORD, new ColorPalette(R.style.ColorPalette_Nord),
			WHITE, new ColorPalette(R.style.ColorPalette_White)

	);

    private @StyleRes int base;
    private @StyleRes int autoDark;
    private @StyleRes int light;
    private @StyleRes int dark;
    private @StyleRes int black;
    private @StyleRes int autoBlack;

    public ColorPalette(@StyleRes int baseRes) { base = baseRes; }

    public ColorPalette(@StyleRes int lightRes, @StyleRes int darkRes, @StyleRes int autoDarkRes, @StyleRes int blackRes, @StyleRes int autoBlackRes) {
        light = lightRes;
        dark = darkRes;
        autoDark = autoDarkRes;
        black = blackRes;
        autoBlack = autoBlackRes;
    }

    public ColorPalette light(@StyleRes int res) { light = res; return this; }
    public ColorPalette dark(@StyleRes int res, @StyleRes int auto) { dark = res; autoDark = auto; return this; }
    public ColorPalette black(@StyleRes int res, @StyleRes int auto) { dark = res; autoBlack = auto; return this; }

	public void apply(Context context) {
		apply(context, GlobalUserPreferences.theme);
	}

    public void apply(Context context, ThemePreference theme) {
        if (!((dark != 0 && autoDark != 0) || (black != 0 && autoBlack != 0) || light != 0 || base != 0)) {
            throw new IllegalStateException("Invalid color scheme definition");
        }

        Resources.Theme t = context.getTheme();
		t.applyStyle(R.style.ColorPalette_Fallback, true);
        if (base != 0) t.applyStyle(base, true);
        if (light != 0 && theme.equals(ThemePreference.LIGHT)) {
			t.applyStyle(light, true);
		} else if (theme.equals(ThemePreference.DARK)) {
			t.applyStyle(R.style.ColorPalette_Dark, true);
			if (trueBlackTheme) t.applyStyle(R.style.ColorPalette_Dark_TrueBlack, true);
            if (dark != 0 && !trueBlackTheme) t.applyStyle(dark, true);
            else if (black != 0 && trueBlackTheme) t.applyStyle(black, true);
        } else if (theme.equals(ThemePreference.AUTO)) {
			t.applyStyle(R.style.ColorPalette_AutoLightDark, true);
			if (trueBlackTheme) t.applyStyle(R.style.ColorPalette_AutoLightDark_TrueBlack, true);
            if (autoDark != 0 && !trueBlackTheme) t.applyStyle(autoDark, true);
            else if (autoBlack != 0 && trueBlackTheme) t.applyStyle(autoBlack, true);
        }
    }
}
