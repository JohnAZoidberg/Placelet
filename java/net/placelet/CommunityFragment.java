package net.placelet;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;

import net.placelet.connection.User;
import net.placelet.connection.Webserver;
import net.placelet.data.Picture;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class CommunityFragment extends Fragment {
	private MainActivity mainActivity;
	private CommunityAdapter adapter;
	private List<Picture> pictureList = new ArrayList<Picture>();
	private ListView list;
	private static final int PIC_COUNT = 5;
    private static final int PIC_START = 10;
	private int displayed_pics = PIC_START;
	private SwipeRefreshLayout swipeLayout;

    private boolean updateDialogDisplayed = false;
    private boolean newsDialogDisplayed = false;

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mainActivity = (MainActivity) getActivity();
		View rootView = inflater.inflate(R.layout.fragment_community, container, false);
		setUpUploadBar(rootView);
		// Initiate ListView
		list = (ListView) rootView.findViewById(R.id.listView1);
		list.setClickable(true);
		list.setOnScrollListener(new EndlessScrollListener());
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Picture pic = (Picture) list.getItemAtPosition(position);
				NavigateActivities.switchActivity(mainActivity, BraceletActivity.class, false, "brid", pic.brid);
			}
		});
        ScrollBox scrollBox = (ScrollBox) rootView.findViewById(R.id.scrollBox);
        scrollBox.attachToListView(list);
		adapter = new CommunityAdapter(mainActivity, 0, pictureList);
		list.setAdapter(adapter);
		swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
		swipeLayout.setColorScheme(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
		swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				loadPictures(true);
			}
		});

		loadPictures(true);
        loadSavedPics();
		return rootView;
	}

	private void setUpUploadBar(View rootView) {
		ImageView cameraIcon = (ImageView) rootView.findViewById(R.id.cameraIcon);
		cameraIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                NavigateActivities.switchActivity(mainActivity, UploadActivity.class, false, "upload", "camera");
            }
        });
        ImageView newBraceletIcon = (ImageView) rootView.findViewById(R.id.newBraceletIcon);
        newBraceletIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Util.displayRegisterDialog(mainActivity, mainActivity.prefs);
            }
        });
		ImageView galleryIcon = (ImageView) rootView.findViewById(R.id.galleryIcon);
		galleryIcon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				NavigateActivities.switchActivity(mainActivity, UploadActivity.class, false, "upload", "gallery");
			}
		});
	}

    public void loadPictures(boolean reload) {
		toggleLoading(true);
        // load new pics from the internet
        if (Util.notifyIfOffline(mainActivity)) {
            Pictures pics = new Pictures(reload);
            pics.execute();
        } else {
            toggleLoading(false);
        }
	}

	private class Pictures extends AsyncTask<String, String, JSONObject> {
        private boolean reload;
        public Pictures(boolean reload) {
            this.reload = reload;
        }

		@Override
		protected JSONObject doInBackground(String... params) {
			JSONObject content;
			User user = new User(mainActivity.prefs);
            int start = 0;
            int count = PIC_START;
            if(displayed_pics > PIC_START && !reload) {
                start = (displayed_pics - PIC_COUNT);
                count = PIC_COUNT;
            }

			content = user.getCommunityPictures(start, count);
			return content;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
            if(!Webserver.checkConnection(result)) {
                toggleLoading(false);
                Util.alert("what the fuck", mainActivity);
                if (!reload) {
                    //displayed_pics--; TODO Check if necessary
                }
                return;
            }
            // check if new content
            try {
                String updateString = result.getString("update");
                showNews(result);
                showUpdateDialog(result);
                if(User.admin) Util.alert("Update: " + updateString, mainActivity);
                toggleLoading(false);
            } catch (JSONException e) {
                if (reload)
                    displayed_pics = PIC_START;
                Util.saveDate(mainActivity.prefs, "getCommunityPicturesLastUpdate", System.currentTimeMillis() / 1000L);
                String jsonString = result.toString();
                if(!reload) jsonString += "†" + mainActivity.prefs.getString("communityPics", "null");
                Util.saveData(mainActivity.prefs, "communityPics", jsonString);
                updateListView(result, reload);
            }
		}
	}

	private int updateListView(JSONObject input, boolean reload) {
        if(reload) pictureList.clear();
        int numberOfPics = 0;
        showNews(input);
        showUpdateDialog(input);
		for (Iterator<?> iter = input.keys(); iter.hasNext();) {
			String key = (String) iter.next();
			try {
				JSONObject pictures = input.getJSONObject(key);
				try {
					Picture picture = new Picture();
					picture.brid = pictures.getString("brid");
					picture.title = pictures.getString("title");
					picture.description = pictures.getString("description");
					picture.city = pictures.getString("city");
					picture.country = pictures.getString("country");
					picture.uploader = pictures.getString("user");
					picture.date = Long.parseLong(pictures.getString("date"));
					picture.id = Integer.parseInt(pictures.getString("id"));
					pictureList.add(picture);
                    numberOfPics++;
				} catch (JSONException ignored) {
				}
			} catch (JSONException ignored) {
            }
		}
        Collections.sort(pictureList);
        adapter.notifyDataSetChanged();
		toggleLoading(false);
        return numberOfPics;
	}

    private void showNews(JSONObject input) {
        if (newsDialogDisplayed) return;
        JSONArray newss = null;
        int snooze = 3600;
        try {
            newss = input.getJSONArray("news");
        } catch (JSONException ignored) {
            return;
        }
        for (int i = 0; i < newss.length(); i++) {
            JSONObject news = null;
            String type = "";
            String content = "";
            try {
                news = (JSONObject) newss.getJSONObject(i);
                type = news.getString("type");
                content = news.getString("content");
            } catch (JSONException ignored) {
                continue;
            }
            try {
                snooze = news.getInt("snooze");
            } catch (JSONException ignored) {
            }
            if (type.equals("updatePrefs")) {
                try {
                    String prefKey = news.getString("prefKey");
                    String prefContent = news.getString("content");
                    Util.saveData(mainActivity.prefs, prefKey, prefContent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                continue;
            }
            if (type.equals("toast")) {// Show Toast
                Util.alert(content, mainActivity);
                continue;
            }
            Long lastNewsNotified = mainActivity.prefs.getLong("newsLater", 0);
            if (type.equals("dialog") && lastNewsNotified + snooze < (System.currentTimeMillis() / 1000L)) {// Show Dialog
                String _action = "URL";
                try {
                    _action = news.getString("action");
                } catch (JSONException ignored) {
                }
                final String action = _action;

                String positiveLabel = "Okay";
                try {
                    positiveLabel = news.getString("positiveLabel");
                } catch (JSONException ignored) {
                }

                String negativeLabel = mainActivity.getString(R.string.cancel);
                try {
                    negativeLabel = news.getString("negativeLabel");
                } catch (JSONException ignored) {
                }

                String title = mainActivity.getString(R.string.message);
                try {
                    title = news.getString("title");
                } catch (JSONException ignored) {
                }

                if (content.equals("")) content = "http://placelet.de";
                final String destination = content;


                final AlertDialog dialog = new AlertDialog.Builder(mainActivity)
                        .setTitle(title)
                        .setPositiveButton(positiveLabel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                if (action.equals("URL")) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse(destination));
                                    startActivity(intent);
                                } else if (action.equals("Activity")) {
                                    try {
                                        Intent intent = new Intent(mainActivity, Class.forName("net.placelet." + destination));
                                        startActivity(intent);
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        })
                        .setNegativeButton(negativeLabel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences.Editor editor = mainActivity.prefs.edit();
                                editor.putLong("newsLater", System.currentTimeMillis() / 1000L);
                                editor.apply();
                                dialog.cancel();
                            }
                        }).create();
                dialog.show();
                continue;
            }
        }
        newsDialogDisplayed = true;
    }

    private void showUpdateDialog(JSONObject input) {
        boolean update = false;
        try {
            update = input.getBoolean("u");
        } catch (JSONException e) {
            return;
        }
        Long lastNotified = mainActivity.prefs.getLong("updateLater", 0);
        if(!updateDialogDisplayed && update && lastNotified + 3600 < (System.currentTimeMillis() / 1000L)) {
            final AlertDialog dialog = new AlertDialog.Builder(mainActivity)
                .setTitle(mainActivity.getString(R.string.please_update))
                .setPositiveButton(mainActivity.getString(R.string.update), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("market://details?id=net.placelet"));
                        startActivity(intent);
                    }
                })
                .setNegativeButton(mainActivity.getString(R.string.remind_later), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = mainActivity.prefs.edit();
                        editor.putLong("updateLater", System.currentTimeMillis() / 1000L);
                        editor.apply();

                        dialog.cancel();
                    }
                }).create();
            dialog.show();
            updateDialogDisplayed = true;
        }
    }

    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	private void loadSavedPics() {
        // display saved pics if it shouldn't reload and if there are pics saved
        String savedPics = mainActivity.prefs.getString("communityPics", "null");
        if (!savedPics.equals("null")) {
            pictureList.clear();
            String[] savedPicArray = savedPics.split("†");
            for(String pics : savedPicArray) {
                if (!pics.equals("null")) {
                    try {
                        JSONObject jObject = new JSONObject(pics);
                        displayed_pics += PIC_COUNT;
                        updateListView(jObject, false);
                    } catch (JSONException ignored) {
                    }
                }
            }
        }
	}

	private void toggleLoading(boolean start) {
		if (start) {
			if (swipeLayout != null)
				swipeLayout.setRefreshing(true);
		} else {
			if (swipeLayout != null)
				swipeLayout.setRefreshing(false);
		}
	}

	public class EndlessScrollListener implements OnScrollListener {
		private int preLast;

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			final int lastItem = firstVisibleItem + visibleItemCount;
			if (lastItem + 2 == totalItemCount) {
				if (preLast != lastItem) { //to avoid multiple calls for last item
                    displayed_pics += PIC_COUNT;
                    loadPictures(false); // load more
					preLast = lastItem;
				}
			}
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
		}
	}

}