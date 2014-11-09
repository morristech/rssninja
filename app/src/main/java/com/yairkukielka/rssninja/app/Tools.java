package com.yairkukielka.rssninja.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.yairkukielka.rssninja.settings.PreferencesActivity;


public class Tools {

    /**
     * Get only unread articles setting from preferences
     *
     * @param context context
     * @return true if only unread articles
     */
    public static Boolean getPrefOnlyUnread(Context context) {
        if (context != null) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            return sharedPref.getBoolean(PreferencesActivity.KEY_ONLY_UNREAD, false);
        }
        return null;
    }
}
