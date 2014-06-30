package net.placelet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
	setContentView(R.layout.activity_options);
	prefs = this.getSharedPreferences("net.placelet", Context.MODE_PRIVATE);
	downloadPermitted = prefs.getBoolean("downloadPermitted", false);
	toggleDownload = (Button) findViewById(R.id.toggleDownload);
	setToggleDownloadText();
	getActionBar().setTitle(R.string.options);
	getActionBar().setDisplayHomeAsUpEnabled(true);
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
	if(downloadPermitted) Toast.makeText(this, R.string.pic_download_activated, Toast.LENGTH_LONG).show();
	else Toast.makeText(this, R.string.pic_download_deactivated, Toast.LENGTH_LONG).show();
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
	switch (item.getItemId()) {
	case R.id.action_logout:
	    Intent intent = new Intent(this, LoginActivity.class);
	    intent.putExtra("logout", "true");
	    startActivity(intent);
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
	case R.id.action_options:
	    switchToOptions();
	    break;
	case R.id.action_about:
	    Intent aboutIntent;
	    aboutIntent = new Intent(this, AboutActivity.class);
	    startActivity(aboutIntent);
	    break;

	default:
	    return super.onOptionsItemSelected(item);
	}
	return true;
    }

    private void switchToOptions() {
	if (User.username != User.NOT_LOGGED_IN) {
	    Intent profileIntent = new Intent(this, OptionsActivity.class);
	    startActivity(profileIntent);
	}
    }

    private void switchToMainActivity() {
	Intent mainIntent = new Intent(this, MainActivity.class);
	startActivity(mainIntent);
    }

    private void switchToProfile() {
	if (User.username != User.NOT_LOGGED_IN) {
	    Intent profileIntent = new Intent(this, ProfileActivity.class);
	    startActivity(profileIntent);
	}
    }

    private void switchToUpload() {
	if (User.username != User.NOT_LOGGED_IN) {
	    Intent uploadIntent = new Intent(this, UploadActivity.class);
	    startActivity(uploadIntent);
	}
    }

}