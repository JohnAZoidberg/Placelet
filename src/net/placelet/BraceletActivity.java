package net.placelet;

import java.util.Collections;
import java.util.Iterator;

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
import android.view.Menu;
import android.view.MenuItem;

public class BraceletActivity extends FragmentActivity {
	private ViewPager mPager;
	private PagerAdapter mPagerAdapter;
	private static final int NUM_PAGES = 2;

	public SharedPreferences prefs;
	public SharedPreferences settingsPrefs;

	private BraceletFragment braceletFragment;
	private PictureFragment pictureFragment;

	public Bracelet bracelet = new Bracelet();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
		bracelet.brid = intent.getStringExtra("brid");

		loadPictures(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Util.inflateActionBar(this, menu, true);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return NavigateActivities.activitySwitchMenu(item, this);
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
					setProgressBarIndeterminateVisibility(false);
					return;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			String jsonString = result.toString();
			Util.saveData(prefs, "braceletPics-" + bracelet.brid, jsonString);

			bracelet.pictures.clear();
			try {
				bracelet.owner = result.getString("owner");
				bracelet.name = result.getString("name");
				bracelet.date = result.getLong("date");
				bracelet.picAnz = result.getInt("pic_anz");
				bracelet.lastCity = result.getString("lastcity");
				bracelet.lastCountry = result.getString("lastcountry");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			for (Iterator<?> iter = result.keys(); iter.hasNext();) {
				String key = (String) iter.next();
				try {
					JSONObject pictures = result.getJSONObject(key);
					Picture picture = new Picture();
					// picture.brid = pictures.getString("brid");
					picture.title = pictures.getString("title");
					picture.description = pictures.getString("description");
					picture.city = pictures.getString("city");
					picture.country = pictures.getString("country");
					picture.uploader = pictures.getString("user");
					picture.date = pictures.getLong("date");
					picture.id = pictures.getInt("id");
					picture.fileext = pictures.getString("fileext");
					picture.loadImage = settingsPrefs.getBoolean("pref_download_pics", true);
					bracelet.pictures.add(picture);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			Collections.sort(bracelet.pictures);
			pictureFragment.updateListView();
			braceletFragment.updateData();
		}
	}

	public void loadPictures(boolean reload) {
		if (bracelet.brid == null)
			bracelet.brid = "588888";
		setProgressBarIndeterminateVisibility(true);
		// display saved pics if it shouldn't reload and if there are pics saved
		String savedPics = prefs.getString("braceletPics-" + bracelet.brid, "null");
		if (!savedPics.equals("null") && !reload) {
			pictureFragment.loadSavedPics(savedPics);
		}
		// load new pics from the internet
		if (Util.notifyIfOffline(this)) {
			BraceletData pics = new BraceletData();
			pics.execute();
		} else {
			setProgressBarIndeterminateVisibility(false);
		}
	}
}