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
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {
    private ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    private ActionBar actionBar;
    public SharedPreferences prefs;
    public SharedPreferences settingsPrefs;
    static public boolean active = false;
    public String brid;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	// Inflate the menu items for use in the action bar
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.action_bar, menu);
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
	Display display = getWindowManager().getDefaultDisplay();
	Point size = new Point();
	display.getSize(size);
	Util.width = size.x;
	Util.height = size.y;
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
	actionBar.addTab(actionBar.newTab().setIcon(R.drawable.ic_action_mail).setTabListener(this));
	// Set Action-Bar title
	actionBar.setTitle(User.username);

	viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

	    @Override
	    public void onPageSelected(int position) {
		// on changing the page
		// make respected tab selected
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

    private void switchToUpload() {
	if (User.username != User.NOT_LOGGED_IN) {
	    Intent uploadIntent = new Intent(this, UploadActivity.class);
	    startActivity(uploadIntent);
	}
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	case R.id.action_logout:
	    Intent intent = new Intent(this, LoginActivity.class);
	    intent.putExtra("logout", "true");
	    startActivity(intent);
	    break;
	case R.id.action_profile:
	    switchToLoginActivity(true);
	    break;
	case R.id.action_upload:
	    switchToUpload();
	    break;
	case R.id.action_about:
	    Intent aboutIntent;
	    aboutIntent = new Intent(this, AboutActivity.class);
	    startActivity(aboutIntent);
	    break;
	case R.id.action_options:
	    switchToOptions();
	    break;
	default:
	    break;
	}
	return true;
    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
	// on tab selected
	// show respected fragment view
	viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
    }

    @Override
    protected void onStart() {
	Intent intent = getIntent();
	if (intent.hasExtra("fragment")) {
	    int fragmentNr = intent.getIntExtra("fragment", 0);
	    switchFragments(fragmentNr);
	}
	if (intent.hasExtra("MessagePush")) {
	    switchToMessage(intent.getStringExtra("MessagePush"));
	}
	// Toast.makeText(this, User.getUsername() + "\n" + User.getDynPW(),
	// Toast.LENGTH_LONG).show();
	switchToLoginActivity(false);
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

    private void switchToLoginActivity(boolean toProfile) {
	if (User.username.equals(User.NOT_LOGGED_IN)) {
	    Intent intent;
	    intent = new Intent(this, LoginActivity.class);
	    startActivity(intent);
	} else if (toProfile) {
	    Intent intent;
	    intent = new Intent(this, ProfileActivity.class);
	    startActivity(intent);
	}
    }

    public static boolean isActive() {
	return active;
    }

    public void switchFragments(int number) {
	actionBar.setSelectedNavigationItem(number);
    }

    private void switchToOptions() {
	if (User.username != User.NOT_LOGGED_IN) {
	    Intent profileIntent = new Intent(this, OptionsActivity.class);
	    startActivity(profileIntent);
	}
    }

    public void switchToBraceletFragment(Picture pic) {
	brid = pic.brid;
	BraceletFragment fragment = (BraceletFragment) mAdapter.getFragment(1);
	fragment.loadPictures();
	switchFragments(1);
    }
}