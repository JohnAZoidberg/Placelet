package net.placelet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
            setPreferenceOnClickListeners();
		}

        private void setPreferenceOnClickListeners() {
            Preference resetUpdatePref = findPreference("pref_reset_update");
            resetUpdatePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Util.resetUpdates(prefs);
                    Util.alert(activity.getString(R.string.will_be_reloaded), activity);
                    return true;
                }
            });

            Preference websitePref = findPreference("pref_website");
            websitePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    String url = "http://www.placelet.de";
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
            });

            Preference devPref = findPreference("pref_developer");
            devPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("message/rfc822");
                    intent.putExtra(Intent.EXTRA_EMAIL, "ds@struckmeierfliesen.de");
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Placelet - App");

                    startActivity(Intent.createChooser(intent, activity.getString(R.string.send_email)));
                    return true;
                }
            });

            Preference versionPref = findPreference("pref_version");
            versionPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("market://details?id=net.placelet"));
                    startActivity(intent);
                    return true;
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