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
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class BraceletFragment extends Fragment {

	private MainActivity mainActivity;
	private SharedPreferences prefs;
	private TextView textView;
	private String brid;
	private BraceletAdapter adapter;
	private List<Picture> pictureList = new ArrayList<Picture>();
	private ListView list;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mainActivity = (MainActivity) getActivity();
		prefs = mainActivity.prefs;
		View rootView = inflater.inflate(R.layout.fragment_bracelet, container, false);
		// Toast.makeText(mainActivity, "Whatsup?", Toast.LENGTH_LONG).show();
		textView = (TextView) rootView.findViewById(R.id.textView1);
		list = (ListView) rootView.findViewById(R.id.listView1);
		list.setClickable(true);
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Picture pic = (Picture) list.getItemAtPosition(position);
				showPopup(pic);
			}
		});
		adapter = new BraceletAdapter(mainActivity, 0, pictureList);
		loadPictures();
		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	private class Pictures extends AsyncTask<String, String, JSONObject> {
		@Override
		protected JSONObject doInBackground(String... params) {
			User user = new User(prefs);
			JSONObject content = user.getBraceletPictures(brid);
			return content;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			mainActivity.setProgressBarIndeterminateVisibility(false);
			updateListView(result);
		}
	}

	public void loadPictures() {
		if (mainActivity.brid != null)
			brid = mainActivity.brid;
		else
			brid = "588888";
		mainActivity.setProgressBarIndeterminateVisibility(true);
		Pictures pics = new Pictures();
		pics.execute();
		list.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}

	private void updateListView(JSONObject input) {
		pictureList.clear();
		for (Iterator<String> iter = input.keys(); iter.hasNext();) {
			String key = iter.next();
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
				picture.loadImage = mainActivity.settingsPrefs.getBoolean("pref_download_pics", false);
				pictureList.add(picture);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		Collections.sort(pictureList);
		list.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	private void showPopup(Picture picture) {
		if (picture.loadImage) {
			LayoutInflater inflater = (LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View popupView = inflater.inflate(R.layout.popup_lightbox, null, false);
			Display display = mainActivity.getWindowManager().getDefaultDisplay();
			Point size = new Point();
			display.getSize(size);
			int width = size.x;
			int height = size.y;
			final PopupWindow pw = new PopupWindow(popupView, width, (int) (height), true);
			ImageView imgView = (ImageView) popupView.findViewById(R.id.imageView1);
			String picUrl;
			if (mainActivity.settingsPrefs.getBoolean("pref_highdef_pics", false)) {
				picUrl = "http://placelet.de/pictures/bracelets/pic-" + picture.id + "." + picture.fileext;
			} else {
				picUrl = "http://placelet.de/pictures/bracelets/thumb-" + picture.id + ".jpg";
			}
			mainActivity.setProgressBarIndeterminateVisibility(true);
			Picasso.with(mainActivity).load(picUrl).into(imgView, new Callback() {
				@Override
				public void onError() {
					pw.dismiss();
				}

				@Override
				public void onSuccess() {
					mainActivity.setProgressBarIndeterminateVisibility(false);
				}
			});
			imgView.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick(View v) {
					pw.dismiss();
				}
			});
			pw.showAtLocation(mainActivity.findViewById(R.id.listView1), Gravity.CENTER, 0, 0);
		}
	}
}