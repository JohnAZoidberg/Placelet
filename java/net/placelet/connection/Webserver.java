package net.placelet.connection;

import android.net.http.AndroidHttpClient;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Webserver {
	public static final String androidVersion = "1.2.4";
    private static final boolean debug = true;
	private String connectionURL = "http://placelet.de/android/android" + androidVersion + ".php";
	HttpClient httpClient = new DefaultHttpClient();
	HttpPost httpPost = new HttpPost(connectionURL);

	public JSONObject postRequest(HashMap<String, String> args) {
		JSONObject jArray = null;
		String result = stringPostRequest(args);
		try {
			jArray = new JSONObject(result);
		} catch (JSONException e) {
            try {
                jArray = new JSONObject("{error: \"server\"}");
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            Log.e("log_tag", "Error parsing data: " + e.toString() + "\n\"" + result + "\"");
		}

		return jArray;
	}

	public String stringPostRequest(HashMap<String, String> args) {
        httpPost.addHeader("Accept-Encoding", "gzip");
        AndroidHttpClient.modifyRequestToAcceptGzipResponse(httpPost);
		InputStream is = null;
		String result = "";
		List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(args.size());
		Iterator<String> iter = args.keySet().iterator();

		while (iter.hasNext()) {
			String key = (String) iter.next();
			String val = (String) args.get(key);
			nameValuePair.add(new BasicNameValuePair(key, val));
		}
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
			HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            is = AndroidHttpClient.getUngzippedContent(response.getEntity());
            //is = entity.getContent(); // Use for debugging if a PHP error causes IOException
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
            e.printStackTrace();
		}
		// Convert response to string
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			result = sb.toString();
		} catch (Exception e) {
			Log.e("log_tag", "Error converting result " + e.toString());
			result = "{error: no_internet}";
		}
        double length_before = result.length();
        Log.e("stats", "Length of request before minifying: " + result.length());
        Log.e("debug", "Data from Webserver(PHP) before minifying: \n\"" + result + "\"");
        result = deminify_json(result);
        double length_after = result.length();
        Log.e("debug", "Data from Webserver(PHP): \n\"" + result + "\"");
        Log.e("stats", "Length of request: " + result.length());
        Log.e("stats", "Saving: " + (length_after / length_before));
		return result;
	}

    public String multipartRequest(String post, String filepath, String filefield) throws ParseException, IOException {
		String urlTo = connectionURL;
		HttpURLConnection connection = null;
		DataOutputStream outputStream = null;
		InputStream inputStream = null;

		String twoHyphens = "--";
		String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";
		String lineEnd = "\r\n";

		String result = "";

		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024;

		String[] q = filepath.split("/");
		int idx = q.length - 1;

		try {
			File file = new File(filepath);
			FileInputStream fileInputStream = new FileInputStream(file);

			URL url = new URL(urlTo);
			connection = (HttpURLConnection) url.openConnection();

			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);

			connection.setRequestMethod("POST");
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0");
			connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

			outputStream = new DataOutputStream(connection.getOutputStream());
			outputStream.writeBytes(twoHyphens + boundary + lineEnd);
			outputStream.writeBytes("Content-Disposition: form-data; name=\"" + filefield + "\"; filename=\"" + q[idx] + "\"" + lineEnd);
			outputStream.writeBytes("Content-Type: image/jpeg" + lineEnd);
			outputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);
			outputStream.writeBytes(lineEnd);

			bytesAvailable = fileInputStream.available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];

			bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			while (bytesRead > 0) {
				outputStream.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}

			outputStream.writeBytes(lineEnd);

			// Upload POST Data
			String[] posts = post.split("&");
			int max = posts.length;
			for (int i = 0; i < max; i++) {
				outputStream.writeBytes(twoHyphens + boundary + lineEnd);
				String[] kv = posts[i].split("=");
				outputStream.writeBytes("Content-Disposition: form-data; name=\"" + kv[0] + "\"" + lineEnd);
				outputStream.writeBytes("Content-Type: text/plain" + lineEnd);
				outputStream.writeBytes(lineEnd);
				outputStream.writeBytes(kv[1]);
				outputStream.writeBytes(lineEnd);
			}

			outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

			inputStream = connection.getInputStream();
			result = convertStreamToString(inputStream);

			fileInputStream.close();
			inputStream.close();
			outputStream.flush();
			outputStream.close();

            double length_before = result.length();
            Log.e("stats", "Length of request before minifying: " + result.length());
            Log.e("debug", "Data from Webserver(PHP) before minifying: \n\"" + result + "\"");
            result = deminify_json(result);
            double length_after = result.length();
            Log.e("debug", "Data from Webserver(PHP): \n\"" + result + "\"");
            Log.e("stats", "Length of request: " + result.length());
            Log.e("stats", "Saving: " + (length_after / length_before));
			return result;
		} catch (Exception e) {
			Log.e("MultipartRequest", "Multipart Form Upload Error");
			e.printStackTrace();
			return "error";
		}
	}

	private String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

    public static boolean checkConnection(JSONObject result) {
        // check if connected to the internet
        try {
            if (result.getString("error").equals("no_internet")) {
                return false;
            }
        } catch (JSONException e) {
            return true;
        }
        return true;
    }

    private String deminify_json(String json) {
        String username_json = "John#Zoidberg";
        if(!json.contains("John#Zoidberg")) username_json = User.username;
        String[] searchList = {"1‡", "2‡", "3‡", "4‡", "5‡", "6‡", "7‡", "8‡", "9‡", "‡10", "‡11", "‡12", "‡13", "‡14", "‡15", "‡16", "‡17", "‡18", "‡19", "‡20", "‡21", "‡22", "‡3", "‡24", "‡25", "‡26", "‡27", "‡28", "‡29"};
        String[] replaceList = {"recipient", "name", "sender", "sent", "seen", "message", "update", "exists", "brid", "title", "description", "city", "country", "userid", "date", "upload", "user", "user", "ownBracelet", "alreadyUpToDate", "picid", "longitude", "latitude", "state", "commid", "fileext", username_json, "Deutschland", "United States"};
        return StringUtils.replaceEach(json,  searchList, replaceList);
    }
}