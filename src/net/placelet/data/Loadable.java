package net.placelet.data;

import org.json.JSONObject;

import android.content.SharedPreferences;

public interface Loadable {
	
	public void loadData(JSONObject result);

	public void setProgressBar(boolean b);

	public SharedPreferences getPrefs();
}
