package net.placelet.connection;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import net.placelet.data.Picture;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.json.JSONObject;

import com.pushbots.push.Pushbots;

import android.content.SharedPreferences;

public class User {
	private SharedPreferences prefs;
	public final static int ERROR = 0;
	public final static int NOT_EXISTING = 1;
	public final static int WRONG_PW = 2;
	public final static int SUCCESS = 3;
	public final static String NOT_LOGGED_IN = "logged_out";

	public static String username;
	public static String dynPW;
	
	public static boolean trial = false;

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

	public JSONObject sendMessage(String recipient, String content) {
		Webserver server = new Webserver();
		HashMap<String, String> args = new HashMap<String, String>();
		// Login-Variables
		args.put("user", username);
		args.put("dynPW", dynPW);
		// Message-Variables
		args.put("recipient", recipient);
		args.put("content", content);
		args.put("androidSendMessages", "true");
		return server.postRequest(args);
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
		picture.urlencode();
		int error = 8;
		picture.date = System.currentTimeMillis() / 1000L;
		JSONObject result = null;
		Webserver server = new Webserver();
		String post = "androidUploadPicture=true" + "&brid=" + picture.brid + "&description=" + picture.description + "&city=" + picture.city
				+ "&country=" + picture.country + "&title=" + picture.title + "&date=" + picture.date + "&latitude=" + picture.latitude
				+ "&longitude=" + picture.longitude + "&user=" + User.username + "&dynPW=" + User.dynPW;
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
		JSONObject result = null;
		Webserver server = new Webserver();
		HashMap<String, String> args = new HashMap<String, String>();
        args.put("androidAuthenticate", "true");
        args.put("user", username);
        args.put("dynPW", dynPW);

		args.put("androidGetBraceletData", "true");
		args.put("braceID", brid);
		result = server.postRequest(args);
		return result;
	}

	public JSONObject getOwnBracelets() {
		JSONObject result = null;
		Webserver server = new Webserver();
		HashMap<String, String> args = new HashMap<String, String>();
		args.put("androidGetOwnBracelets", "true");
		args.put("user", username);
		result = server.postRequest(args);
		return result;
	}

    public boolean subscribe(String brid) {
        JSONObject result = null;
        Webserver server = new Webserver();
        HashMap<String, String> args = new HashMap<String, String>();
        args.put("androidAuthenticate", "true");
        args.put("user", username);
        args.put("dynPW", dynPW);

        args.put("androidSubscribe", "true");
        args.put("brid", brid);
        result = server.postRequest(args);
        try {
            result.getBoolean("subscribed");
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    public boolean unsubscribe(String brid) {
        JSONObject result = null;
        Webserver server = new Webserver();
        HashMap<String, String> args = new HashMap<String, String>();
        args.put("androidAuthenticate", "true");
        args.put("user", username);
        args.put("dynPW", dynPW);

        args.put("androidSubscribe", "false");
        args.put("brid", brid);
        result = server.postRequest(args);
        try {
            result.getBoolean("subscribed");
            return true;
        } catch (JSONException e) {
            return false;
        }
    }
}