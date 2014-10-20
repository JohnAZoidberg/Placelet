package net.placelet;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
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

import net.placelet.data.Picture;

public class PictureFragment extends Fragment {
	private BraceletActivity braceletActivity;
	private PictureDetailAdapter adapter;
	private ListView list;
	public SwipeRefreshLayout swipeLayout;

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
		
		swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
		swipeLayout.setColorScheme(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
		swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				braceletActivity.loadPictures(true);
			}
		});
		
		updateData();
		return rootView;
	}

	public void updateData() {
		adapter = new PictureDetailAdapter(braceletActivity, 0, braceletActivity.bracelet.pictures);
		list.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		braceletActivity.setProgressBarIndeterminateVisibility(false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	private void showPopup(Picture picture) {
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