package net.placelet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class IOMessageActivity extends Activity implements OnClickListener {

	private SharedPreferences prefs;
	private SharedPreferences settingsPrefs;
	private IOMessageAdapter adapter;
	private List<Message> messageList = new ArrayList<Message>();
	private ListView list;
	private String recipient = "nope";
	private Boolean recipientVerified = false;
	private TextView recipientDisplay;
	private EditText messageField;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!User.username.equals(User.NOT_LOGGED_IN)) {
			// Inflate the menu items for use in the action bar
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.action_bar, menu);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_io_message);
		prefs = this.getSharedPreferences("net.placelet", Context.MODE_PRIVATE);
		settingsPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setTitle(User.username);
		Button b = (Button) findViewById(R.id.button1);
		b.setOnClickListener(this);
		list = (ListView) findViewById(R.id.messagesList);
		messageField = (EditText) findViewById(R.id.editText1);
		adapter = new IOMessageAdapter(this, 0, messageList);
		recipientDisplay = (TextView) findViewById(R.id.username);
		getIntents();
		loadMessages();
	}

	private void getIntents() {
		Intent intent = getIntent();
		if (intent.hasExtra("recipient")) {
			setRecipient(intent.getStringExtra("recipient"));
			recipientDisplay.setText(recipient);
		}
		if (intent.hasExtra("recipientVerified")) {
			recipientVerified = intent.getBooleanExtra("recipientVerified", false);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	private class Messages extends AsyncTask<String, String, JSONObject> {
		@Override
		protected JSONObject doInBackground(String... params) {
			JSONObject login;
			User user = new User(prefs);
			String recip = getRecipient();
			if (params.length == 1) {
				String content = params[0];
				user.sendMessage(recipient, content);
			}
			login = user.getIOMessages(recip);
			return login;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			try {
				if (result.getString("error").equals("no_internet")) {
					setProgressBarIndeterminateVisibility(false);
					if(!recipientVerified) {
						displayErrorAtMessageFragment(getString(R.string.user_notextisting));
					}
					return;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			Boolean exists = false;
			try {
				if (result != null)
					exists = result.getBoolean("exists");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (recipientVerified || exists) {
				String jsonString = result.toString();
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString("messages-" + recipient, jsonString);
				editor.commit();
				updateListView(result);
			} else {
				displayErrorAtMessageFragment(getString(R.string.user_notextisting));
			}
		}
	}

	public void sendMessage() {
		setProgressBarIndeterminateVisibility(true);
		String message = messageField.getText().toString();
		messageField.setText("");
		Messages login = new Messages();
		login.execute(message);
	}

	private void loadMessages() {
		setProgressBarIndeterminateVisibility(true);
		String savedMessages = prefs.getString("messages-" + recipient, "null");
		if (!savedMessages.equals("null")) {
			loadSavedMessages(savedMessages);
		}
		Messages login = new Messages();
		login.execute();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				HashMap<String, String> extras = new HashMap<String, String>();
				extras.put("fragment", "2");
				NavigateActivities.switchActivity(this, MainActivity.class, false, extras);
				return true;
			case R.id.action_reload:
				loadMessages();
			default:
				return NavigateActivities.activitySwitchMenu(item, this);
		}
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
			case R.id.button1:
				if (messageField.getText().toString().trim().length() > 0) {
					sendMessage();
				} else
					alert(getString(R.string.enter_message));
				break;
		}
	}

	private void updateListView(JSONObject input) {
		messageList.clear();
		for (Iterator<String> iter = input.keys(); iter.hasNext();) {
			String key = iter.next();
			try {
				JSONObject chat = input.getJSONObject(key);
				if (recipient.equals(chat.getJSONObject("recipient").getString("name"))) {
					for (Iterator<String> iter2 = chat.keys(); iter2.hasNext();) {
						String key2 = iter2.next();
						try {
							JSONObject messages = chat.getJSONObject(key2);
							Message msg = new Message();
							msg.message = messages.getString("message");
							msg.sent = messages.getLong("sent");
							msg.loadImage = settingsPrefs.getBoolean("pref_download_pics", true);
							msg.senderID = Integer.parseInt(messages.getJSONObject("sender").getString("id"));
							msg.sender = messages.getJSONObject("sender").getString("name");
							messageList.add(msg);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		Collections.sort(messageList);
		Collections.reverse(messageList);
		list.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		setProgressBarIndeterminateVisibility(false);
	}

	private void displayErrorAtMessageFragment(String err) {
		Toast.makeText(this, err, Toast.LENGTH_LONG).show();
		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra("fragment", "2");
		startActivity(intent);
	}

	private void alert(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}

	private String getRecipient() {
		return recipient;
	}

	private void setRecipient(String recip) {
		this.recipient = recip;
	}

	private void loadSavedMessages(String result) {
		JSONObject jArray = null;
		try {
			jArray = new JSONObject(result);
		} catch (JSONException e) {
			// TODO hier was hinmachen
			Log.e("log_tag", "Error parsing data " + e.toString());
		}
		if (jArray != null)
			updateListView(jArray);
	}
}