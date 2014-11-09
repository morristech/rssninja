package com.yairkukielka.rssninja.settings;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

import com.yairkukielka.rssninja.R;


public class PreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    public static final String KEY_ONLY_UNREAD = "pref_key_only_unread";
    public static final String KEY_TEXT_SIZE = "pref_key_article_text_size";
    public static final String KEY_SHOW_ARTICLE_TITLE = "pref_key_show_article_title";
    public static final String KEY_LOGOUT = "pref_key_logout";
    public static final String KEY_LIST_WITH_CARDS = "pref_key_list_with_cards";
    public static final String KEY_LICENCES = "pref_key_licences";
    public static final String KEY_RATE_APP = "pref_rate_app";
    public static int PREFERENCES_CODE = 1;
    public static boolean LOG_OUT = false;

    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (!key.equals(KEY_SHOW_ARTICLE_TITLE) && !key.equals(KEY_TEXT_SIZE)) {
            setResult(PREFERENCES_CODE);
        }
    }

    @Override
    public void onCreate(Bundle aSavedState) {
        super.onCreate(aSavedState);
        addPreferencesFromResource(R.xml.preferences);
        // action bar icon navagable up
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Preference logoutPref = (Preference) findPreference(KEY_LOGOUT);
        logoutPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                // return the code to reset the accessToken
                if (KEY_LOGOUT.equals(preference.getKey())) {
                    LOG_OUT = true;
                }
                setResult(PREFERENCES_CODE);
                finish();
                return true;
            }
        });
        Preference thanksPref = (Preference) findPreference(KEY_LICENCES);
        thanksPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                // return the code to reset the accessToken
                if (KEY_LICENCES.equals(preference.getKey())) {
                    Intent licencesIntent = new Intent(PreferencesActivity.this, LicencesActivity.class);
                    startActivity(licencesIntent);
                }
                return true;
            }

        });

        Preference ratePref = (Preference) findPreference(KEY_RATE_APP);
        ratePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Uri uri = Uri.parse("market://details?id=" + getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    showMarketErrorDialogcontext(PreferencesActivity.this);
                }
                return true;
            }

        });
    }

    private void showMarketErrorDialogcontext(Context context) {
        new AlertDialog.Builder(context).setTitle("Error")
                .setMessage(getResources().getString(R.string.error_opening_market))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                }).setIcon(android.R.drawable.ic_dialog_alert).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.open_main, R.anim.close_next);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
