package net.placelet;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {
	private ViewPager viewPager;
	private TabsPagerAdapter mAdapter;
	private ActionBar actionBar;
	public SharedPreferences prefs;
	public SharedPreferences settingsPrefs;
	static public boolean active = false;
	public String brid;
	public Display display;
	public int currentTabId = 0;
	private static boolean trial = false;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Util.inflateActionBar(this, menu, false);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_main);
		prefs = this.getSharedPreferences("net.placelet", Context.MODE_PRIVATE);
		settingsPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		User.username = prefs.getString("username", User.NOT_LOGGED_IN);
		User.dynPW = prefs.getString("dynPW", User.NOT_LOGGED_IN);
		initializeFragments();
		Util.display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		Util.display.getSize(size);
		Util.width = size.x;
		Util.height = size.y;
		// Switch to LoginActivity on first creation
		if (!trial) {
			trial = true;
			if (!User.getStatus()) {
				NavigateActivities.switchActivity(this, LoginActivity.class, false);
			}
		}
	}

	private void initializeFragments() {
		// Initialization
		viewPager = (ViewPager) findViewById(R.id.pager);
		actionBar = getActionBar();
		mAdapter = new TabsPagerAdapter(getSupportFragmentManager());

		viewPager.setAdapter(mAdapter);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		// Add Tabs
		actionBar.addTab(actionBar.newTab().setText(R.string.community_uc).setTabListener(this));
		// Set Action-Bar title
		if (User.getStatus()) {
			actionBar.setTitle(User.username);
			actionBar.addTab(actionBar.newTab().setIcon(R.drawable.ic_action_mail).setTabListener(this));
		} else {
			actionBar.setTitle(R.string.app_name);
		}

		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				// on changing the page make respected tab selected
				actionBar.setSelectedNavigationItem(position);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Reload content of current fragment
		if (item.getItemId() == R.id.action_reload) {
			Fragment fragment = mAdapter.getFragment(currentTabId);
			switch (currentTabId) {
				case 0:
					((CommunityFragment) fragment).loadPictures(0, true);
					break;
				case 1:
					((MessagesFragment) fragment).loadMessages(true);
					break;
				case 2:
					break;
			}
		}
		return NavigateActivities.activitySwitchMenu(item, this);
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// on tab selected show respected fragment view
		currentTabId = tab.getPosition();
		viewPager.setCurrentItem(currentTabId);
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	protected void onStart() {
		Intent intent = getIntent();
		if (intent.hasExtra("fragment")) {
			// switch to specific fragment
			int fragmentNr = Integer.valueOf(intent.getStringExtra("fragment"));
			switchFragments(fragmentNr);
		}
		if (intent.hasExtra("MessagePush")) {
			// switch to chat with specific user
			switchToMessage(intent.getStringExtra("MessagePush"));
		}
		super.onStart();
		active = true;
	}

	private void switchToMessage(String sender) {
		if (sender.equals(User.NOT_LOGGED_IN)) {
			switchFragments(1);
		} else {
			Intent intent = new Intent(this, IOMessageActivity.class);
			intent.putExtra("recipient", sender);
			startActivity(intent);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		active = false;
	}

	public static boolean isActive() {
		return active;
	}

	public void switchFragments(int number) {
		actionBar.setSelectedNavigationItem(number);
	}
}