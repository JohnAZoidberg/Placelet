package net.placelet.connection;

import android.content.SharedPreferences;

import com.pushbots.push.Pushbots;

import net.placelet.Util;
import net.placelet.data.Picture;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

public class User {
	private SharedPreferences prefs;
	public final static int ERROR = 0;
	public final static int NOT_EXISTING = 1;
	public final static int WRONG_PW = 2;
	public final static int SUCCESS = 3;
	public final static String NOT_LOGGED_IN = "logged_out";

	public static String username;
	public static String dynPW;
    public static boolean admin;
	
	public static boolean trial = false;

	public User(SharedPreferences preferences) {
		prefs = preferences;
	}

	/*public static String getUsername() {
		return username;
	}

	public static String getDynPW() {
		return dynPW;
	}*/

	public void logout() {
		username = NOT_LOGGED_IN;
		dynPW = NOT_LOGGED_IN;
        admin = false;
        Util.resetUpdates(prefs);
        SharedPreferences.Editor editor = prefs.edit();
		editor.putString("username", NOT_LOGGED_IN);
		editor.putString("dynPW", NOT_LOGGED_IN);
        editor.putBoolean("admin", false);
		editor.putInt("userid", 0);
		editor.apply();
	}

	public int login(String user, String password) {
		String deviceToken = Pushbots.getInstance().getSharedPrefs().getRegistrationID();
		SharedPreferences.Editor editor = prefs.edit();
		JSONObject result;
		Webserver server = new Webserver();
		HashMap<String, String> args = new HashMap<String, String>();
		args.put("user", user);
		args.put("password", password);
		args.put("androidLogin", "true");
		args.put("deviceToken", deviceToken);
		try {
			result = server.postRequest(args);
			if (result.getBoolean("success")) {
				int userid = result.getInt("userid");
				String dynamicPW = result.getString("dynPW");
				// boolean firstLogin = result.getBoolean("firstLogin");
				boolean status= result.getBoolean("admin");

				username = user;
				dynPW = dynamicPW;
                admin = status;
				editor.putInt("userid", userid);
				editor.putString("dynPW", dynamicPW);
				editor.putString("username", user);
                editor.putBoolean("admin", status);
				editor.apply();
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
			e.printStackTrace();
		}
		return 0;
	}

	public int register(String user, String password, String email) {
		JSONObject result;
		Webserver server = new Webserver();
		HashMap<String, String> args = new HashMap<String, String>();
		args.put("reg_login", user);
		args.put("reg_password", password);
		args.put("reg_email", email);
		args.put("androidRegister", "true");
		try {
			result = server.postRequest(args);
			return result.getInt("registered");
		} catch (JSONException e) {
			e.printStackTrace();
		}
        return 0;
	}

	public JSONObject getMessages() {
        String lastUpdate = "" + prefs.getLong("getMessagesLastUpdate", 0);
		JSONObject result = null;
		Webserver server = new Webserver();
		HashMap<String, String> args = new HashMap<String, String>();
		args.put("user", username);
		args.put("dynPW", dynPW);
		args.put("androidGetMessages", "true");
        args.put("androidAuthenticate", "true");
        args.put("lastUpdate", lastUpdate);
		result = server.postRequest(args);
		return result;
	}

	public JSONObject getIOMessages(String recipient) {
        String lastUpdate = "" + prefs.getLong("getIOMessagesLastUpdate-" + recipient, 0);
		JSONObject result = null;
		Webserver server = new Webserver();
		HashMap<String, String> args = new HashMap<String, String>();
		args.put("user", username);
		args.put("dynPW", dynPW);
		args.put("recipient", recipient);
        args.put("androidAuthenticate", "true");
		args.put("androidGetIOMessages", "true");
        args.put("lastUpdate", lastUpdate);
		result = server.postRequest(args);
		return result;
	}

	public JSONObject sendMessage(String recipient, String content) {
        String lastUpdate = "" + prefs.getLong("getIOMessagesLastUpdate-" + recipient, 0);
		Webserver server = new Webserver();
		HashMap<String, String> args = new HashMap<String, String>();
		// Login-Variables
		args.put("user", username);
		args.put("dynPW", dynPW);
		// Message-Variables
		args.put("recipient", recipient);
		args.put("content", content);
		args.put("androidSendMessages", "true");
        args.put("androidAuthenticate", "true");
        args.put("lastUpdate", lastUpdate);
		return server.postRequest(args);
	}

	public JSONObject getCommunityPictures(int picStart, int picCount) {
        String lastUpdate = "" + prefs.getLong("getCommunityPicturesLastUpdate", 0);
		JSONObject result = null;
		Webserver server = new Webserver();
		HashMap<String, String> args = new HashMap<String, String>();
		args.put("androidGetCommunityPictures", "true");
        args.put("user", username);
        args.put("v", Webserver.androidVersion);
		args.put("pic_start", "" + picStart);
        args.put("pic_count", "" + picCount);
        args.put("lastUpdate", lastUpdate);
		result = server.postRequest(args);
		return result;
	}

	public int uploadPicture(Picture picture, String filepath) {
		picture.urlencode();
		int error = 8;
		picture.date = System.currentTimeMillis() / 1000L;
		JSONObject result = null;
		Webserver server = new Webserver();
		String post = "androidUploadPicture=true" + "&brid=" + picture.brid + "&description=" + picture.description + "&city=" + picture.city
				+ "&country=" + picture.country + "&title=" + picture.title + "&date=" + picture.date + "&latitude=" + picture.latitude
				+ "&longitude=" + picture.longitude + "&user=" + User.username + "&dynPW=" + User.dynPW + "androidAuthenticate=true";
		String filefield = "uploadPic";

		String resultString = "{}";
		try {
			resultString = server.multipartRequest(post, filepath, filefield);
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			result = new JSONObject(resultString);
			error = result.getInt("upload");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return error;
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
			for (Iterator<?> iter = result.keys(); iter.hasNext();) {
				String key = (String) iter.next();
				resultString += key + " : " + result.getString(key) + "\n";
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return resultString;
	}
	
	// returns true if logged in and false if logged out
	public static boolean getStatus() {
		if (User.username.equals(User.NOT_LOGGED_IN)) {
			return false;
		}else {
			return true;
		}
	}

	public JSONObject getBraceletData(String brid) {
        String lastUpdate = "" + prefs.getLong("getBraceletDataLastUpdate-" + brid, 0);
		JSONObject result = null;
		Webserver server = new Webserver();
		HashMap<String, String> args = new HashMap<String, String>();
        args.put("androidAuthenticate", "true");
        args.put("user", username);
        args.put("dynPW", dynPW);
        args.put("lastUpdate", lastUpdate);

		args.put("androidGetBraceletData", "true");
		args.put("braceID", brid);
		result = server.postRequest(args);
		return result;
	}

	public JSONObject getOwnBracelets() {
        String lastUpdate = "" + prefs.getLong("getOwnBraceletsLastUpdate", 0);
		JSONObject result = null;
		Webserver server = new Webserver();
		HashMap<String, String> args = new HashMap<String, String>();
		args.put("androidGetOwnBracelets", "true");
		args.put("user", username);
        args.put("lastUpdate", lastUpdate);
		result = server.postRequest(args);
		return result;
	}

    public boolean subscribe(String brid, boolean subscribe) {
        JSONObject result = null;
        Webserver server = new Webserver();
        HashMap<String, String> args = new HashMap<String, String>();
        args.put("androidAuthenticate", "true");
        args.put("user", username);
        args.put("dynPW", dynPW);

        String subParam = "true";
        if(!subscribe) subParam = "false";
        args.put("androidSubscribe", subParam);
        args.put("brid", brid);
        result = server.postRequest(args);
        try {
            result.getBoolean("subscribed");
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    public int registerBracelet(String brid) {
        JSONObject result = null;
        Webserver server = new Webserver();
        HashMap<String, String> args = new HashMap<String, String>();
        args.put("androidAuthenticate", "true");
        args.put("user", username);
        args.put("dynPW", dynPW);

        args.put("androidRegisterBracelet", "true");
        args.put("brid", brid);
        result = server.postRequest(args);
        try {
            return result.getInt("registered");
        } catch (JSONException e) {
            return -1;
        }
    }
}