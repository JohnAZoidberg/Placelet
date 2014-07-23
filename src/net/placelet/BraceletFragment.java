package net.placelet;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import net.placelet.data.Bracelet;
import net.placelet.data.Picture;
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

	private GoogleMap googleMap;

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
		try {
			// Loading map
			initializeMap();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return rootView;
	}

	private void initializeMap() {
		if (googleMap == null) {
			googleMap = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
			// check if map is created successfully or not
			if (googleMap == null) {
				Util.alert("Sorry! unable to create maps", braceletActivity);
			}else {
				googleMap.getUiSettings().setRotateGesturesEnabled(false);
				putMarkers();
			}
		}
	}

	private void putMarkers() {
		initializeMap();
		PolylineOptions rectOptions = new PolylineOptions();
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		for (Picture picture : bracelet.pictures) {
			LatLng latLng = new LatLng(picture.latitude, picture.longitude);
			MarkerOptions marker = new MarkerOptions().position(latLng).title(picture.title);
			googleMap.addMarker(marker);

			rectOptions.add(latLng);
			googleMap.addPolyline(rectOptions);

			builder.include(latLng);
		}
		LatLngBounds bounds = builder.build();// TODO sometimes error: no included points
		
		CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 25, 25, 5);
		googleMap.moveCamera(cu);
	}

	public void updateData() {
		bracelet = braceletActivity.bracelet;
		if (bracelet.isFilled()) {
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
		putMarkers();
	}
}