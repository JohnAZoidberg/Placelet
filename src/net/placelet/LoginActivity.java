package net.placelet;

import java.util.HashMap;

import net.placelet.connection.User;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener {

	private SharedPreferences prefs;
	private boolean showRegister = false;

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
		prefs = getSharedPreferences("net.placelet", Context.MODE_PRIVATE);
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
			case R.id.loginButton:
				if (showRegister) {
					register();
				} else {
					login();
				}
				break;
			case R.id.registerButton:
				toggleRegister();
				break;
		}
	}

	private void toggleRegister() {
		showRegister = !showRegister;
		EditText repeatPwField = (EditText) findViewById(R.id.repeatPwField);
		EditText emailField = (EditText) findViewById(R.id.emailField);
		Button loginButton = (Button) findViewById(R.id.loginButton);
		Button toggleButton = (Button) findViewById(R.id.registerButton);
		if (showRegister) {
			emailField.setVisibility(View.VISIBLE);
			repeatPwField.setVisibility(View.VISIBLE);
			toggleButton.setText(getString(R.string.show_login_form));
			loginButton.setText(getString(R.string.register));

		} else {
			emailField.setVisibility(View.GONE);
			repeatPwField.setVisibility(View.GONE);
			loginButton.setText(getString(R.string.login_uc));
			toggleButton.setText(getString(R.string.show_register_form));
		}
	}

	private class Login extends AsyncTask<String, String, Integer> {
		@Override
		protected Integer doInBackground(String... params) {
			User user = new User(prefs);
			int login;
			String username = params[0];
			String pw = params[1];
			if (params[2] != null) {
				String email = params[2];
				login = user.register(username, pw, email);
			} else {
				login = user.login(username, pw);
			}
			return login;
		}

		@Override
		protected void onPostExecute(Integer result) {
			setProgressBarIndeterminateVisibility(false);
			if (showRegister) {
				handleRegisterError(result);
			} else {
				handleLoginError(result);
			}
		}

		@Override
		protected void onPreExecute() {
			setProgressBarIndeterminateVisibility(true);
			super.onPreExecute();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return NavigateActivities.activitySwitchMenu(item, this);
	}

	public void alert(String string) {
		Toast.makeText(this, string, Toast.LENGTH_LONG).show();
	}

	private void switchToMainActivity() {
		HashMap<String, String> extras = new HashMap<String, String>();
		extras.put("stay", "true");
		NavigateActivities.switchActivity(this, MainActivity.class, false, extras);
	}

	private void login() {
		if (Util.notifyIfOffline(this)) {
			EditText usernameField = (EditText) findViewById(R.id.usernameField);
			EditText passwordField = (EditText) findViewById(R.id.passwordField);
			String username = usernameField.getText().toString();
			String pasword = passwordField.getText().toString();
			Login login = new Login();
			login.execute(username, pasword, null);
		}
	}

	private void register() {
		if (Util.notifyIfOffline(this)) {
			EditText usernameField = (EditText) findViewById(R.id.usernameField);
			EditText passwordField = (EditText) findViewById(R.id.passwordField);
			EditText repeatPwField = (EditText) findViewById(R.id.repeatPwField);
			EditText emailField = (EditText) findViewById(R.id.emailField);
			String username = usernameField.getText().toString();
			String password = passwordField.getText().toString();
			String repeatPassword = repeatPwField.getText().toString();
			String email = emailField.getText().toString();
			if (password.equals(repeatPassword)) {
				Login login = new Login();
				login.execute(username, password, email);
			} else {
				handleRegisterError(2);
			}
		}
	}

	private void handleRegisterError(int error) {
		switch (error) {
			case 0:
				alert(getString(R.string.noinput));
				break;
			case 1:
				alert(getString(R.string.register_success));
				break;
			case 2:
				alert(getString(R.string.different_pws));
				break;
			case 3:
				alert(getString(R.string.name_exists));
				break;
			case 4:
				alert(getString(R.string.email_exists));
				break;
			case 5:
				alert(getString(R.string.username_too_short));
				break;
			case 6:
				alert(getString(R.string.username_too_long));
				break;
			case 7:
				alert(getString(R.string.password_too_short));
				break;
			case 8:
				alert(getString(R.string.password_too_long));
				break;
			case 9:
				alert(getString(R.string.invalid_email));
				break;
			default:
				alert(error + "");
		}
	}

	public void handleLoginError(Integer result) {
		switch (result) {
			case 0:
				alert(getString(R.string.account_notextisting));
				break;
			case 1:
				alert(getString(R.string.wrong_pasword));
				break;
			case 2:
				alert(getString(R.string.account_not_verified));
				break;
			case User.SUCCESS:
				switchToMainActivity();
				break;
			default:
				alert(result.toString());
		}
	}
}