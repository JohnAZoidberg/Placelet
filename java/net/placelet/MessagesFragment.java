package net.placelet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.placelet.connection.User;
import net.placelet.data.Message;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

public class MessagesFragment extends Fragment {
	private SharedPreferences prefs;
	private EditText selectUser;
	private MainActivity mainActivity;
	private MessagesAdapter adapter;
	private List<Message> messageList = new ArrayList<Message>();
	private ListView list;
	private SwipeRefreshLayout swipeLayout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mainActivity = (MainActivity) getActivity();
		prefs = mainActivity.prefs;
		View rootView = inflater.inflate(R.layout.fragment_messages, container, false);
		list = (ListView) rootView.findViewById(R.id.messagesList);
		list.setClickable(true);
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Message msg = (Message) list.getItemAtPosition(position);
				switchToIOMessage(msg, true);
			}
		});
		adapter = new MessagesAdapter(mainActivity, 0, messageList);
		selectUser = (EditText) rootView.findViewById(R.id.editText1);
		selectUser.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
				// If the keyevent is a key-down event on the "enter" button
				if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
					Message msg = new Message();
					msg.sender = selectUser.getText().toString();
					switchToIOMessage(msg, false);
					return true;
				}
				return false;
			}
		});
		swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
		swipeLayout.setColorScheme(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
		swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				loadMessages(true);
			}
		});
		
		loadMessages(false);
		return rootView;
	}

	private void switchToIOMessage(Message msg, boolean verified) {
		Intent intent = new Intent(mainActivity, IOMessageActivity.class);
		intent.putExtra("recipient", msg.sender);
		intent.putExtra("recipientVerified", verified);
		startActivity(intent);

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	private class Messages extends AsyncTask<String, String, JSONObject> {
		@Override
		protected JSONObject doInBackground(String... params) {
			User user = new User(prefs);
			JSONObject login = user.getMessages();
			return login;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			try {
				if (result.getString("error").equals("no_internet")) {
					swipeLayout.setRefreshing(false);
					return;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			String jsonString = result.toString();
			SharedPreferences.Editor editor = mainActivity.prefs.edit();
			editor.putString("messages", jsonString);
			editor.commit();
			updateListView(result);
		}
	}

	public void loadMessages(boolean reload) {
		swipeLayout.setRefreshing(true);
		String savedMessages = mainActivity.prefs.getString("messages", "null");
		if (!savedMessages.equals("null") && !reload) {
			loadSavedMessages(savedMessages);
		}
		if (Util.notifyIfOffline(mainActivity)) {
			Messages login = new Messages();
			login.execute(User.username);
		} else {
			swipeLayout.setRefreshing(false);
		}
	}

	private void updateListView(JSONObject input) {
		messageList.clear();
		for (Iterator<?> iter = input.keys(); iter.hasNext();) {
			String key = (String) iter.next();
			try {
				JSONObject chat = input.getJSONObject(key);
				Message msg = new Message();
				try {
					if (msg == null || msg.sent < Long.valueOf(chat.getString("sent"))) {
						msg.content = chat.getString("message");
						msg.sent = chat.getLong("sent");
						msg.seen = chat.getLong("seen");
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				msg.sender = key;
				messageList.add(msg);
			} catch (JSONException e) {
                e.printStackTrace();
            }
		}

		Collections.sort(messageList);
		list.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		swipeLayout.setRefreshing(false);
	}

	private void loadSavedMessages(String result) {
		JSONObject jArray = null;
		try {
			jArray = new JSONObject(result);
		} catch (JSONException e) {
			Log.e("log_tag", "Error parsing data " + e.toString());
		}
		if (jArray != null)
			updateListView(jArray);
	}
}