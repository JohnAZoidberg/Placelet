package net.placelet;

import java.util.HashMap;
import java.util.Iterator;

import net.placelet.connection.User;
import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;

public class NavigateActivities {
	public static boolean activitySwitchMenu(MenuItem item, Context context) {
		switch (item.getItemId()) {
			case R.id.action_logout:
				HashMap<String, String> extras = new HashMap<String, String>();
				if(User.getStatus()) {
					extras.put("logout", "true");
				}
				switchActivity(context, LoginActivity.class, false, extras);
				break;
			case android.R.id.home:
				switchActivity(context, MainActivity.class, false);
				return true;
			case R.id.action_options:
				switchActivity(context, OptionsActivity.class, false);
				break;
			case R.id.action_about:
				switchActivity(context, AboutActivity.class, false);
				break;
		}
		return true;
	}

	public static void switchActivity(Context context, Class<?> cls, boolean onlyLogin) {
		if ((onlyLogin && User.getStatus()) || !onlyLogin) {
			Intent intent = new Intent(context, cls);
			context.startActivity(intent);
		}
	}

	public static void switchActivity(Context context, Class<?> cls, boolean onlyLogin, HashMap<String, String> extras) {
		if ((onlyLogin && User.getStatus()) || !onlyLogin) {
			Intent intent = new Intent(context, cls);

			Iterator<String> iter = extras.keySet().iterator();
			while (iter.hasNext()) {
				String key = (String) iter.next();
				String val = (String) extras.get(key);
				intent.putExtra(key, val);
			}

			context.startActivity(intent);
		}
	}

	public static void switchActivity(Context context, Class<?> cls, boolean onlyLogin, String key, String val) {
		if ((onlyLogin && User.getStatus()) || !onlyLogin) {
			Intent intent = new Intent(context, cls);
			intent.putExtra(key, val);
			context.startActivity(intent);
		}
	}
}