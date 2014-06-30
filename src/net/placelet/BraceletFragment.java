package net.placelet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import net.placelet.R;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
 
public class BraceletFragment extends Fragment {
    
    private MainActivity mainActivity;
    private SharedPreferences prefs;
    private TextView textView;
    private String brid;
    private BraceletAdapter adapter;
    private List<Picture> pictureList = new ArrayList<Picture>();
    private ListView list;
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	mainActivity = (MainActivity) getActivity();
	prefs = mainActivity.prefs;
        View rootView = inflater.inflate(R.layout.fragment_bracelet, container, false);
	//Toast.makeText(mainActivity, "Whatsup?", Toast.LENGTH_LONG).show();
        textView = (TextView) rootView.findViewById(R.id.textView1);
        list = (ListView) rootView.findViewById(R.id.listView1);
        adapter = new BraceletAdapter(mainActivity, 0, pictureList);
        loadPictures();
        //Toast.makeText(mainActivity, "onCreate" + brid, Toast.LENGTH_LONG).show();
        return rootView;
    }
    
    @Override
    public void onStart() {
        //Toast.makeText(mainActivity, "onStart" + brid, Toast.LENGTH_LONG).show();
	super.onStart();
    }

    private class Pictures extends AsyncTask<String, String, JSONObject> {
	@Override
	protected JSONObject doInBackground(String... params) {
	    User user = new User(prefs);
	    JSONObject content = user.getBraceletPictures(brid);
	    return content;
	}

	@Override
	protected void onPostExecute(JSONObject result) {
	    mainActivity.setProgressBarIndeterminateVisibility(false);
	    updateListView(result);
	}
    }

    public void loadPictures() {
	if(mainActivity.brid != null) brid = mainActivity.brid;
	else brid = "588888";
	mainActivity.setProgressBarIndeterminateVisibility(true);
	Pictures pics = new Pictures();
	pics.execute();
	list.setAdapter(adapter);
	adapter.notifyDataSetChanged();
    }

    private void updateListView(JSONObject input) {
	pictureList.clear();
	for (Iterator<String> iter = input.keys(); iter.hasNext();) {
	    String key = iter.next();
	    try {
		JSONObject pictures = input.getJSONObject(key);
		Picture picture = new Picture();
		//picture.brid = pictures.getString("brid");
		picture.title = pictures.getString("title");
		picture.description = pictures.getString("description");
		picture.city = pictures.getString("city");
		picture.country = pictures.getString("country");
		picture.uploader = pictures.getString("user");
		picture.date = pictures.getLong("date");
		picture.id = pictures.getInt("id");
		picture.loadImage = prefs.getBoolean("downloadPermitted", false);
		pictureList.add(picture);
	    } catch (JSONException e) {
		e.printStackTrace();
	    }
	}

	Collections.sort(pictureList);
	list.setAdapter(adapter);
	adapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
	super.onActivityCreated(savedInstanceState);
    }
}