package net.placelet;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.placelet.connection.User;
import net.placelet.data.Bracelet;
import net.placelet.data.Picture;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class MyPlaceletFragment extends Fragment {
	private MainActivity mainActivity;
	private MyPlaceletAdapter adapter;
	private ArrayList<Bracelet> braceletList = new ArrayList<Bracelet>();
	private ListView list;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mainActivity = (MainActivity) getActivity();
		View rootView = inflater.inflate(R.layout.fragment_my_placelet, container, false);
		//Initiate ListView
		list = (ListView) rootView.findViewById(R.id.listView1);
		list.setClickable(true);
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Picture pic = (Picture) list.getItemAtPosition(position);
				//mainActivity.switchToBraceletFragment(pic);
				NavigateActivities.switchActivity(mainActivity, BraceletActivity.class, false, "brid", pic.brid);
			}
		});
		adapter = new MyPlaceletAdapter(mainActivity, 0, braceletList);
		list.setAdapter(adapter);

		loadBracelets(false);
		return rootView;
	}

	private class Bracelets extends AsyncTask<String, String, JSONObject> {

		@Override
		protected JSONObject doInBackground(String... params) {
			JSONObject content;
			User user = new User(mainActivity.prefs);
			content = user.getOwnBracelets();
			return content;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			// check if connected to the internet
			try {
				if (result.getString("error").equals("no_internet")) {
					mainActivity.setProgressBarIndeterminateVisibility(false);
					return;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			updateListView(result);
			String jsonString = result.toString();
			Util.saveData(mainActivity.prefs, "communityPics", jsonString);
		}
	}

	public void loadBracelets(boolean reload) {
		mainActivity.setProgressBarIndeterminateVisibility(true);
		// display saved pics if it shouldn't reload and if there are pics saved
		String savedBracelets = mainActivity.prefs.getString("myPlacelet", "null");
		if (!savedBracelets.equals("null") && !reload) {
			loadSavedBracelets(savedBracelets);
		}
		// load new pics from the internet
		if (Util.notifyIfOffline(mainActivity)) {
			Bracelets bracelets = new Bracelets();
			bracelets.execute();
		} else {
			mainActivity.setProgressBarIndeterminateVisibility(false);
		}
	}

	private void loadSavedBracelets(String result) {
		JSONObject jArray = null;
		try {
			jArray = new JSONObject(result);
		} catch (JSONException e) {
			Log.e("log_tag", "Error parsing data " + e.toString());
		}
		if (jArray != null)
			updateListView(jArray);
	}

	private void updateListView(JSONObject input) {
		braceletList.clear();
		for (Iterator<?> iter = input.keys(); iter.hasNext();) {
			String key = (String) iter.next();
			try {
				JSONArray jsonArray = input.getJSONArray(key);
				int jsonArrayLength = jsonArray.length();

				Bracelet bracelet = new Bracelet("");
				Picture picture = new Picture();
				if (key.equals("pics")) {
					picture.stringData = "Eigene Bilder";
				} else {
					picture.stringData = "Eigene Armbänder";
				}
				bracelet.pictures.add(picture);
				braceletList.add(bracelet);

				for (int i = 0; i < jsonArrayLength; i++) {
					JSONObject row = jsonArray.getJSONObject(i);
					bracelet = new Bracelet("");
					picture = new Picture();
					try {
						picture.title = row.getString("title");
						picture.city = row.getString("city");
						picture.country = row.getString("country");
						picture.brid = row.getString("brid");
						picture.id = Integer.parseInt(row.getString("id"));
					} catch (JSONException e) {
						e.printStackTrace();
					}
					picture.loadImage = mainActivity.settingsPrefs.getBoolean("pref_download_pics", true);
					bracelet.pictures.add(picture);
					// TODO sort pics
					braceletList.add(bracelet);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		adapter.notifyDataSetChanged();
		mainActivity.setProgressBarIndeterminateVisibility(false);
	}

}
