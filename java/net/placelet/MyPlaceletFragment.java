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
import android.support.v4.widget.SwipeRefreshLayout;
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
	private SwipeRefreshLayout swipeLayout;

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
				Bracelet bracelet = (Bracelet) list.getItemAtPosition(position);
				if (bracelet.pictures.get(0).stringData == null)
                    NavigateActivities.switchActivity(mainActivity, BraceletActivity.class, false, "brid", bracelet.brid);
			}
		});
		adapter = new MyPlaceletAdapter(mainActivity, 0, braceletList);
		list.setAdapter(adapter);

		swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
		swipeLayout.setColorScheme(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
		swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				loadBracelets(true);
			}
		});

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
					swipeLayout.setRefreshing(false);
					return;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			updateListView(result);
			String jsonString = result.toString();
			Util.saveData(mainActivity.prefs, "myPlacelet", jsonString);
		}
	}

	public void loadBracelets(boolean reload) {
		swipeLayout.setRefreshing(true);
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
			swipeLayout.setRefreshing(false);
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

				Bracelet separatorBacelet = new Bracelet("");
				Picture separatorPicture = new Picture();
				if (key.equals("pics")) {
                    separatorPicture.stringData = mainActivity.getString(R.string.own_pics);
				} else if(key.equals("ownBracelets")){
                    separatorPicture.stringData = mainActivity.getString(R.string.own_bracelets);
				}
                separatorBacelet.pictures.add(separatorPicture);
				braceletList.add(separatorBacelet);

				for (int i = 0; i < jsonArrayLength; i++) {
					try {
                        JSONObject row = jsonArray.getJSONObject(i);
						Picture picture = new Picture();
						picture.brid = row.getString("brid");

						Bracelet bracelet = new Bracelet(picture.brid);
						picture.title = row.getString("title");
						picture.city = row.getString("city");
						picture.country = row.getString("country");
						picture.id = Integer.parseInt(row.getString("id"));
                        picture.loadImage = mainActivity.settingsPrefs.getBoolean("pref_download_pics", true);
                        bracelet.pictures.add(picture);
                        // TODO sort pics
                        braceletList.add(bracelet);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		adapter.notifyDataSetChanged();
		swipeLayout.setRefreshing(false);
	}

}
