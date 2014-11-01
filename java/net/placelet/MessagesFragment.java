package net.placelet;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.melnykov.fab.FloatingActionButton;

import net.placelet.connection.User;
import net.placelet.connection.Webserver;
import net.placelet.data.Message;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MessagesFragment extends Fragment {
	private SharedPreferences prefs;
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
				switchToIOMessage(msg.sender, true);
			}
		});
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.attachToListView(list);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final EditText input = new EditText(mainActivity);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                final AlertDialog dialog = new AlertDialog.Builder(mainActivity)
                        .setTitle(getString(R.string.add_user))
                        .setView(input)
                        .setPositiveButton(getString(R.string.add), null)
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).create();

                dialog.setOnShowListener(new DialogInterface.OnShowListener() {

                    @Override
                    public void onShow(DialogInterface d) {

                        Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        b.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                String username = input.getText().toString();
                                if (username.equals("") || !StringUtils.isAlphanumeric(username)) {
                                    Util.alert(getString(R.string.invalid_username), mainActivity);
                                } else if(username.length() < 4) {
                                    Util.alert(getString(R.string.username_too_short), mainActivity);
                                }else if(username.length() > 15) {
                                    Util.alert(getString(R.string.username_too_long), mainActivity);
                                }else {
                                    switchToIOMessage(username, false);
                                }
                            }
                        });
                    }
                });
                dialog.show();
            }
        });
		adapter = new MessagesAdapter(mainActivity, 0, messageList);

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

	private void switchToIOMessage(String sender, boolean verified) {
		Intent intent = new Intent(mainActivity, IOMessageActivity.class);
		intent.putExtra("recipient", sender);
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
            if(!Webserver.checkResult(result)) {
					swipeLayout.setRefreshing(false);
					return;
			}
            // check if new content
            try {
                String updateString = result.getString("update");
                if(User.admin) Util.alert("Update: " + updateString, mainActivity);
                swipeLayout.setRefreshing(false);
            } catch (JSONException e) {
                Util.saveDate(mainActivity.prefs, "getMessagesLastUpdate", System.currentTimeMillis() / 1000L);
                String jsonString = result.toString();
                Util.saveData(mainActivity.prefs, "messages", jsonString);
                updateListView(result);
            }
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
					if (msg.sent < chat.getLong("sent")) {
						msg.content = chat.getString("message");
                        msg.recipient = chat.getJSONObject("recipient").getString("name");
						msg.sent = chat.getLong("sent");
						msg.seen = chat.getLong("seen");
					}
				} catch (JSONException ignored) {
				}
				msg.sender = key;
				messageList.add(msg);
			} catch (JSONException ignored) {
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
		} catch (JSONException ignored) {
		}
		if (jArray != null)
			updateListView(jArray);
	}
}