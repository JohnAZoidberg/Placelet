package net.placelet;

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

import net.placelet.connection.User;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;

import java.util.HashMap;

public class LoginActivity extends Activity implements OnClickListener {

	private SharedPreferences prefs;
	private boolean showRegister = false;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, R.id.action_options, 0, R.string.options).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

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
            String password = passwordField.getText().toString();
            if(username.length() < 4) {
                handleRegisterError(5);
            }else if(username.length() > 15) {
                handleRegisterError(6);
            }else if(password.length() < 6) {
                handleRegisterError(7);
            }else if(password.length() > 30) {
                handleRegisterError(8);
            }else {
                Login login = new Login();
                login.execute(username, password, null);
            }
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
			if (!password.equals(repeatPassword)) {
                handleRegisterError(2);
            }else if(username.length() < 4) {
                handleRegisterError(5);
			}else if(username.length() > 15) {
                handleRegisterError(6);
            }else if(password.length() < 6) {
                handleRegisterError(7);
            }else if(password.length() > 30) {
                handleRegisterError(8);
            }else if(!EmailValidator.getInstance().isValid(email)) {
                handleRegisterError(9);
            }else if(!StringUtils.isAlphanumeric(username)) {
                handleRegisterError(10);
            } else {
                Login login = new Login();
                login.execute(username, password, email);
			}
		}
	}

	private void handleRegisterError(int error) {
		switch (error) {
			case 0:
				Util.alert(getString(R.string.noinput), this);
				break;
			case 1:
				Util.alert(getString(R.string.register_success), this);
				break;
			case 2:
				Util.alert(getString(R.string.different_pws), this);
				break;
			case 3:
				Util.alert(getString(R.string.name_exists), this);
				break;
			case 4:
				Util.alert(getString(R.string.email_exists), this);
				break;
			case 5:
				Util.alert(getString(R.string.username_too_short), this);
				break;
			case 6:
				Util.alert(getString(R.string.username_too_long), this);
				break;
			case 7:
				Util.alert(getString(R.string.password_too_short), this);
				break;
			case 8:
				Util.alert(getString(R.string.password_too_long), this);
				break;
			case 9:
				Util.alert(getString(R.string.invalid_email), this);
				break;
            case 10:
                Util.alert(getString(R.string.invalid_username), this);
                break;
			default:
				Util.alert(error + "", this);
		}
	}

	public void handleLoginError(Integer result) {
		switch (result) {
			case 0:
				Util.alert(getString(R.string.account_notextisting), this);
				break;
			case 1:
				Util.alert(getString(R.string.wrong_pasword), this);
				break;
			case 2:
				Util.alert(getString(R.string.account_not_verified), this);
				break;
			case User.SUCCESS:
				switchToMainActivity();
				break;
			default:
				Util.alert(result.toString(), this);
		}
	}
}