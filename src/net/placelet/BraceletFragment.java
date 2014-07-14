package net.placelet;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class BraceletFragment extends Fragment {
	private BraceletActivity braceletActivity;
	private Bracelet bracelet;
	
	private TextView nameView;
	private TextView ownerView;
	private TextView picCountView;
	private TextView lastLocationView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		braceletActivity = (BraceletActivity) getActivity();
		bracelet = braceletActivity.bracelet;
		View rootView = inflater.inflate(R.layout.fragment_bracelet, container, false);
		nameView = (TextView) rootView.findViewById(R.id.braceletName);
		ownerView = (TextView) rootView.findViewById(R.id.braceletOwner);
		picCountView = (TextView) rootView.findViewById(R.id.braceletPicCount);
		lastLocationView = (TextView) rootView.findViewById(R.id.braceletLastLocation);
		return rootView;
	}
	
	public void updateData() {
		nameView.setText(bracelet.name);
		ownerView.setText(bracelet.owner);
		picCountView.setText(bracelet.picAnz + "");
		lastLocationView.setText(bracelet.lastCity + ", " + bracelet.lastCountry);
	}
}