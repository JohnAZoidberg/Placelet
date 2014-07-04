package net.placelet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class MessagesFragment extends Fragment implements OnClickListener {
    private SharedPreferences prefs;
    private TextView textView;
    private EditText selectUser;
    private MainActivity mainActivity;
    private MessagesAdapter adapter;
    private List<Message> messageList = new ArrayList<Message>();
    private ListView list;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	// messageList = test();
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
	textView = (TextView) rootView.findViewById(R.id.textView1);
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
	// Toast.makeText(mainActivity, "onCreate", Toast.LENGTH_LONG).show();
	loadMessages();
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
	    mainActivity.setProgressBarIndeterminateVisibility(false);
	    updateListView(result);
	    textView.setVisibility(View.GONE);
	}
    }

    private void loadMessages() {
	mainActivity.setProgressBarIndeterminateVisibility(true);
	textView.setText(getString(R.string.messages_loading));
	Messages login = new Messages();
	login.execute(User.username);
    }

    public void setText(String text) {
	TextView textView = (TextView) getView().findViewById(R.id.textView1);
	textView.setText(text);
    }

    public void sendMessage() {
	mainActivity.setProgressBarIndeterminateVisibility(true);
	EditText messageField = (EditText) getView().findViewById(R.id.editText1);
	EditText recipientField = (EditText) getView().findViewById(R.id.editText2);
	String recipient = recipientField.getText().toString();
	String message = messageField.getText().toString();
	Messages login = new Messages();
	login.execute(recipient, message);
    }

    @Override
    public void onClick(View v) {
	switch (v.getId()) {
	case R.id.button1:
	    sendMessage();
	    break;
	}
    }

    private void updateListView(JSONObject input) {
	messageList.clear();
	for (Iterator<String> iter = input.keys(); iter.hasNext();) {
	    String key = iter.next();
	    try {
		JSONObject chat = input.getJSONObject(key);
		Message msg = new Message();
		try {
		    if (msg == null || msg.sent < Long.valueOf(chat.getString("sent"))) {
			msg.message = chat.getString("message");
			msg.sent = chat.getLong("sent");
		    }
		} catch (JSONException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
		msg.sender = key;
		messageList.add(msg);
	    } catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}

	Collections.sort(messageList);
	list.setAdapter(adapter);
	adapter.notifyDataSetChanged();
    }
}