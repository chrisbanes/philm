package app.philm.in.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import app.philm.in.R;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.main_preferences);
    }
}
