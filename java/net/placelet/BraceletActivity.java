package net.placelet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
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

import net.placelet.connection.User;
import net.placelet.connection.Webserver;
import net.placelet.data.Bracelet;
import net.placelet.data.Comment;
import net.placelet.data.Picture;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class BraceletActivity extends FragmentActivity {
    public Bracelet bracelet;
    public SharedPreferences prefs;
    public SharedPreferences settingsPrefs;
    private boolean reloadHidden = false;

    private static final int SUBSCRIBE_BUTTON = 1;

    private PictureDetailAdapter adapter;
    private ExpandableListView list;
    public SwipeRefreshLayout swipeLayout;

    private TextView headerView;
    private TextView startEndView;
    private TextView distanceView;
    private TextView showMapView;

    private SupportMapFragment mapFragment;
    private GoogleMap googleMap = null;

    private boolean showMap = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_bracelet);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(R.string.app_name);
        prefs = this.getSharedPreferences("net.placelet", Context.MODE_PRIVATE);
        settingsPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        googleMap = mapFragment.getMap();
        if(googleMap == null) Util.alert("null", this);

        Intent intent = getIntent();
        String brid = intent.getStringExtra("brid");
        bracelet = new Bracelet(brid);

        list = (ExpandableListView) findViewById(R.id.listView1);
        // collapses previous group if new one is expanded
        list.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            private int prevPosition = -1;
            @Override
            public void onGroupExpand(int groupPosition) {
                if(prevPosition != -1 && prevPosition != groupPosition) {
                    list.collapseGroup(prevPosition);
                }
                prevPosition = groupPosition;
            }
        });
        adapter = new PictureDetailAdapter(this, 0, bracelet.pictures);
        list.setAdapter(adapter);

        headerView = (TextView) findViewById(R.id.braceletHeader);
        distanceView = (TextView) findViewById(R.id.braceletDistance);
        startEndView = (TextView) findViewById(R.id.startEnd);
        showMapView = (TextView) findViewById(R.id.showMapView);

        RelativeLayout headerLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        headerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleVisibility();
            }
        });
        toggleVisibility();
        loadPictures(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Util.inflateActionBar(this, menu, reloadHidden);
        menu.add(Menu.NONE, SUBSCRIBE_BUTTON, 0, getString(R.string.subscribe)).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        if(bracelet != null) {
            MenuItem bedMenuItem = menu.findItem(SUBSCRIBE_BUTTON);
            if (bracelet.subscribed) {
                bedMenuItem.setTitle(getString(R.string.unsubscribe));
            } else {
                bedMenuItem.setTitle(getString(R.string.subscribe));
            }
        }
        invalidateOptionsMenu();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Reload
        switch (item.getItemId()) {
            case R.id.action_reload:
                loadPictures(true);
                break;
            case SUBSCRIBE_BUTTON:
                toggleSubscribe();
                break;
        }
        return NavigateActivities.activitySwitchMenu(item, this);
    }

    private void toggleSubscribe() {
        if(Util.notifyIfOffline(this)) {
            Subscription sub = new Subscription(this);
            sub.execute();
        }
    }

    public void postComment(int picid, String content, Button sendComment) {
        if(checkCommentContent(content)) {
            Commenting comm = new Commenting(picid, content, sendComment);
            comm.execute(picid);
        }else {
            Util.alert(getString(R.string.invalid_comment), this);
            sendComment.setEnabled(true);
        }
    }

    private boolean checkCommentContent(String content) {
        // if it contains the blockwords and doesn't contain "placelet" OR is empty it's false
        return !content.isEmpty() && !(!Util.stringContains(content, "placelet") && Util.stringContains(content, new String[]{"http", "www", ".com", ".de", ".net"}));
    }

    private class Commenting extends AsyncTask<Integer, Void, JSONObject> {
        private String content;
        private int picid;
        private Button sendComment;

        public Commenting(int picid, String content, Button sendComment) {
            this.picid = picid;
            this.content = content;
            this.sendComment = sendComment;
        }

        @Override
        protected JSONObject doInBackground(Integer... params) {
            User user = new User(prefs);
            return user.comment(bracelet.brid, picid, content);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            sendComment.setEnabled(true);
            // check if connected to the internet
            try {
                if (result.getString("error").equals("no_internet")) {
                    //toggleLoading(false);
                    return;
                }
            } catch (JSONException ignored) {
            }
            // check if new content
            try {
                String updateString = result.getString("update");
                if(User.admin) Util.alert("Update: " + updateString, BraceletActivity.this);
                toggleLoading(false);
                bracelet.subscribed = result.getBoolean("subscribed");
                invalidateOptionsMenu();
            } catch (JSONException e) {
                Util.saveDate(prefs, "getBraceletDataLastUpdate-" + bracelet.brid, System.currentTimeMillis() / 1000L);
                String jsonString = result.toString();
                Util.saveData(prefs, "braceletData-" + bracelet.brid, jsonString);
                updateBracelet(result);
            }
        }
    }

    private class Subscription extends AsyncTask<String, String, Boolean> {
        private Context context;
        public Subscription (Context context) {
            this.context = context;
        }
        @Override
        protected Boolean doInBackground(String... params) {
            User user = new User(prefs);
            return user.subscribe(bracelet.brid, !bracelet.subscribed);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(result) {
                if (bracelet.subscribed) {
                    bracelet.subscribed = false;
                    Util.alert(getString(R.string.unsubscribed), context);
                    invalidateOptionsMenu();
                } else {
                    bracelet.subscribed = true;
                    Util.alert(getString(R.string.subscribed), context);
                    invalidateOptionsMenu();
                }
            }
        }
    }

    private class BraceletData extends AsyncTask<String, String, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... params) {
            User user = new User(prefs);
            JSONObject content = user.getBraceletData(bracelet.brid);
            return content;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            // check if connected to the internet
            if(!Webserver.checkResult(result)) {
                toggleLoading(false);
                return;
            }
            // check if new content
            try {
                String updateString = result.getString("update");
                if(User.admin) Util.alert("Update: " + updateString, BraceletActivity.this);
                toggleLoading(false);
            } catch (JSONException e) {
                Util.saveDate(prefs, "getBraceletDataLastUpdate-" + bracelet.brid, System.currentTimeMillis() / 1000L);
                String jsonString = result.toString();
                Util.saveData(prefs, "braceletData-" + bracelet.brid, jsonString);
                updateBracelet(result);
            }
        }
    }

    public void loadPictures(boolean reload) {
        toggleLoading(true);
        // display saved pics if it shouldn't reload and if there are pics saved
        String savedBracelet = prefs.getString("braceletData-" + bracelet.brid, "null");
        if (!savedBracelet.equals("null") && !reload) {
            loadSavedBracelet(savedBracelet);
        }
        // load new pics from the internet
        if (Util.notifyIfOffline(this)) {
            BraceletData pics = new BraceletData();
            pics.execute();
        }else {
            toggleLoading(false);
        }
    }

    public void updateBracelet(JSONObject result) {
        //bracelet = new Bracelet(bracelet.brid);
        bracelet.clear();
        try {
            bracelet.owner = result.getString("owner");
            bracelet.name = result.getString("name");
            bracelet.date = result.getLong("date");
            bracelet.picAnz = result.getInt("pic_anz");
            bracelet.lastCity = result.getString("lastcity");
            bracelet.lastCountry = result.getString("lastcountry");
            bracelet.subscribed = result.getBoolean("subscribed");
        } catch (JSONException ignored) {
        }
        for (Iterator<?> iter = result.keys(); iter.hasNext();) {
            String key = (String) iter.next();
            try {
                JSONObject pictures = result.getJSONObject(key);
                Picture picture = new Picture();
                picture.braceName = bracelet.name;
                picture.title = pictures.getString("title");
                picture.description = pictures.getString("description");
                picture.city = pictures.getString("city");
                picture.country = pictures.getString("country");
                picture.uploader = pictures.getString("user");
                picture.date = pictures.getLong("date");
                picture.id = pictures.getInt("id");
                picture.fileext = pictures.getString("fileext");
                picture.latitude = pictures.getDouble("latitude");
                picture.longitude = pictures.getDouble("longitude");

                JSONObject comments;
                int i = 1;
                boolean commentExists = true;
                do {
                    try {
                        comments = pictures.getJSONObject(i + "");
                        Comment comment = new Comment();
                        comment.user = comments.getString("user");
                        comment.user = comment.user.equals("null") ? "Anonym" : comment.user;
                        comment.content = comments.getString("comment");
                        comment.date = comments.getLong("date");
                        picture.comments.add(comment);
                    } catch (JSONException e) {
                        commentExists = false;
                    }
                    i++;
                }while(commentExists);
                Comment comment = new Comment();
                comment.user = "JohnZoidberg";
                comment.content = "Testkommentar";
                comment.date = Integer.MAX_VALUE;
                picture.comments.add(comment);
                bracelet.pictures.add(picture);
            } catch (JSONException ignored) {
            }
        }
        invalidateOptionsMenu();
        bracelet.sort();
        bracelet.html_entity_decode();
        updateData();
        toggleLoading(false);
    }

    public void loadSavedBracelet(String result) {
        JSONObject jArray = null;
        try {
            jArray = new JSONObject(result);
            updateBracelet(jArray);
        } catch (JSONException ignored) {
        }
    }

    private void toggleLoading(boolean start) {
        if (start) {
            setProgressBarIndeterminateVisibility(true);
            reloadHidden = true;
        } else {
            setProgressBarIndeterminateVisibility(false);
            reloadHidden = false;
        }
        invalidateOptionsMenu();
    }

    private void initializeMap() {
        if (googleMap == null) {
            try {
                SupportMapFragment fragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
                googleMap = fragment.getMap();
                // check if map is created successfully or not
                if (googleMap == null) {
                    Util.alert("Sorry! unable to create maps", this);
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
            headerView.setText(bracelet.name + " " + getString(R.string.by) + " " + bracelet.owner);
            distanceView.setText(bracelet.getDistance() + " km");
            String firstLocation = bracelet.pictures.get(bracelet.pictures.size() - 1).city + ", " + bracelet.pictures.get(bracelet.pictures.size() - 1).country;
            String lastLocation = bracelet.pictures.get(0).city + ", " + bracelet.pictures.get(0).country;
            String text = "<font color='blue'>" + firstLocation + "</font> -->&nbsp;<font color='green'>" + lastLocation + "</font>";
            startEndView.setText(Html.fromHtml(text), TextView.BufferType.SPANNABLE);
            putMarkers();

            adapter.notifyDataSetChanged();
        }
    }

    public void toggleVisibility() {
        if (showMap) {
            distanceView.setVisibility(View.GONE);
            mapFragment.getView().setVisibility(View.GONE);
            list.setVisibility(View.VISIBLE);
            showMap = false;
            showMapView.setText("Karte anzeigen");
        }else {
            distanceView.setVisibility(View.VISIBLE);
            mapFragment.getView().setVisibility(View.VISIBLE);
            list.setVisibility(View.GONE);
            showMap = true;
            showMapView.setText("Bilder anzeigen");
        }
    }
}
