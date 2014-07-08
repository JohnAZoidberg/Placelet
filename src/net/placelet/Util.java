package net.placelet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.Display;
import android.widget.ImageView;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.squareup.picasso.Picasso;

@SuppressLint("SimpleDateFormat")
public class Util {
	public static int width;
	public static int height;
	public static Display display;

	public static String timestampToDate(long timestamp) {
		Date date = new Date(timestamp * 1000);
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
		String formattedDate = sdf.format(date);
		return formattedDate;
	}

	public static String timestampToTime(long timestamp) {
		String format;
		int diff = (int) Math.ceil(((System.currentTimeMillis() / 1000L) - timestamp) / 86400);
		if (diff == 0) {
			format = "HH:mm";
		} else {
			format = "dd.MM.yy HH:mm";
		}
		Date date = new Date(timestamp * 1000);
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		String formattedDate = sdf.format(date);
		return formattedDate;
	}

	public static String timeDiff(long timestamp, Context context) {
		int diff = (int) Math.ceil(((System.currentTimeMillis() / 1000L) - timestamp) / 86400);
		if (diff == 0) {
			return context.getString(R.string.today);
		} else if (diff == 1) {
			return context.getString(R.string.yesterday);
		} else if (diff > 7 && diff < 30) {
			if (diff / 7 == 1) {
				return context.getString(R.string.vor) + diff / 7 + context.getString(R.string.weeks_ago_sg);
			} else
				return context.getString(R.string.vor) + diff / 7 + context.getString(R.string.weeks_ago_pl);
		} else if (diff > 30 && diff < 365) {
			if (diff / 7 == 1) {
				return context.getString(R.string.vor) + diff / 30 + context.getString(R.string.months_ago_sg);
			} else
				return context.getString(R.string.vor) + diff / 30 + context.getString(R.string.months_ago_pl);
		} else {
			return context.getString(R.string.vor) + diff + context.getString(R.string.days_ago_pl);
		}
	}

	public static void saveData(SharedPreferences prefs, String key, String value) {
		if (!value.equals("{error: no_internet}")) {
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString(key, value);
			editor.commit();
		}
	}

	public static void loadThumbnail(Context context, ImageView imgView, int picid) {
		int picWidth = (int) (Util.width * 0.3);
		int picHeight = (int) (Util.height * 0.15);
		// different width and height ratio if in landscape orientation
		int orientation = Util.display.getRotation();
		if (orientation == 1 || orientation == 3) {
			picWidth = (int) (Util.width * 0.15);
			picHeight = (int) (Util.height * 0.3);
		}
		imgView.setMaxHeight(picWidth);
		imgView.setMaxWidth(picHeight);
		Picasso.with(context).load("http://placelet.de/pictures/bracelets/thumb-" + picid + ".jpg").placeholder(R.drawable.no_pic).resize(picWidth, picHeight).into(imgView);
		
	}
}
