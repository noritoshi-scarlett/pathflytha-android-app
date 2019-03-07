package pl.noritoshi_scarlett.pathflytha.fragments_calculate;


import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v4.app.Fragment;

import pl.noritoshi_scarlett.pathflytha.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        // APP VERSION
        String appVersion;
        try {
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            appVersion = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            appVersion = "unknown";
        }

        // DEFAULT SETS
        findPreference("preference_version_app").setSummary(appVersion);
        loadPreferences();
    }

    @Override
    public void onResume() {
        super.onResume();

        loadPreferences();
    }

    @Override
    public void onPause() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = getPreferenceScreen().findPreference(key);
        setSummary(pref);
    }

    private void loadPreferences() {
        sharedPreferences = getPreferenceManager().getSharedPreferences();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        PreferenceScreen preferenceScreen = getPreferenceScreen();
        for(int i = 0; i < preferenceScreen.getPreferenceCount(); i++) {
            setSummary(getPreferenceScreen().getPreference(i));
        }
    }

    private void setSummary(Preference pref) {
        if (pref instanceof EditTextPreference) {
            updateSummary((EditTextPreference) pref);
        }
        else if (pref instanceof PreferenceCategory) {
            for(int i = 0; i < ((PreferenceCategory) pref).getPreferenceCount(); i++) {
                setSummary(((PreferenceCategory) pref).getPreference(i));
            }
        }
    }

    private void updateSummary(EditTextPreference preference) {
        preference.setSummary(preference.getText());
    }
}
