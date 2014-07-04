package net.placelet;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class OptionsActivity extends Activity implements OnClickListener {
	private SharedPreferences prefs;
	private Button toggleDownload;
	private boolean downloadPermitted;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
		getActionBar().setTitle(R.string.options);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	public static class SettingsFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.preferences);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!User.username.equals(User.NOT_LOGGED_IN)) {
			// Inflate the menu items for use in the action bar
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.action_bar, menu);
			MenuItem item = menu.findItem(R.id.action_reload);
			item.setVisible(false);
			invalidateOptionsMenu();
		}
		return super.onCreateOptionsMenu(menu);
	}

	private void setToggleDownloadText() {
		if (downloadPermitted) {
			toggleDownload.setText(R.string.deactivate_download);
		} else {
			toggleDownload.setText(R.string.activate_download);
		}

	}

	private void toggleDownloadOptions() {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean("downloadPermitted", !downloadPermitted);
		editor.commit();
		downloadPermitted = prefs.getBoolean("downloadPermitted", false);
		setToggleDownloadText();
		if (downloadPermitted)
			Toast.makeText(this, R.string.pic_download_activated, Toast.LENGTH_LONG).show();
		else
			Toast.makeText(this, R.string.pic_download_deactivated, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.toggleDownload:
				toggleDownloadOptions();
				break;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return NavigateActivities.activitySwitchMenu(item, this);
	}

}