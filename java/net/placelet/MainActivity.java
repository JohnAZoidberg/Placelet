package net.placelet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import net.placelet.connection.User;
import net.placelet.connection.Webserver;

import java.io.IOException;

public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {
    private ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    private android.support.v7.app.ActionBar actionBar;
    public SharedPreferences prefs;
    public SharedPreferences settingsPrefs;
    static public boolean active = false;
    public String brid;
    public Display display;
    public int currentTabId = 0;
    private static boolean trial = false;
    GoogleCloudMessaging gcm;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Util.inflateActionBar(this, menu, true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = this.getSharedPreferences("net.placelet", Context.MODE_PRIVATE);
        settingsPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        User.username = prefs.getString("username", User.NOT_LOGGED_IN);
        User.dynPW = prefs.getString("dynPW", User.NOT_LOGGED_IN);
        User.admin = prefs.getBoolean("admin", false);
        // Switch to LoginActivity on first creation
        if (!trial) {
            trial = true;
            if (User.username.equals(User.NOT_LOGGED_IN)) {
                NavigateActivities.switchActivity(this, LoginActivity.class, false);
                finish();
            }
        }
        // Register Device
        gcm = GoogleCloudMessaging.getInstance(this);
        String regID = prefs.getString("gcmID", "");
        if (regID.isEmpty()) {
            registerInBackground();
        }
        Util.display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        Util.display.getSize(size);
        Util.width = size.x;
        Util.height = size.y;
    }

    private void initializeFragments() {
        // Initialization
        viewPager = (ViewPager) findViewById(R.id.pager);
        actionBar = getSupportActionBar();
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(mAdapter);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        // Add Tabs
        actionBar.removeAllTabs();
        actionBar.addTab(actionBar.newTab().setIcon(R.drawable.globe).setTabListener(this));
        // Set Action-Bar title
        if (User.getStatus()) {
            actionBar.setTitle(User.username);
            actionBar.addTab(actionBar.newTab().setIcon(R.drawable.ic_action_mail).setTabListener(this));
        } else {
            actionBar.setTitle(R.string.app_name);
        }
        actionBar.addTab(actionBar.newTab().setIcon(R.drawable.ic_action_user).setTabListener(this));

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
                    ((CommunityFragment) fragment).loadPictures(true);
                    break;
                case 1:
                    ((MessagesFragment) fragment).loadMessages(true);
                    break;
                case 2:
                    ((MyPlaceletFragment) fragment).loadBracelets(true);
                    break;
            }
        }
        return NavigateActivities.activitySwitchMenu(item, this);
    }

    @Override
    protected void onStart() {
        initializeFragments();
        Intent intent = getIntent();
        Uri data = intent.getData();
        if(data != null) {
            Util.alert(data.getPath(), this);
            if(data.getPath().equals("/nachrichten")) {
                String recipientParam = data.getQueryParameter("msg");
                if(recipientParam != null) {
                    String recipient = recipientParam;
                    switchToMessage(recipient);
                    Util.alert(recipient, this);
                }else {
                    switchFragments(1);
                }
            }
        }
        if (intent.hasExtra("fragment")) {
            // switch to specific fragment
            int fragmentNr = Integer.valueOf(intent.getStringExtra("fragment"));
            switchFragments(fragmentNr);
        }
        if (intent.hasExtra("MessagePush")) {
            // switch to chat with specific user
            String extra = intent.getStringExtra("MessagePush");
            if(extra.equals(User.NOT_LOGGED_IN)) switchFragments(1);
            else switchToMessage(extra);
        }
        if (intent.hasExtra("PicturePush")) {
            // switch to chat with specific user
            Intent goIntent = new Intent(this, BraceletActivity.class);
            goIntent.putExtra("brid", brid);
            goIntent.putExtra("notification", "");
            startActivity(goIntent);
        }
        super.onStart();
        active = true;
    }

    private void switchToBracelet(String brid) {
        Intent intent = new Intent(this, BraceletActivity.class);
        intent.putExtra("brid", brid);
        startActivity(intent);
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




    private void registerInBackground() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                String regid = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(MainActivity.this);
                    }
                    regid = gcm.register(Webserver.SENDER_ID);
                    Util.saveData(prefs, "gcmID", regid);
                } catch (IOException ignored) {
                }
                return null;
            }
        }.execute(null, null, null);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {
        currentTabId = tab.getPosition();
        viewPager.setCurrentItem(currentTabId);
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {

    }
}