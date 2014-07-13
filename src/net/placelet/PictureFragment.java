package net.placelet;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PictureFragment extends Fragment {
	private BraceletActivity braceletActivity;
	private Bracelet bracelet;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		braceletActivity = (BraceletActivity) getActivity();
		bracelet = braceletActivity.bracelet;
		View rootView = inflater.inflate(R.layout.fragment_picture, container, false);
		TextView bridView = (TextView) rootView.findViewById(R.id.braceletID);
		bridView.setText(bracelet.brid);
		return rootView;
	}
}