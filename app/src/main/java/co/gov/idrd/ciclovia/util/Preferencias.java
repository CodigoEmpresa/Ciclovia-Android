package co.gov.idrd.ciclovia.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by daniel on 24/11/17.
 */

public class Preferencias {
    private SharedPreferences sharedPreferences;
    private static String PREF_NAME = "prefs";

    public Preferencias() {
        // Blank
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static String getUsername(Context context) {
        return getPrefs(context).getString("username_key", "");
    }

    public static void setUsername(Context context, String input) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString("username_key", input);
        editor.commit();
    }
}