package net.placelet;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import net.placelet.data.Bracelet;
import net.placelet.data.Picture;

import java.util.Iterator;

public class BraceletFragment extends Fragment {
	private BraceletActivity braceletActivity;
	private Bracelet bracelet;

	private TextView headerView;
    private TextView startEndView;
	private ImageView imgView1;
	private ImageView imgView2;
	private ImageView imgView3;
    private TextView distanceView;

	private GoogleMap googleMap;
    private LinearLayout linearLayout;

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		braceletActivity = (BraceletActivity) getActivity();
		bracelet = braceletActivity.bracelet;
		View rootView = inflater.inflate(R.layout.fragment_bracelet, container, false);

		headerView = (TextView) rootView.findViewById(R.id.braceletHeader);
        distanceView = (TextView) rootView.findViewById(R.id.braceletDistance);
        startEndView = (TextView) rootView.findViewById(R.id.startEnd);
		imgView1 = (ImageView) rootView.findViewById(R.id.imageView1);
		imgView2 = (ImageView) rootView.findViewById(R.id.imageView2);
		imgView3 = (ImageView) rootView.findViewById(R.id.imageView3);
        linearLayout = (LinearLayout) rootView.findViewById(R.id.linearLayout1);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                braceletActivity.switchFragment(1);
            }
        });
		updateData();
		initializeMap();
		return rootView;
	}

	private void initializeMap() {
		if (googleMap == null) {
			try {
				googleMap = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
				// check if map is created successfully or not
				if (googleMap == null) {
					Util.alert("Sorry! unable to create maps", braceletActivity);
				} else {
					googleMap.getUiSettings().setRotateGesturesEnabled(false);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void putMarkers() {
		initializeMap();
		PolylineOptions rectOptions = new PolylineOptions();
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
        boolean firstMarker = true;
		for (Iterator<Picture> i = bracelet.pictures.iterator(); i.hasNext(); ) {
            Picture picture = i.next();
			LatLng latLng = new LatLng(picture.latitude, picture.longitude);
			MarkerOptions marker = new MarkerOptions().position(latLng).title(picture.title);
            if(firstMarker) {
                marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                firstMarker = false;
            }
            if (!i.hasNext()) {
                marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            }
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
            headerView.setText(bracelet.name + " " + braceletActivity.getString(R.string.by) + " " + bracelet.owner);
            distanceView.setText(bracelet.getDistance() + " km");
            String firstLocation = bracelet.pictures.get(bracelet.pictures.size() - 1).city + ", " + bracelet.pictures.get(bracelet.pictures.size() - 1).country;
            String lastLocation = bracelet.pictures.get(0).city + ", " + bracelet.pictures.get(0).country;
            String text = "<font color='blue'>" + firstLocation + "</font> -->&nbsp;<font color='green'>" + lastLocation + "</font>";
            startEndView.setText(Html.fromHtml(text), TextView.BufferType.SPANNABLE);

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
			putMarkers();
		}
	}
}