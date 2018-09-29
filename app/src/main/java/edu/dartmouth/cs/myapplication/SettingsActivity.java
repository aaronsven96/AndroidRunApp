package edu.dartmouth.cs.myapplication;

import android.content.Intent;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //set up toolbar
        Toolbar myToolbar = findViewById(R.id.toolbar_settings);
        setSupportActionBar(myToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //set fragment
        PrefsFragment mPrefsFragment = new PrefsFragment();
        getFragmentManager().beginTransaction().replace(R.id.content_frame, mPrefsFragment).commit();

    }
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.action_bar_settings, menu);
        return true;
    }

    public static class PrefsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.fragment_settings);
            //set intent to log out
            PreferenceScreen screen = (PreferenceScreen) findPreference("SIGN_INDEX");
            Intent intent = new Intent(getActivity(), SignInActivity.class);
            intent.putExtra("key_sign_in",true);
            screen.setIntent(intent);
        }
    }
}
