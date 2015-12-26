package ru.home.beywer.mobi3.activites;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

import ru.home.beywer.mobi3.R;

public class Preferences  extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "Preferences";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MainActivity.shown = true;
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);

        SharedPreferences mPrefs;
        Preference mLogin;
        Preference mPassword;
        Preference mHost;

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mPrefs.registerOnSharedPreferenceChangeListener(this);

        mLogin = findPreference("login");
        mPassword = findPreference("password");
        mHost = findPreference("host");

        String summary = mPrefs.getString("login", "");
        mLogin.setSummary(summary);
        summary = mPrefs.getString("password", "");
        mPassword.setSummary(summary);
        summary = mPrefs.getString("host", "");
        mHost.setSummary(summary);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "Pref changed " + key + "   " + sharedPreferences.getString(key, "default"));
        findPreference(key).setSummary(sharedPreferences.getString(key, ""));
    }

    @Override
    protected void onDestroy() {
        MainActivity.shown = false;
        super.onDestroy();
    }
}
