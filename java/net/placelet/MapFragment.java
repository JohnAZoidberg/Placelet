package net.placelet;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class MapFragment extends Fragment {

    private SupportMapFragment mapFragment;
    private GoogleMap googleMap = null;
    private BraceletActivity braceletActivity;
    private Bracelet bracelet;
    private TextView distanceView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        braceletActivity = (BraceletActivity) getActivity();
        bracelet = braceletActivity.bracelet;
        mapFragment = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.map));
        //googleMap = mapFragment.getMap();
        if(googleMap != null) googleMap.getUiSettings().setRotateGesturesEnabled(false);
        distanceView = (TextView) rootView.findViewById(R.id.braceletDistance);
        return rootView;
    }

    private void initializeMap() {
        if (googleMap == null) {
            try {
                SupportMapFragment fragment = ((SupportMapFragment) braceletActivity.getSupportFragmentManager().findFragmentById(R.id.map));
                googleMap = fragment.getMap();
                // check if map is created successfully or not
                if (googleMap == null) {
                    Util.alert("Sorry! unable to create maps", braceletActivity);
                    System.out.println("nope");
                } else {
                    googleMap.getUiSettings().setRotateGesturesEnabled(false);
                }

            } catch (Exception e) {
                System.out.println("caught");
                e.printStackTrace();
            }
        }
    }

    private void putMarkers() {
        if(googleMap == null) initializeMap();
        if(googleMap == null) return;
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
        if (bracelet.isFilled()) {
            bracelet = braceletActivity.bracelet;
            distanceView.setText(bracelet.getDistance() + " km");
            putMarkers();
        }
    }
}
