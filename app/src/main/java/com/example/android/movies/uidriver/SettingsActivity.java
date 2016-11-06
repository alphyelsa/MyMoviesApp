package com.example.android.movies.uidriver;


import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object key) {

        ListPreference listPref = (ListPreference) preference;
        int index = listPref.findIndexOfValue((String) key);
        //if(key.equals(KEY_PREF_POPULAR))
        //{
            //preference.
        //}else{

        //}
        //String stringValue = newValue.toString();
        //preference.setSummary(stringValue);
        return true;
    }
}
