package net.placelet;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		// initiate ActionBar
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setTitle(R.string.app_name);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!User.username.equals(User.NOT_LOGGED_IN)) {
			// Inflate the menu items for use in the action bar
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.action_bar, menu);
			MenuItem item = menu.findItem(R.id.action_reload);
			// disable reload button
			item.setVisible(false);
			item.setEnabled(false);
			invalidateOptionsMenu();
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return NavigateActivities.activitySwitchMenu(item, this);
	}
}