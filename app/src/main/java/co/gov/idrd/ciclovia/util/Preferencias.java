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

    public static double getlatitude(Context context) {
        return Double.parseDouble(getPrefs(context).getString("userlast_latitude", "0"));
    }

    public static void setlatitude(Context context, double input) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString("userlast_latitude", Double.toString( input));
        editor.commit();
    }

    public static double getlongitude(Context context) {
        return Double.parseDouble(getPrefs(context).getString("userlast_longitude", "0"));
    }

    public static void setlongitude(Context context, double input) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString("userlast_longitude",  Double.toString(input));
        editor.commit();
    }


    public static Float getzoom(Context context) {
        return getPrefs(context).getFloat("userlast_zoom", 0);
    }

    public static void setzoom(Context context, Float input) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putFloat("userlast_zoom", input);
        editor.commit();
    }

    public static Float gettilt(Context context) {
        return getPrefs(context).getFloat("userlast_tilt", 0);
    }

    public static void settilt(Context context, Float input) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putFloat("userlast_tilt", input);
        editor.commit();
    }


    public static Float getbearing(Context context) {
        return getPrefs(context).getFloat("userlast_bearing", 0);
    }

    public static void setbearing(Context context, Float input) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putFloat("userlast_bearing", input);
        editor.commit();
    }


}