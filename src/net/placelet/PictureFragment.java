package net.placelet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class PictureFragment extends Fragment {
	private BraceletActivity braceletActivity;
	private String brid;
	private BraceletAdapter adapter;
	private List<Picture> pictureList = new ArrayList<Picture>();
	private ListView list;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		braceletActivity = (BraceletActivity) getActivity();
		View rootView = inflater.inflate(R.layout.fragment_picture, container, false);
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
		return rootView;
	}

	public void updateListView() {
		adapter = new BraceletAdapter(braceletActivity, 0, braceletActivity.bracelet.pictures);
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

	public void loadSavedPics(String result) {
		JSONObject jArray = null;
		try {
			jArray = new JSONObject(result);
		} catch (JSONException e) {
			Log.e("log_tag", "Error parsing data " + e.toString());
		}
		if (jArray != null)
			updateListView();
	}
}