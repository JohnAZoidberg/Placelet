package net.placelet;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.Menu;
import android.view.MenuItem;

public class OptionsActivity extends Activity {
    private static OptionsActivity activity;
    private static SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
		getActionBar().setTitle(R.string.options);
		getActionBar().setDisplayHomeAsUpEnabled(true);
        activity = this;
        prefs = this.getSharedPreferences("net.placelet", Context.MODE_PRIVATE);
	}

	public static class SettingsFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preferences);
            Preference resetUpdate = findPreference("pref_reset_update");
            resetUpdate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Util.resetUpdates(prefs);
                    Util.alert(activity.getString(R.string.will_be_reloaded), activity);
                    return false;
                }
            });
		}
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Util.inflateActionBar(this, menu, true);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return NavigateActivities.activitySwitchMenu(item, this);
	}
}