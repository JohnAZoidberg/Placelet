package net.placelet;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

public class PictureFragment extends Fragment {
	private BraceletActivity braceletActivity;
	private PictureDetailAdapter adapter;
	private ExpandableListView list;
	public SwipeRefreshLayout swipeLayout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		braceletActivity = (BraceletActivity) getActivity();
		View rootView = inflater.inflate(R.layout.fragment_picture, container, false);
		// initiate listview
		list = (ExpandableListView) rootView.findViewById(R.id.listView1);
		//list.setClickable(true);
		
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
}