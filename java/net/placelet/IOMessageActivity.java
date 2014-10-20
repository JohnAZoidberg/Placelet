package net.placelet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.placelet.connection.User;
import net.placelet.data.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
	private Button b;
    private RelativeLayout footer;
    private boolean reloadHidden = false;

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Util.inflateActionBar(this, menu, reloadHidden);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!User.getStatus()) {
			HashMap<String, String> extras = new HashMap<String, String>();
			extras.put("fragment", "1");
			NavigateActivities.switchActivity(this, MainActivity.class, false, extras);
		}
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_io_message);
		prefs = getSharedPreferences("net.placelet", Context.MODE_PRIVATE);
		settingsPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		// set up Action Bar
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setTitle(User.username);
		b = (Button) findViewById(R.id.button1);
		b.setOnClickListener(this);
		list = (ListView) findViewById(R.id.messagesList);
		messageField = (EditText) findViewById(R.id.editText1);
		recipientDisplay = (TextView) findViewById(R.id.username);
		getIntents();
		adapter = new IOMessageAdapter(this, 0, messageList);
        list.setAdapter(adapter);
		loadMessages(true);
	}

	private void getIntents() {
		Intent intent = getIntent();
		if (intent.hasExtra("recipient")) {
			recipient = intent.getStringExtra("recipient");
			recipientDisplay.setText(recipient);
		}
		if (intent.hasExtra("recipientVerified")) {
			recipientVerified = intent.getBooleanExtra("recipientVerified", false);
		}
	}

	private class Messages extends AsyncTask<String, String, JSONObject> {
		@Override
		protected JSONObject doInBackground(String... params) {
			JSONObject login = null;
			User user = new User(prefs);
			if (params.length == 1) {
				String content = params[0];
				try {
					login = user.sendMessage(recipient, URLEncoder.encode(content, "utf-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}else {
                login = user.getIOMessages(recipient);
            }
			return login;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			// check if connected to the internet
			try {
				if (result.getString("error").equals("no_internet")) {
					setProgressBarIndeterminateVisibility(false);
					if (!recipientVerified) {
						displayErrorAtMessageFragment(getString(R.string.user_notextisting));
					}
					return;
				}
			} catch (JSONException ignored) {
			}
			Boolean exists = false;
			try {
				if (result != null)
					exists = result.getBoolean("exists");
			} catch (JSONException ignored) {
			}
			if (recipientVerified || exists) {
                // check if new content
                try {
                    String updateString = result.getString("update");
                    if(User.admin) Util.alert("Update: " + updateString, IOMessageActivity.this);
                    toggleLoading(false, true);
                } catch (JSONException e) {
                    Util.saveDate(prefs, "getIOMessagesLastUpdate-" + recipient, System.currentTimeMillis() / 1000L);
                    String jsonString = result.toString();
                    Util.saveData(prefs, "IOmessages-" + recipient, jsonString);
                    updateListView(result);
                }
			} else {
				displayErrorAtMessageFragment(getString(R.string.user_notextisting));
			}
		}
	}

	public void sendMessage() {
		if (Util.notifyIfOffline(this)) {
			toggleLoading(true, true);
			String message = messageField.getText().toString();
			messageField.setText("");
			Messages send = new Messages();
			send.execute(message);
		}
	}

	private void loadMessages(boolean reload) {
        toggleLoading(true, false);
		String savedMessages = prefs.getString("IOmessages-" + recipient, "null");
		if (!savedMessages.equals("null") && !reload) {
			loadSavedMessages(savedMessages);
		}
		if (Util.notifyIfOffline(this)) {
			Messages login = new Messages();
			login.execute();
		} else {
            toggleLoading(false, false);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				HashMap<String, String> extras = new HashMap<String, String>();
				extras.put("fragment", "1");
				NavigateActivities.switchActivity(this, MainActivity.class, false, extras);
				return true;
			case R.id.action_reload:
				loadMessages(true);
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
		for (Iterator<?> iter = input.keys(); iter.hasNext();) {
			String key = (String) iter.next();
			try {
				JSONObject chat = input.getJSONObject(key);
				if (recipient.equals(chat.getJSONObject("recipient").getString("name"))) {
					for (Iterator<?> iter2 = chat.keys(); iter2.hasNext();) {
						String key2 = (String) iter2.next();
						try {
							JSONObject messages = chat.getJSONObject(key2);
							Message msg = new Message();
							msg.content = messages.getString("message");
							msg.sent = messages.getLong("sent");
                            msg.seen = messages.getLong("seen");
							msg.senderID = Integer.parseInt(messages.getJSONObject("sender").getString("id"));
							msg.sender = messages.getJSONObject("sender").getString("name");
							messageList.add(msg);
						} catch (JSONException ignored) {
						}
					}
				}
			} catch (JSONException ignored) {
			}
		}

		Collections.sort(messageList);
		Collections.reverse(messageList);
        messageList = mergeMessages(messageList);
		adapter.notifyDataSetChanged();
        Message lastMessage = new Message();
        if(messageList.size() > 0) lastMessage = messageList.get(messageList.size() - 1);
        if(footer != null)
            list.removeFooterView(footer);
        if(lastMessage.seen > 0 && lastMessage.sender.equals(User.username)) {
            footer = (RelativeLayout) getLayoutInflater().inflate(R.layout.seen_footer, null);
            TextView footer_seen = (TextView) footer.findViewById(R.id.textView);
            footer_seen.append(" " + Util.timestampToTime(lastMessage.seen));
            list.addFooterView(footer);
        }
		toggleLoading(false, true);
	}

    private void displayErrorAtMessageFragment(String err) {
		Toast.makeText(this, err, Toast.LENGTH_LONG).show();
		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra("fragment", "1");
		startActivity(intent);
	}

	private void alert(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}

	private void loadSavedMessages(String result) {
		JSONObject jArray = null;
		try {
			jArray = new JSONObject(result);
		} catch (JSONException ignored) {
		}
		if (jArray != null)
			updateListView(jArray);
	}

	private void toggleLoading(boolean loading, boolean disableButton) {
		if (loading) {
			if(disableButton) b.setEnabled(false);
			setProgressBarIndeterminateVisibility(true);
            reloadHidden = true;

		} else {
            if(disableButton) b.setEnabled(true);
            reloadHidden = false;
			setProgressBarIndeterminateVisibility(false);
		}
        invalidateOptionsMenu();
	}

    private List<Message> mergeMessages(List<Message> messages) {
        List<Message> messagesClone = new ArrayList<Message>();
        for(Message message : messages) messagesClone.add(message);
        messages.clear();

        for(Message message : messagesClone) {
            if(messages.size() > 0) {
                Message lastMerged = messages.get(messages.size() - 1);
                if(message.sender.equals(messages.get(messages.size() - 1).sender)) {
                    messages.get(messages.size() - 1).addContent("\n\n" + message.content);
                }else {
                    messages.add(message);
                }
            }else {
                messages.add(message);
            }
        }
        return messages;
    }
}