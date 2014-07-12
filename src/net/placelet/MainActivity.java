package net.placelet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.R.color;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	CustomDrawerAdapter adapter;

	List<DrawerItem> dataList;

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
		initializeNavDrawer(savedInstanceState);
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

	private void initializeNavDrawer(Bundle savedInstanceState) {
		dataList = new ArrayList<DrawerItem>();
		mTitle = mDrawerTitle = getTitle();
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		//mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		// Add Drawer Item to dataList
		dataList.add(new DrawerItem(getString(R.string.home), 0));
		dataList.add(new DrawerItem(getString(R.string.upload), R.drawable.ic_action_upload));
		dataList.add(new DrawerItem(getString(R.string.options), R.drawable.ic_action_settings));
		dataList.add(new DrawerItem(getString(R.string.about), R.drawable.ic_action_about));
		if (User.getStatus())
			dataList.add(new DrawerItem(getString(R.string.logout_uc), color.transparent));
		else
			dataList.add(new DrawerItem(getString(R.string.login_uc), color.transparent));
		adapter = new CustomDrawerAdapter(this, R.layout.custom_drawer_item, dataList);

		mDrawerList.setAdapter(adapter);
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to
																	// onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu(); // creates call to
																	// onPrepareOptionsMenu()
			}
		};

		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState == null) {
			SelectItem(0);
		}
	}

	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			SelectItem(arg2);
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
		actionBar.addTab(actionBar.newTab().setText(R.string.bracelet).setTabListener(this));
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
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Reload content of current fragment
		if (item.getItemId() == R.id.action_reload) {
			Fragment fragment = mAdapter.getFragment(currentTabId);
			switch (currentTabId) {
				case 0:
					((CommunityFragment) fragment).loadPictures(0, true);
					break;
				case 1:
					((BraceletFragment) fragment).loadPictures(true);
					break;
				case 2:
					((MessagesFragment) fragment).loadMessages(true);
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
			switchFragments(2);
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

	public void switchToBraceletFragment(Picture pic) {
		brid = pic.brid;
		BraceletFragment fragment = (BraceletFragment) mAdapter.getFragment(1);
		// TODO Fehler bei Drehen beheben!
		if (fragment != null)
			fragment.loadPictures(false);
		switchFragments(1);
	}

	public void SelectItem(int position) {
		switch (position) {
			case 1:
				NavigateActivities.switchActivity(this, UploadActivity.class, false);
				break;
			case 2:
				NavigateActivities.switchActivity(this, OptionsActivity.class, false);
				break;
			case 3:
				HashMap<String, String> extras = new HashMap<String, String>();
				if (User.getStatus())
					extras.put("logout", "true");
				NavigateActivities.switchActivity(this, LoginActivity.class, false, extras);
				break;
			case 4:
				NavigateActivities.switchActivity(this, AboutActivity.class, false);
				break;
		}
		mDrawerList.setItemChecked(position, true);
		setTitle(dataList.get(position).getItemName());
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggles
		mDrawerToggle.onConfigurationChanged(newConfig);
	}
}