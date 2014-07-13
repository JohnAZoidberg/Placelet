package net.placelet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import net.placelet.R;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.AdapterView.OnItemClickListener;

public class BraceletFragment extends Fragment {
	private BraceletActivity braceletActivity;
	private String brid;
	private BraceletAdapter adapter;
	private List<Picture> pictureList = new ArrayList<Picture>();
	private ListView list;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		braceletActivity = (BraceletActivity) getActivity();
		View rootView = inflater.inflate(R.layout.fragment_bracelet, container, false);
		// initiate listview
		list = (ListView) rootView.findViewById(R.id.listView1);
		list.setClickable(true);
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Picture pic = (Picture) list.getItemAtPosition(position);
				showPopup(pic);
			}
		});
		adapter = new BraceletAdapter(braceletActivity, 0, pictureList);
		loadPictures(false);
		return rootView;
	}

	private class Pictures extends AsyncTask<String, String, JSONObject> {
		@Override
		protected JSONObject doInBackground(String... params) {
			User user = new User(braceletActivity.prefs);
			JSONObject content = user.getBraceletPictures(brid);
			return content;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			// check if connected to the internet
			try {
				if (result.getString("error").equals("no_internet")) {
					braceletActivity.setProgressBarIndeterminateVisibility(false);
					return;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			String jsonString = result.toString();
			Util.saveData(braceletActivity.prefs, "braceletPics-" + brid, jsonString);
			updateListView(result);
		}
	}

	public void loadPictures(boolean reload) {
		if (braceletActivity.bracelet.brid != null)
			brid = braceletActivity.bracelet.brid;
		else
			brid = "588888";
		braceletActivity.setProgressBarIndeterminateVisibility(true);
		// display saved pics if it shouldn't reload and if there are pics saved
		String savedPics = braceletActivity.prefs.getString("braceletPics-" + brid, "null");
		if (!savedPics.equals("null") && !reload) {
			loadSavedPics(savedPics);
		}
		// load new pics from the internet
		if (Util.notifyIfOffline(braceletActivity)) {
			Pictures pics = new Pictures();
			pics.execute();
		}else {
			braceletActivity.setProgressBarIndeterminateVisibility(false);
		}
	}

	private void updateListView(JSONObject input) {
		pictureList.clear();
		for (Iterator<?> iter =  input.keys(); iter.hasNext();) {
			String key = (String) iter.next();
			try {
				JSONObject pictures = input.getJSONObject(key);
				Picture picture = new Picture();
				// picture.brid = pictures.getString("brid");
				picture.title = pictures.getString("title");
				picture.description = pictures.getString("description");
				picture.city = pictures.getString("city");
				picture.country = pictures.getString("country");
				picture.uploader = pictures.getString("user");
				picture.date = pictures.getLong("date");
				picture.id = pictures.getInt("id");
				picture.fileext = pictures.getString("fileext");
				picture.loadImage = braceletActivity.settingsPrefs.getBoolean("pref_download_pics", true);
				pictureList.add(picture);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		Collections.sort(pictureList);
		list.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		braceletActivity.setProgressBarIndeterminateVisibility(false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	private void showPopup(Picture picture) {
		if (picture.loadImage) {
			braceletActivity.setProgressBarIndeterminateVisibility(true);
			LayoutInflater inflater = (LayoutInflater) braceletActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View popupView = inflater.inflate(R.layout.popup_lightbox, null, false);
			final PopupWindow pw = new PopupWindow(popupView, Util.width, (int) (Util.height), true);
			ImageView imgView = (ImageView) popupView.findViewById(R.id.imageView1);
			// Display high res picture if preferred
			String picUrl;
			if (braceletActivity.settingsPrefs.getBoolean("pref_highdef_pics", false)) {
				picUrl = "http://placelet.de/pictures/bracelets/pic-" + picture.id + "." + picture.fileext;
			} else {
				picUrl = "http://placelet.de/pictures/bracelets/thumb-" + picture.id + ".jpg";
			}
			Picasso.with(braceletActivity).load(picUrl).into(imgView, new Callback() {
				@Override
				public void onError() {
					braceletActivity.setProgressBarIndeterminateVisibility(false);
					pw.dismiss();
				}

				@Override
				public void onSuccess() {
					braceletActivity.setProgressBarIndeterminateVisibility(false);
				}
			});
			imgView.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick(View v) {
					pw.dismiss();
				}
			});
			pw.showAtLocation(braceletActivity.findViewById(R.id.listView1), Gravity.CENTER, 0, 0);
		}
	}

	private void loadSavedPics(String result) {
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