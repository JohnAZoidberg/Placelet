package net.placelet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.json.JSONObject;

import com.pushbots.push.Pushbots;

import android.content.SharedPreferences;

public class User {
    private SharedPreferences prefs;
    final static int ERROR = 0;
    final static int NOT_EXISTING = 1;
    final static int WRONG_PW = 2;
    final static int SUCCESS = 3;
    final static String NOT_LOGGED_IN = "logged_out";

    public static String username;
    public static String dynPW;

    public User(SharedPreferences preferences) {
	prefs = preferences;
    }

    public static String getUsername() {
	return username;
    }

    public static String getDynPW() {
	return dynPW;
    }

    public void logout() {
	SharedPreferences.Editor editor = prefs.edit();
	username = NOT_LOGGED_IN;
	dynPW = NOT_LOGGED_IN;
	editor.putString("username", NOT_LOGGED_IN);
	editor.putString("dynPW", NOT_LOGGED_IN);
	editor.putInt("userid", 0);
	editor.commit();
    }

    public int login(String user, String pasword) {
	String deviceToken = Pushbots.getInstance().getSharedPrefs()
		.getRegistrationID();
	SharedPreferences.Editor editor = prefs.edit();
	JSONObject result;
	Webserver server = new Webserver();
	HashMap<String, String> args = new HashMap<String, String>();
	args.put("user", user);
	args.put("pasword", pasword);
	args.put("androidLogin", "true");
	args.put("deviceToken", deviceToken);
	try {
	    result = server.postRequest(args);
	    if (result.getBoolean("success")) {
		int userid = result.getInt("userid");
		String dynamicPW = result.getString("dynPW");
		// boolean firstLogin = result.getBoolean("firstLogin");
		// boolean admin = result.getBoolean("admin");

		username = user;
		dynPW = dynamicPW;
		editor.putInt("userid", userid);
		editor.putString("dynPW", dynamicPW);
		editor.putString("username", user);
		editor.commit();
		return SUCCESS;
	    } else {
		switch (result.getInt("error")) {
		case NOT_EXISTING:
		    return NOT_EXISTING;
		case WRONG_PW:
		    return WRONG_PW;

		}

	    }

	} catch (JSONException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return 0;
    }

    public JSONObject getProfileInfo(String user) {
	JSONObject result;
	Webserver server = new Webserver();
	HashMap<String, String> args = new HashMap<String, String>();
	args.put("user", user);
	args.put("androidProfileInfo", "true");
	result = server.postRequest(args);
	/*
	 * String resultString = ""; try { result = server.postRequest(args);
	 * for (Iterator<String> iter = result.keys(); iter.hasNext();) { String
	 * key = iter.next(); resultString += key + " : " +
	 * result.getString(key) + "\n"; }
	 * 
	 * } catch (JSONException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); }
	 */
	return result;
    }

    public JSONObject getMessages() {
	JSONObject result = null;
	Webserver server = new Webserver();
	HashMap<String, String> args = new HashMap<String, String>();
	args.put("user", username);
	args.put("dynPW", dynPW);
	args.put("androidGetMessages", "true");
	result = server.postRequest(args);
	return result;
    }

    public JSONObject getIOMessages(String recipient) {
	JSONObject result = null;
	Webserver server = new Webserver();
	HashMap<String, String> args = new HashMap<String, String>();
	args.put("user", username);
	args.put("dynPW", dynPW);
	args.put("recipient", recipient);
	args.put("androidGetIOMessages", "true");
	result = server.postRequest(args);
	return result;
    }

    public String sendMessage(String recipient, String content) {
	Webserver server = new Webserver();
	HashMap<String, String> args = new HashMap<String, String>();
	// Login-Variables
	args.put("user", username);
	args.put("dynPW", dynPW);
	// Message-Variables
	args.put("recipient", recipient);
	args.put("content", content);
	args.put("androidSendMessages", "true");
	return server.stringPostRequest(args);
    }

    public JSONObject getCommunityPictures(int picCount) {
	JSONObject result = null;
	Webserver server = new Webserver();
	HashMap<String, String> args = new HashMap<String, String>();
	args.put("androidGetCommunityPictures", "true");
	args.put("pic_count", "" + picCount);
	result = server.postRequest(args);
	return result;
    }

    public int uploadPicture(Picture picture, String filepath) {
	int error = 8;
	picture.date = System.currentTimeMillis() / 1000L;
	JSONObject result = null;
	Webserver server = new Webserver();
	String post = "androidUploadPicture=true" + "&brid=" + picture.brid
		+ "&description=" + picture.description + "&city="
		+ picture.city + "&country=" + picture.country + "&title="
		+ picture.title + "&date=" + picture.date + "&latitude="
		+ picture.latitude + "&longitude=" + picture.longitude
		+ "&user=" + User.username + "&dynPW=" + User.dynPW;
	String filefield = "uploadPic";

	String resultString = "{}";
	try {
	    resultString = server.multipartRequest(post, filepath, filefield);
	} catch (ParseException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	try {
	    result = new JSONObject(resultString);
	    error = result.getInt("upload");
	} catch (JSONException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return error;
    }

    public JSONObject getBraceletPictures(String brid) {
	JSONObject result = null;
	Webserver server = new Webserver();
	HashMap<String, String> args = new HashMap<String, String>();
	args.put("androidGetBraceletPictures", "true");
	args.put("braceID", brid);
	result = server.postRequest(args);
	return result;
    }

    public String writeTest(String content) {
	JSONObject result = null;
	Webserver server = new Webserver();
	HashMap<String, String> args = new HashMap<String, String>();
	args.put("androidText", "true");
	args.put("textContent", content);
	String resultString = "";
	try {
	    result = server.postRequest(args);
	    for (Iterator<String> iter = result.keys(); iter.hasNext();) {
		String key = iter.next();
		resultString += key + " : " + result.getString(key) + "\n";
	    }

	} catch (JSONException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return resultString;
    }
}