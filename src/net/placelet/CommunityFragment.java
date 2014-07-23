package net.placelet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import net.placelet.R;
import net.placelet.connection.User;
import net.placelet.data.Picture;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class CommunityFragment extends Fragment {
	private MainActivity mainActivity;
	private CommunityAdapter adapter;
	private List<Picture> pictureList = new ArrayList<Picture>();
	private ListView list;
	private final int PIC_COUNT = 5;
	private int picnr = 8;
	private Button btnLoadMore;
	private SwipeRefreshLayout swipeLayout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mainActivity = (MainActivity) getActivity();
		View rootView = inflater.inflate(R.layout.fragment_community, container, false);
		setUpUploadBar(rootView);
		// Initiate ListView
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
		adapter = new CommunityAdapter(mainActivity, 0, pictureList);
		list.setAdapter(adapter);
		loadPictures(0, false);

		swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
		swipeLayout.setColorScheme(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
		swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				loadPictures(0, true);
			}
		});
		return rootView;
	}

	private void setUpUploadBar(View rootView) {
		ImageView cameraIcon = (ImageView) rootView.findViewById(R.id.cameraIcon);
		cameraIcon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				NavigateActivities.switchActivity(mainActivity, UploadActivity.class, false, "upload", "camera");
			}
		});
		ImageView galleryIcon = (ImageView) rootView.findViewById(R.id.galleryIcon);
		galleryIcon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				NavigateActivities.switchActivity(mainActivity, UploadActivity.class, false, "upload", "gallery");
			}
		});
	}

	public void loadPictures(int start, boolean reload) {
		toggleLoading(true);
		// display saved pics if it shouldn't reload and if there are pics saved
		String savedPics = mainActivity.prefs.getString("communityPics", "null");
		if (!savedPics.equals("null") && !reload) {
			loadSavedPics(savedPics);
		} else {
			start = picnr;
		}
		// load new pics from the internet
		if (Util.notifyIfOffline(mainActivity)) {
			Pictures pics = new Pictures();
			pics.start = start;
			pics.execute();
		} else {
			toggleLoading(false);
		}
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
			// check if connected to the internet
			try {
				if (result.getString("error").equals("no_internet")) {
					toggleLoading(false);
					picnr = PIC_COUNT;
					if (btnLoadMore != null) {
						list.removeFooterView(btnLoadMore);
						btnLoadMore = null;
					}
					return;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			updateListView(result, start);
			String jsonString = result.toString();
			Util.saveData(mainActivity.prefs, "communityPics", jsonString);
		}
	}

	private void loadMore(boolean reload) {
		if (btnLoadMore != null)
			btnLoadMore.setEnabled(false);
		if (!reload)
			picnr += PIC_COUNT;
		loadPictures(picnr, false);
	}

	private void updateListView(JSONObject input, int start) {
		pictureList.clear();
		for (Iterator<?> iter = input.keys(); iter.hasNext();) {
			String key = (String) iter.next();
			try {
				JSONObject pictures = input.getJSONObject(key);
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
					pictureList.add(picture);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		Collections.sort(pictureList);
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
		adapter.notifyDataSetChanged();
		toggleLoading(false);
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
			Log.e("log_tag", "Error parsing data " + e.toString());
		}
		if (jArray != null)
			updateListView(jArray, 0);
	}

	private void toggleLoading(boolean start) {
		if (start) {
			//mainActivity.setProgressBarIndeterminateVisibility(true);
			if(swipeLayout != null) swipeLayout.setRefreshing(true);
			if (btnLoadMore != null) {
				btnLoadMore.setText(getText(R.string.loading));
				btnLoadMore.setEnabled(false);
			}
		} else {
			//mainActivity.setProgressBarIndeterminateVisibility(false);
			if(swipeLayout != null) swipeLayout.setRefreshing(false);
			if (btnLoadMore != null) {
				btnLoadMore.setEnabled(true);
				btnLoadMore.setText(getString(R.string.load_more));
			}
		}
	}
}