package net.placelet;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import net.placelet.connection.User;
import net.placelet.connection.Webserver;
import net.placelet.data.Bracelet;
import net.placelet.data.Picture;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class MyPlaceletFragment extends Fragment {
	private MainActivity mainActivity;
	private MyPlaceletAdapter adapter;
	private ArrayList<Bracelet> bracelets = new ArrayList<Bracelet>();
    private ArrayList<Picture> pictures = new ArrayList<Picture>();
	private ListView list;
	private SwipeRefreshLayout swipeLayout;
    private CommunityAdapter pictureAdapter;
    private BraceletAdapter braceletAdapter;

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainActivity = (MainActivity) getActivity();
        View rootView = inflater.inflate(R.layout.fragment_my_placelet, container, false);
        //Initiate ListView
        list = (ListView) rootView.findViewById(R.id.listView1);
        list.setClickable(true);
        setupAdapters();

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

    public void setupAdapters() {
        pictureAdapter = new CommunityAdapter(mainActivity, 0, pictures);
        braceletAdapter = new BraceletAdapter(mainActivity, 0, bracelets);

        MyPlaceletAdapter adapter = new MyPlaceletAdapter(mainActivity);
        adapter.addSection(getString(R.string.own_pics), pictureAdapter);
        adapter.addSection(getString(R.string.own_bracelets), braceletAdapter);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if(position > pictureAdapter.getCount()) {
                    Bracelet bracelet = (Bracelet) list.getItemAtPosition(position);
                    if(bracelet.brid.equals("register_bracelet")) {
                        Util.displayRegisterDialog(mainActivity, mainActivity.prefs);
                    }else {
                        NavigateActivities.switchActivity(mainActivity, BraceletActivity.class, false, "brid", bracelet.brid);
                    }
                }else {
                    Picture pic = (Picture) list.getItemAtPosition(position);
                    if(pic.brid.equals("upload_picture")) {
                        NavigateActivities.switchActivity(mainActivity, UploadActivity.class, false);
                    }else {
                        NavigateActivities.switchActivity(mainActivity, BraceletActivity.class, false, "brid", pic.brid);
                    }
                }
            }
        });
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
                Util.saveDate(mainActivity.prefs, "getOwnBraceletsLastUpdate", System.currentTimeMillis() / 1000L);
                String jsonString = result.toString();
                Util.saveData(mainActivity.prefs, "myPlacelet", jsonString);
                updateListView(result);
            }
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
		} catch (JSONException ignored) {
		}
		if (jArray != null)
			updateListView(jArray);
	}

	private void updateListView(JSONObject input) {
        pictures.clear();
        bracelets.clear();
        for (Iterator<?> iter = input.keys(); iter.hasNext();) {
			String key = (String) iter.next();
			try {
				JSONArray jsonArray = input.getJSONArray(key);
				int jsonArrayLength = jsonArray.length();

                if(key.equals("pics") || key.equals("ownBracelets")) {
                    for (int i = 0; i < jsonArrayLength; i++) {
                        try {
                            JSONObject row = jsonArray.getJSONObject(i);
                            if (key.equals("pics")) {
                                Picture picture = new Picture();
                                picture.brid = row.getString("brid");
                                picture.title = row.getString("title");
                                picture.city = row.getString("city");
                                picture.country = row.getString("country");
                                picture.id = Integer.parseInt(row.getString("id"));
                                pictures.add(picture);
                            } else if (key.equals("ownBracelets")) {
                                Picture picture = new Picture();
                                picture.brid = row.getString("brid");

                                Bracelet bracelet = new Bracelet(picture.brid);
                                bracelet.setDistance(row.getInt("distance"));
                                bracelet.name = row.getString("name");
                                picture.title = row.getString("title");
                                picture.city = row.getString("city");
                                picture.country = row.getString("country");
                                picture.id = Integer.parseInt(row.getString("id"));
                                bracelet.pictures.add(picture);
                                bracelets.add(bracelet);
                            }
                        } catch (JSONException e) {
                        }
                    }
				}
			} catch (JSONException e) {
			}
		}
        Collections.sort(pictures);
        // If no pictures or bracelets add pseudo picture or bracelet to post/register new one
        if(bracelets.size() == 0) {
            Bracelet bracelet = new Bracelet("register_bracelet");
            bracelet.name = mainActivity.getString(R.string.new_bracelet);
            Picture picture = new Picture();
            picture.city = mainActivity.getString(R.string.city);
            picture.country = mainActivity.getString(R.string.country);
            bracelet.pictures.add(picture);
            bracelets.add(bracelet);
        }
        if(pictures.size() == 0) {
            Picture picture = new Picture();
            picture.brid = "upload_picture";
            picture.title = mainActivity.getString(R.string.new_picture);
            picture.city = mainActivity.getString(R.string.city);
            picture.country = mainActivity.getString(R.string.country);
            pictures.add(picture);
        }
        setupAdapters();
        braceletAdapter.notifyDataSetChanged();
        pictureAdapter.notifyDataSetChanged();
        swipeLayout.setRefreshing(false);
	}

}
