package net.placelet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_about);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(R.string.app_name);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	if (!User.username.equals(User.NOT_LOGGED_IN)) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.action_bar, menu);
	}
	return super.onCreateOptionsMenu(menu);
    }

    private void switchToProfile() {
	if (!User.username.equals(User.NOT_LOGGED_IN)) {
	    Intent profileIntent = new Intent(this, ProfileActivity.class);
	    startActivity(profileIntent);
	}
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	case R.id.action_logout:
	    Intent loginIntent = new Intent(this, LoginActivity.class);
	    loginIntent.putExtra("logout", "true");
	    startActivity(loginIntent);
	    break;
	case R.id.action_profile:
	    switchToProfile();
	    break;
	case android.R.id.home:
	    switchToMainActivity();
	    return true;
	case R.id.action_upload:
	    switchToUpload();
	    break;
	case R.id.action_about:
	    switchToUpload();
	    break;
	case R.id.action_options:
	    switchToOptions();
	    break;
	default:
	    return super.onOptionsItemSelected(item);
	}
	return true;
    }

    private void switchToMainActivity() {
	Intent mainIntent = new Intent(this, MainActivity.class);
	startActivity(mainIntent);
    }

    private void switchToUpload() {
	if (!User.username.equals(User.NOT_LOGGED_IN)) {
	    Intent uploadIntent = new Intent(this, UploadActivity.class);
	    startActivity(uploadIntent);
	}
    }

    private void switchToOptions() {
		if (User.username != User.NOT_LOGGED_IN) {
			Intent profileIntent = new Intent(this, OptionsActivity.class);
			startActivity(profileIntent);
		}
    }
}