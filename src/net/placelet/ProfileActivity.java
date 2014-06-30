package net.placelet;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class ProfileActivity extends Activity implements OnClickListener {
    
    private TextView textView;
    private SharedPreferences prefs;
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	// Inflate the menu items for use in the action bar
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.action_bar, menu);
	return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	setContentView(R.layout.activity_profile);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(User.username);
	prefs = this.getSharedPreferences("net.placelet", Context.MODE_PRIVATE);
	//switchToProfile();
	textView = (TextView) findViewById(R.id.email);
	displayProfileInfo();
    }

    @Override
    public void onClick(View arg0) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	case R.id.action_logout:
	    Intent intent = new Intent(this, LoginActivity.class);
	    intent.putExtra("logout", "true");
	    startActivity(intent);
	    break;
	case R.id.action_upload:
	    switchToUpload();
	    break;
        case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            return true;
	case R.id.action_about:
	    Intent aboutIntent;
	    aboutIntent = new Intent(this, AboutActivity.class);
	    startActivity(aboutIntent);
	    break;
	case R.id.action_options:
	    switchToOptions();
	    break;
        default:
            return super.onOptionsItemSelected(item);
        }
	return true;
    }

    @Override
    protected void onStart() {
	textView.setText(getString(R.string.profile_loading) + User.username + ".");
	ProfileActivity.this.setProgressBarIndeterminateVisibility(true);
	super.onStart();
    }
    
    private class ProfileInfo extends AsyncTask<String, String, JSONObject> {
	@Override
	protected JSONObject doInBackground(String... params) {
	    String username = params[0];
	    User user = new User(prefs);
	    JSONObject login = user.getProfileInfo(username);
	    return login;
	}

	@Override
	protected void onPostExecute(JSONObject result) {
	    ProfileActivity.this.setProgressBarIndeterminateVisibility(false);
	    String email = "";
	    try {
		email = result.getString("email");
	    } catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	    textView.setText(email);
	}
	
    }
    
    private void displayProfileInfo() {
	ProfileInfo login = new ProfileInfo();
	login.execute(User.username);
    }
    
    private void switchToUpload() {
	if(User.username != User.NOT_LOGGED_IN) {
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