package net.placelet;

import java.util.ArrayList;
import java.util.Collections;
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
	    // Toast.makeText(this, recipient, Toast.LENGTH_LONG).show();
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
	    setProgressBarIndeterminateVisibility(false);
	    Boolean exists = false;
	    try {
		if(result != null) exists = result.getBoolean("exists");
	    } catch (JSONException e) {
		e.printStackTrace();
	    }
	    if (recipientVerified || exists) {
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
	Messages login = new Messages();
	login.execute();
    }

    private void switchToProfile() {
	if (User.username != User.NOT_LOGGED_IN) {
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
	    Intent mainIntent = new Intent(this, MainActivity.class);
	    mainIntent.putExtra("fragment", 2);
	    startActivity(mainIntent);
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

    private void switchToUpload() {
	if (User.username != User.NOT_LOGGED_IN) {
	    Intent uploadIntent = new Intent(this, UploadActivity.class);
	    startActivity(uploadIntent);
	}
    }

    @Override
    public void onClick(View arg0) {
	switch (arg0.getId()) {
	case R.id.button1:
	    if(messageField.getText().toString().trim().length() > 0) {
		sendMessage();
	    }else alert(getString(R.string.enter_message));
	    break;
	}
    }

    private void updateListView(JSONObject input) {
	messageList.clear();
	for (Iterator<String> iter = input.keys(); iter.hasNext();) {
	    String key = iter.next();
	    try {
		JSONObject chat = input.getJSONObject(key);
		if (recipient.equals(chat.getJSONObject("recipient").getString(
			"name"))) {
		    for (Iterator<String> iter2 = chat.keys(); iter2.hasNext();) {
			String key2 = iter2.next();
			try {
			    JSONObject messages = chat.getJSONObject(key2);
			    Message msg = new Message();
			    msg.message = messages.getString("message");
			    msg.sent = messages.getLong("sent");
			    msg.loadImage = settingsPrefs.getBoolean("pref_download_pics", false);
			    // msg.sender =
			    // messages.getJSONObject("sender").getString("name");
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
	list.setAdapter(adapter);
	adapter.notifyDataSetChanged();
    }

    private void displayErrorAtMessageFragment(String err) {
	Toast.makeText(this, err, Toast.LENGTH_LONG).show();
	Intent intent = new Intent(this, MainActivity.class);
	intent.putExtra("fragment", 2);
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

    private void switchToOptions() {
	if (User.username != User.NOT_LOGGED_IN) {
	    Intent profileIntent = new Intent(this, OptionsActivity.class);
	    startActivity(profileIntent);
	}
    }
}