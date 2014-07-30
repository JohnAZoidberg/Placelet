package net.placelet;

import java.util.Collections;
import java.util.Iterator;

import net.placelet.connection.User;
import net.placelet.data.Bracelet;
import net.placelet.data.Picture;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

public class BraceletActivity extends FragmentActivity {
	private ViewPager mPager;
	private PagerAdapter mPagerAdapter;
	private static final int NUM_PAGES = 2;

    private static final int SUBSCRIBE_BUTTON = 1;

	public SharedPreferences prefs;
	public SharedPreferences settingsPrefs;

	private BraceletFragment braceletFragment;
	private PictureFragment pictureFragment;

	public Bracelet bracelet;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.bracelet_activity);

		// Instantiate a ViewPager and a PagerAdapter.
		mPager = (ViewPager) findViewById(R.id.pager);
		mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
		mPager.setAdapter(mPagerAdapter);
		// initiate ActionBar
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setTitle(R.string.app_name);

		prefs = this.getSharedPreferences("net.placelet", Context.MODE_PRIVATE);
		settingsPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		Intent intent = getIntent();
		String brid = intent.getStringExtra("brid");
		bracelet = new Bracelet(brid);
		loadPictures(false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Util.inflateActionBar(this, menu, true);
        menu.add(Menu.NONE, SUBSCRIBE_BUTTON, 0, getString(R.string.subscribe)).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        if(bracelet != null) {
            MenuItem bedMenuItem = menu.findItem(SUBSCRIBE_BUTTON);
            if (bracelet.subscribed) {
                bedMenuItem.setTitle(getString(R.string.unsubscribe));
            } else {
                bedMenuItem.setTitle(getString(R.string.subscribe));
            }
        }
        invalidateOptionsMenu();
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Reload
		switch (item.getItemId()) {
            case R.id.action_reload:
			    loadPictures(true);
                break;
            case SUBSCRIBE_BUTTON:
                toggleSubscribe();
                break;
		}
		return NavigateActivities.activitySwitchMenu(item, this);
	}

    private void toggleSubscribe() {
        Subscription sub = new Subscription(this);
        sub.execute();
    }

    private class Subscription extends AsyncTask<String, String, Boolean> {
        private Context context;
        public Subscription (Context context) {
            this.context = context;
        }
        @Override
        protected Boolean doInBackground(String... params) {
            User user = new User(prefs);
            return bracelet.subscribed ? user.unsubscribe(bracelet.brid) : user.subscribe(bracelet.brid);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            User user = new User(prefs);
            if(result) {
                if (bracelet.subscribed) {
                    bracelet.subscribed = false;
                    Util.alert(getString(R.string.unsubscribed), context);
                    invalidateOptionsMenu();
                } else {
                    bracelet.subscribed = true;
                    Util.alert(getString(R.string.subscribed), context);
                    invalidateOptionsMenu();
                }
            }
        }
    }

    @Override
	public void onBackPressed() {
		if (mPager.getCurrentItem() == 0) {
			// If the user is currently looking at the first step, allow the system to handle the
			// Back button. This calls finish() on this activity and pops the back stack.
			super.onBackPressed();
		} else {
			// Otherwise, select the previous step.
			mPager.setCurrentItem(mPager.getCurrentItem() - 1);
		}
	}

	private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
		public ScreenSlidePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			if (position == 0) {
				braceletFragment = new BraceletFragment();
				return braceletFragment;
			} else {
				pictureFragment = new PictureFragment();
				return pictureFragment;
			}
		}

		@Override
		public int getCount() {
			return NUM_PAGES;
		}
	}

	private class BraceletData extends AsyncTask<String, String, JSONObject> {
		@Override
		protected JSONObject doInBackground(String... params) {
			User user = new User(prefs);
			JSONObject content = user.getBraceletData(bracelet.brid);
			return content;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			// check if connected to the internet
			try {
				if (result.getString("error").equals("no_internet")) {
					//toggleLoading(false);
					return;
				}
			} catch (JSONException e) {
			}
			String jsonString = result.toString();
			Util.saveData(prefs, "braceletData-" + bracelet.brid, jsonString);
			updateBracelet(result);
		}
	}

	public void loadPictures(boolean reload) {
		toggleLoading(true);
		// display saved pics if it shouldn't reload and if there are pics saved
		String savedBracelet = prefs.getString("braceletData-" + bracelet.brid, "null");
		if (!savedBracelet.equals("null") && !reload) {
			loadSavedBracelet(savedBracelet);
		}
		// load new pics from the internet
		if (Util.notifyIfOffline(this)) {
			BraceletData pics = new BraceletData();
			pics.execute();
		}else {
			toggleLoading(false);
		}
	}

	public void updateBracelet(JSONObject result) {
		bracelet.pictures.clear();
		try {
			bracelet.owner = result.getString("owner");
			bracelet.name = result.getString("name");
			bracelet.date = result.getLong("date");
			bracelet.picAnz = result.getInt("pic_anz");
			bracelet.lastCity = result.getString("lastcity");
			bracelet.lastCountry = result.getString("lastcountry");
            bracelet.subscribed = result.getBoolean("subscribed");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		for (Iterator<?> iter = result.keys(); iter.hasNext();) {
			String key = (String) iter.next();
			try {
				JSONObject pictures = result.getJSONObject(key);
				Picture picture = new Picture();
				picture.title = pictures.getString("title");
				picture.description = pictures.getString("description");
				picture.city = pictures.getString("city");
				picture.country = pictures.getString("country");
				picture.uploader = pictures.getString("user");
				picture.date = pictures.getLong("date");
				picture.id = pictures.getInt("id");
				picture.fileext = pictures.getString("fileext");
				picture.latitude = pictures.getDouble("latitude");
				picture.longitude = pictures.getDouble("longitude");
				picture.loadImage = settingsPrefs.getBoolean("pref_download_pics", true);
				bracelet.pictures.add(picture);
			} catch (JSONException e) {
			}
		}
        invalidateOptionsMenu();
		Collections.sort(bracelet.pictures);
		bracelet.html_entity_decode();
		if(pictureFragment != null) {
			pictureFragment.updateData();
		}
		if(braceletFragment != null) {
			braceletFragment.updateData();
		}
		toggleLoading(false);
	}

	public void loadSavedBracelet(String result) {
		JSONObject jArray = null;
		try {
			jArray = new JSONObject(result);
			updateBracelet(jArray);
		} catch (JSONException e) {
			Log.e("log_tag", "Error parsing data " + e.toString());
		}
	}

	private void toggleLoading(boolean start) {
		if (start) {
			//setProgressBarIndeterminateVisibility(true);
			if(pictureFragment!= null && pictureFragment.swipeLayout != null) pictureFragment.swipeLayout.setRefreshing(true);
		} else {
			//setProgressBarIndeterminateVisibility(false);
			if(pictureFragment!= null && pictureFragment.swipeLayout != null) pictureFragment.swipeLayout.setRefreshing(false);
		}
	}

    public void switchFragment(int number) {
        mPager.setCurrentItem(number);
    }
}