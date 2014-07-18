package net.placelet.data;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import net.placelet.HTMLDecodable;
import net.placelet.Util;
import net.placelet.connection.User;

public class Bracelet implements HTMLDecodable {
	public String name = null;
	public String owner = null;
	public String brid = null;
	public long date = -1;

	public int picAnz = -1;
	public String lastCity = null;
	public String lastCountry = null;

	public ArrayList<Picture> pictures = new ArrayList<Picture>();
	
	private Loadable activity;

	public Bracelet(String brid, Loadable activity) {
		this.activity = activity;
		this.brid = brid;
		BraceletData pics = new BraceletData();
		pics.execute();
	}

	@Override
	public void html_entity_decode() {
		if (name != null) {
			name = StringEscapeUtils.unescapeHtml4(name);
		}
		if (owner != null) {
			owner = StringEscapeUtils.unescapeHtml4(owner);
		}
		if (lastCity != null) {
			lastCity = StringEscapeUtils.unescapeHtml4(lastCity);
		}
		if (lastCountry != null) {
			lastCountry = StringEscapeUtils.unescapeHtml4(lastCountry);
		}
		if (pictures != null && pictures.size() > 0) {
			for (Picture picture : pictures) {
				picture.html_entity_decode();
			}
		}
	}

	@Override
	public void urlencode() {
		if (pictures != null && pictures.size() > 0)
			for (Picture picture : pictures) {
				picture.urlencode();
			}
		if (name != null)
			try {
				name = URLEncoder.encode(name, "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		if (owner != null)
			try {
				owner = URLEncoder.encode(owner, "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		if (lastCity != null)
			try {
				lastCity = URLEncoder.encode(lastCity, "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		if (lastCountry != null)
			try {
				lastCountry = URLEncoder.encode(lastCountry, "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
	}
	
	public boolean isFilled() {
		if (name != null && owner != null && brid!= null && date != -1 && picAnz != 1 && lastCity != null &&lastCountry != null && pictures.size() > 0) {
			return true;
		}
		return false;
	}

	private class BraceletData extends AsyncTask<String, String, JSONObject> {
		SharedPreferences prefs = activity.getPrefs();
		@Override
		protected JSONObject doInBackground(String... params) {
			User user = new User(prefs);
			JSONObject content = user.getBraceletData(brid);
			return content;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			// check if connected to the internet
			try {
				if (result.getString("error").equals("no_internet")) {
					activity.setProgressBar(false);
					return;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			String jsonString = result.toString();
			Util.saveData(prefs, "braceletData-" + brid, jsonString);
			activity.loadData(result);
		}
	}
}
