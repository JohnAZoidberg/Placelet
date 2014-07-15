package net.placelet;

import net.placelet.data.Bracelet;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class BraceletFragment extends Fragment {
	private BraceletActivity braceletActivity;
	private Bracelet bracelet;

	private TextView nameView;
	private TextView ownerView;
	private TextView picCountView;
	private TextView lastLocationView;
	private ImageView imgView1;
	private ImageView imgView2;
	private ImageView imgView3;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		braceletActivity = (BraceletActivity) getActivity();
		bracelet = braceletActivity.bracelet;
		View rootView = inflater.inflate(R.layout.fragment_bracelet, container, false);

		nameView = (TextView) rootView.findViewById(R.id.braceletName);
		ownerView = (TextView) rootView.findViewById(R.id.braceletOwner);
		picCountView = (TextView) rootView.findViewById(R.id.braceletPicCount);
		lastLocationView = (TextView) rootView.findViewById(R.id.braceletLastLocation);
		imgView1 = (ImageView) rootView.findViewById(R.id.imageView1);
		imgView2 = (ImageView) rootView.findViewById(R.id.imageView2);
		imgView3 = (ImageView) rootView.findViewById(R.id.imageView3);
		updateData();
		return rootView;
	}

	public void updateData() {
		nameView.setText(bracelet.name);
		ownerView.setText(bracelet.owner);
		picCountView.setText(bracelet.picAnz + "");
		lastLocationView.setText(bracelet.lastCity + ", " + bracelet.lastCountry);

		switch (bracelet.pictures.size()) {
			default:
			case 3:
				Util.loadThumbnail(braceletActivity, imgView3, bracelet.pictures.get(2).id);
			case 2:
				Util.loadThumbnail(braceletActivity, imgView2, bracelet.pictures.get(1).id);
			case 1:
				Util.loadThumbnail(braceletActivity, imgView1, bracelet.pictures.get(0).id);
				break;
			case 0:
				break;
		}
	}
}