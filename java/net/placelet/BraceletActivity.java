package net.placelet;

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
import android.view.Window;
import android.widget.Button;

import net.placelet.connection.User;
import net.placelet.connection.Webserver;
import net.placelet.data.Bracelet;
import net.placelet.data.Comment;
import net.placelet.data.Picture;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

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
		setContentView(R.layout.activity_bracelet);

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
        if (intent.hasExtra("fragment")) {
            // switch to specific fragment
            int fragmentNr = intent.getIntExtra("fragment", 0);
            switchFragments(fragmentNr);
        }
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
        if(Util.notifyIfOffline(this)) {
            Subscription sub = new Subscription(this);
            sub.execute();
        }
    }

    public void postComment(int picid, String content, Button sendComment) {
        if(checkCommentContent(content)) {
            Commenting comm = new Commenting(picid, content, sendComment);
            comm.execute(picid);
        }else {
            Util.alert("No links or empty comments!", this);
            sendComment.setEnabled(true);
        }
    }

    private boolean checkCommentContent(String content) {
        // if it contains the blockwords and doesn't contain "placelet" OR is empty it's false
        return !content.isEmpty() && !(!Util.stringContains(content, "placelet") && Util.stringContains(content, new String[]{"http", "www", ".com", ".de", ".net"}));
    }

    private class Commenting extends AsyncTask<Integer, Void, JSONObject> {
        private String content;
        private int picid;
        private Button sendComment;

        public Commenting(int picid, String content, Button sendComment) {
            this.picid = picid;
            this.content = content;
            this.sendComment = sendComment;
        }

        @Override
        protected JSONObject doInBackground(Integer... params) {
            User user = new User(prefs);
            return user.comment(bracelet.brid, picid, content);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            sendComment.setEnabled(true);
            // check if connected to the internet
            try {
                if (result.getString("error").equals("no_internet")) {
                    //toggleLoading(false);
                    return;
                }
            } catch (JSONException ignored) {
            }
            // check if new content
            try {
                String updateString = result.getString("update");
                if(User.admin) Util.alert("Update: " + updateString, BraceletActivity.this);
                toggleLoading(false);
            } catch (JSONException e) {
                Util.saveDate(prefs, "getBraceletDataLastUpdate-" + bracelet.brid, System.currentTimeMillis() / 1000L);
                String jsonString = result.toString();
                Util.saveData(prefs, "braceletData-" + bracelet.brid, jsonString);
                updateBracelet(result);
            }
        }
    }

    private class Subscription extends AsyncTask<String, String, Boolean> {
        private Context context;
        public Subscription (Context context) {
            this.context = context;
        }
        @Override
        protected Boolean doInBackground(String... params) {
            User user = new User(prefs);
            return user.subscribe(bracelet.brid, !bracelet.subscribed);
        }

        @Override
        protected void onPostExecute(Boolean result) {
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
            if(!Webserver.checkResult(result)) {
                toggleLoading(false);
                return;
			}
            // check if new content
            try {
                String updateString = result.getString("update");
                if(User.admin) Util.alert("Update: " + updateString, BraceletActivity.this);
                toggleLoading(false);
            } catch (JSONException e) {
                Util.saveDate(prefs, "getBraceletDataLastUpdate-" + bracelet.brid, System.currentTimeMillis() / 1000L);
                String jsonString = result.toString();
                Util.saveData(prefs, "braceletData-" + bracelet.brid, jsonString);
                updateBracelet(result);
            }
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
        //bracelet = new Bracelet(bracelet.brid);
		try {
			bracelet.owner = result.getString("owner");
			bracelet.name = result.getString("name");
			bracelet.date = result.getLong("date");
			bracelet.picAnz = result.getInt("pic_anz");
			bracelet.lastCity = result.getString("lastcity");
			bracelet.lastCountry = result.getString("lastcountry");
            bracelet.subscribed = result.getBoolean("subscribed");
		} catch (JSONException ignored) {
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

                JSONObject comments;
                int i = 1;
                boolean commentExists = true;
                do {
                    try {
                        comments = pictures.getJSONObject(i + "");
                        Comment comment = new Comment();
                        comment.user = comments.getString("user");
                        comment.user = comment.user.equals("null") ? "Anonym" : comment.user;
                        comment.content = comments.getString("comment");
                        comment.date = comments.getLong("date");
                        picture.comments.add(comment);
                    } catch (JSONException e) {
                        commentExists = false;
                    }
                    i++;
                }while(commentExists);
                Comment comment = new Comment();
                comment.user = "JohnZoidberg";
                comment.content = "Testkommentar";
                comment.date = Integer.MAX_VALUE;
                picture.comments.add(comment);
				bracelet.pictures.add(picture);
			} catch (JSONException ignored) {
			}
		}
        invalidateOptionsMenu();
		bracelet.sort();
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
		} catch (JSONException ignored) {
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

    public void switchFragments(int number) {
        mPager.setCurrentItem(number);
    }
}