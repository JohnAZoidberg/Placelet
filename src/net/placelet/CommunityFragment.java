package net.placelet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import net.placelet.R;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class CommunityFragment extends Fragment {
	private MainActivity mainActivity;
	private CommunityAdapter adapter;
	private List<Picture> pictureList = new ArrayList<Picture>();
	private ListView list;
	private final int PIC_COUNT = 5;
	private int picnr = PIC_COUNT;
	private Button btnLoadMore;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mainActivity = (MainActivity) getActivity();
		View rootView = inflater.inflate(R.layout.fragment_community, container, false);
		list = (ListView) rootView.findViewById(R.id.listView1);
		list.setClickable(true);
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Picture pic = (Picture) list.getItemAtPosition(position);
				mainActivity.switchToBraceletFragment(pic);
			}
		});
		adapter = new CommunityAdapter(mainActivity, 0, pictureList);
		list.setAdapter(adapter);
		loadPictures(0, false);

		return rootView;
	}

	public void loadPictures(int start, boolean reload) {
		mainActivity.setProgressBarIndeterminateVisibility(true);
		if (btnLoadMore != null)
			btnLoadMore.setText(getText(R.string.loading));
		String savedPics = mainActivity.prefs.getString("communityPics", "null");
		if (!savedPics.equals("null") && !reload) {
			loadSavedPics(savedPics);
		} else {
			start = picnr;
		}
		Pictures pics = new Pictures();
		pics.start = start;
		pics.execute();
	}

	private class Pictures extends AsyncTask<String, String, JSONObject> {
		public int start = 0;

		@Override
		protected JSONObject doInBackground(String... params) {
			JSONObject content;
			User user = new User(mainActivity.prefs);
			content = user.getCommunityPictures(picnr);
			return content;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			try {
				if (result.getString("error").equals("no_internet")) {
					mainActivity.setProgressBarIndeterminateVisibility(false);
					if(btnLoadMore != null) {
						btnLoadMore.setEnabled(true);
						btnLoadMore.setText(getString(R.string.load_more));
					}
					return;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			updateListView(result, start);
			String jsonString = result.toString();
			Util.saveData(mainActivity.prefs, "communityPics", jsonString);
			if (btnLoadMore == null) {
				btnLoadMore = new Button(mainActivity);
				btnLoadMore.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						loadMore(false);
					}
				});
				list.addFooterView(btnLoadMore);
			}
			if (btnLoadMore != null) {
				btnLoadMore.setEnabled(true);
				btnLoadMore.setText(getString(R.string.load_more));
			}
		}
	}

	private void loadMore(boolean reload) {
		if (btnLoadMore != null)
			btnLoadMore.setEnabled(false);
		if (!reload)
			picnr += PIC_COUNT;
		loadPictures(picnr, true);
	}

	private void updateListView(JSONObject input, int start) {
		pictureList.clear();
		for (Iterator<String> iter = input.keys(); iter.hasNext();) {
			String key = iter.next();
			try {
				JSONObject pictures = input.getJSONObject(key);
				/*
				 * for (Iterator<String> iter2 = pictures.keys(); iter2.hasNext();) {
				 * String key2 = iter2.next();
				 */
				try {
					Picture picture = new Picture();
					picture.brid = pictures.getString("brid");
					picture.title = pictures.getString("title");
					picture.description = pictures.getString("description");
					picture.city = pictures.getString("city");
					picture.country = pictures.getString("country");
					picture.uploader = pictures.getString("user");
					picture.date = Long.parseLong(pictures.getString("date"));
					picture.id = Integer.parseInt(pictures.getString("id"));
					picture.loadImage = mainActivity.settingsPrefs.getBoolean("pref_download_pics", true);
					boolean contains = pictureList.contains(picture);
					if (!contains)
						pictureList.add(picture);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		Collections.sort(pictureList);
		adapter.notifyDataSetChanged();
		mainActivity.setProgressBarIndeterminateVisibility(false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	private void loadSavedPics(String result) {
		JSONObject jArray = null;
		try {
			jArray = new JSONObject(result);
		} catch (JSONException e) {
			// TODO hier was hinmachen
			Log.e("log_tag", "Error parsing data " + e.toString());
		}
		if (jArray != null)
			updateListView(jArray, 0);
	}
}