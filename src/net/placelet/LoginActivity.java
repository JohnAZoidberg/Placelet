package net.placelet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity implements OnClickListener {

	private TextView textView;
	private SharedPreferences prefs;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, R.id.action_about, 0, R.string.about).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_login);
		// getActionBar().setDisplayHomeAsUpEnabled(true);
		prefs = this.getSharedPreferences("net.placelet", Context.MODE_PRIVATE);
		textView = (TextView) findViewById(R.id.textView1);
	}

	@Override
	protected void onStart() {
		Intent intent = getIntent();
		if (intent.hasExtra("logout")) {
			String logout = intent.getStringExtra("logout");
			if (logout.equals("true")) {
				User user = new User(prefs);
				user.logout();
			}
		}
		super.onStart();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.button2:
				login();
				break;
		}
	}

	private class Login extends AsyncTask<String, String, Integer> {
		@Override
		protected Integer doInBackground(String... params) {
			String username = params[0];
			String pw = params[1];
			User user = new User(prefs);
			Integer login = user.login(username, pw);
			return login;
		}

		@Override
		protected void onPostExecute(Integer result) {
			if (result == User.SUCCESS) {
				switchToMainActivity();
			} else if (result == 2) {
				textView.setText(getString(R.string.account_not_verified));
			} else if (result == 0) {
				textView.setText(getString(R.string.account_notextisting));
			} else if (result == 1) {
				textView.setText(getString(R.string.wrong_pasword));
			} else {
				textView.setText(result.toString());
				LoginActivity.this.setProgressBarIndeterminateVisibility(false);
			}
		}

		@Override
		protected void onPreExecute() {
			LoginActivity.this.setProgressBarIndeterminateVisibility(true);
			super.onPreExecute();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return NavigateActivities.activitySwitchMenu(item, this);
	}

	private void switchToMainActivity() {
		/*
		 * Intent mainIntent = new Intent(this, MainActivity.class);
		 * startActivity(mainIntent);
		 */
		if (!User.username.equals(User.NOT_LOGGED_IN))
			NavUtils.navigateUpFromSameTask(this);
	}

	private void login() {
		EditText usernameField = (EditText) findViewById(R.id.editText2);
		EditText pwField = (EditText) findViewById(R.id.editText3);
		String username = usernameField.getText().toString();
		String pasword = pwField.getText().toString();
		Login login = new Login();
		login.execute(username, pasword);
	}
}