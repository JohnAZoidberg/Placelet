package net.placelet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
			if(position == 0) return new BraceletFragment();
			else return new PictureFragment();
		}

		@Override
		public int getCount() {
			return NUM_PAGES;
		}
	}
}