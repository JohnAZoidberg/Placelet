package net.placelet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.text.InputType;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import net.placelet.connection.User;
import net.placelet.data.Picture;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

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
			editor.apply();
		}
	}

    public static void saveDate(SharedPreferences prefs, String key, long date) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(key, date);
        editor.apply();
    }

	public static void loadThumbnail(Context context, ImageView imgView, int picid) {
        float dpi = context.getResources().getDisplayMetrics().density;
		int picWidth = (int) (108 * (dpi));
		int picHeight = (int) (96 * (dpi));
		Picasso.with(context).load("http://placelet.de/pictures/bracelets/thumb-" + picid + ".jpg").resize(picWidth, picHeight).into(imgView);
	}

	public static void inflateActionBar(Activity activity, Menu menu, boolean noReload) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = activity.getMenuInflater();
		inflater.inflate(R.menu.action_bar, menu);
		if (noReload) {
			MenuItem item = menu.findItem(R.id.action_reload);
			item.setVisible(false);
		}
		if (!User.getStatus()) {
			// change logout to login
			MenuItem logoutItem = menu.findItem(R.id.action_logout);
			logoutItem.setTitle(R.string.login_uc);
		}
		if (noReload || !User.getStatus()) {
			activity.invalidateOptionsMenu();
		}
	}

	public static boolean isOnline(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
			return true;
		}
		return false;
	}

	public static boolean notifyIfOffline(Context context) {
		boolean isOnline = Util.isOnline(context);
		if (!isOnline) {
			Toast.makeText(context, R.string.offline, Toast.LENGTH_SHORT).show();
		}
		return isOnline;
	}

	public static void alert(String msg, Context context) {
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}

    public static int getDistance(double latFrom, double longFrom, double latTo, double longTo){
        // convert from degrees to radians
        latFrom = Math.toRadians(latFrom);
        longFrom = Math.toRadians(longFrom);
        latTo = Math.toRadians(latTo);
        longTo = Math.toRadians(longTo);

        double latDelta = latTo - latFrom;
        double longDelta = longTo - longFrom;

        double angle = 2.0 * Math.asin(Math.sqrt(Math.pow(Math.sin(latDelta / 2.0), 2) + Math.cos(latFrom) * Math.cos(latTo) * Math.pow(Math.sin(longDelta / 2.0), 2)));
        return (int) (angle * 6378137.0 / 1000);
    }

    public static void resetUpdates(SharedPreferences prefs) {
        Map<String, ?> prefsAll = prefs.getAll();
        String[] updateKeys = {
                "getCommunityPicturesLastUpdate",
                "getBraceletDataLastUpdate",
                "getOwnBraceletsLastUpdate",
                "getMessagesLastUpdate",
                "getIOMessagesLastUpdate",
        };
        String[] dataKeys = {
                "communityPics",
                "braceletData",
                "myPlacelet",
                "messages",
                "IOmessages-"

        };
        SharedPreferences.Editor editor = prefs.edit();
        for(Map.Entry<String, ?> entry : prefsAll.entrySet()) {
            if(stringContains(entry.getKey(), updateKeys)) {
                editor.putLong(entry.getKey(), 0);
            }else if(stringContains(entry.getKey(), dataKeys)) {
                editor.putString(entry.getKey(), "null");
            }
        }
        editor.apply();
    }

    public static boolean stringContains(String haystack, String[] needles) {
        haystack = haystack.toLowerCase();
        for(String needle : needles) {
            if(haystack.contains(needle.toLowerCase())) return true;
        }
        return false;
    }

    public static void displayRegisterDialog(final Context context, final SharedPreferences prefs) {
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        final AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.register_bracelet))
                .setView(input)
                .setPositiveButton(context.getString(R.string.register), null)
                .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface d) {

                Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        String braceletID = input.getText().toString();
                        if (braceletID.matches("[a-zA-Z0-9]{6}") && Util.notifyIfOffline(context)) {
                            RegisterBracelet registerBr = new RegisterBracelet(braceletID, context, prefs);
                            registerBr.execute();
                            dialog.dismiss();
                        } else
                            Util.alert(context.getString(R.string.wrong_brid_format), context);
                    }
                });
            }
        });
        dialog.show();
    }

    public static void showPopup(Picture picture, SharedPreferences prefs, final Activity context) {
        context.setProgressBarIndeterminateVisibility(true);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_lightbox, null, false);
        final PopupWindow pw = new PopupWindow(popupView, Util.width, (int) (Util.height), true);
        ImageView imgView = (ImageView) popupView.findViewById(R.id.imageView1);
        // Display high res picture if preferred
        String picUrl;
        if (prefs.getBoolean("pref_highdef_pics", false)) {
            picUrl = "http://placelet.de/pictures/bracelets/pic-" + picture.id + "." + picture.fileext;
        } else {
            picUrl = "http://placelet.de/pictures/bracelets/thumb-" + picture.id + ".jpg";
        }
        Picasso.with(context).load(picUrl).into(imgView, new Callback() {
            @Override
            public void onError() {
                context.setProgressBarIndeterminateVisibility(false);
                pw.dismiss();
            }

            @Override
            public void onSuccess() {
                context.setProgressBarIndeterminateVisibility(false);
            }
        });
        imgView.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                pw.dismiss();
            }
        });
        pw.showAtLocation(context.getWindow().getDecorView().getRootView(), Gravity.CENTER, 0, 0);
    }

    private static class RegisterBracelet extends AsyncTask<String, String, Integer> {
        private String brid;
        private Context context;
        private SharedPreferences prefs;

        public RegisterBracelet (String brid, Context context, SharedPreferences prefs) {
            this.brid = brid;
            this.context = context;
            this.prefs = prefs;
        }

        @Override
        protected Integer doInBackground(String... params) {
            User user = new User(prefs);
            return user.registerBracelet(brid);
        }

        @Override
        protected void onPostExecute(Integer result) {
            switch(result) {
                case 0:
                    Util.alert(context.getString(R.string.bracelet_notextisting), context);
                    break;
                case 1:
                    Util.alert(brid + " " + context.getString(R.string.registered_exclamation), context);
                    break;
                case 2:
                    Util.alert(context.getString(R.string.bracelet_registered_to_you), context);
                    break;
                case 3:
                    Util.alert(context.getString(R.string.bracelet_registered_someone), context);
                    break;
                default:
                    Util.alert(context.getString(R.string.server_problem), context);
            }
        }
    }
}
