package net.placelet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import net.placelet.connection.User;
import net.placelet.connection.Webserver;
import net.placelet.data.Picture;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class CommunityFragment extends Fragment {
	private MainActivity mainActivity;
	private CommunityAdapter adapter;
	private List<Picture> pictureList = new ArrayList<Picture>();
	private ListView list;
	private final int PIC_COUNT = 5;
	private int picnr = 10;
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
				//mainActivity.switchToBraceletFragment(pic);
				NavigateActivities.switchActivity(mainActivity, BraceletActivity.class, false, "brid", pic.brid);
			}
		});
		adapter = new CommunityAdapter(mainActivity, 0, pictureList);
		list.setAdapter(adapter);
		swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
		swipeLayout.setColorScheme(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
		swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				loadPictures(0, true);
			}
		});

		loadPictures(0, false);
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
                displayRegisterDialog();
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

    private void displayRegisterDialog() {
        final EditText input = new EditText(mainActivity);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        final AlertDialog dialog = new AlertDialog.Builder(mainActivity)
                .setTitle(mainActivity.getString(R.string.register_bracelet))
                .setView(input)
                .setPositiveButton(mainActivity.getString(R.string.register), null)
                .setNegativeButton(mainActivity.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface d) {

                Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        String braceletID = input.getText().toString();
                        if (braceletID.matches("[a-zA-Z0-9]{6}") && Util.notifyIfOffline(mainActivity)) {
                            RegisterBracelet registerBr = new RegisterBracelet(braceletID);
                            registerBr.execute();
                            dialog.dismiss();
                        } else
                            Util.alert(mainActivity.getString(R.string.wrong_brid_format), mainActivity);
                    }
                });
            }
        });
        dialog.show();
    }

    private class RegisterBracelet extends AsyncTask<String, String, Integer> {
        private String brid;

        public RegisterBracelet (String brid) {
            this.brid = brid;
        }

        @Override
        protected Integer doInBackground(String... params) {
            User user = new User(mainActivity.prefs);
            return user.registerBracelet(brid);
        }

        @Override
        protected void onPostExecute(Integer result) {
            switch(result) {
                case 0:
                    Util.alert(mainActivity.getString(R.string.bracelet_notextisting), mainActivity);
                    break;
                case 1:
                    Util.alert(brid + mainActivity.getString(R.string.registered_exclamation), mainActivity);
                    break;
                case 2:
                    Util.alert(mainActivity.getString(R.string.bracelet_registered_to_you), mainActivity);
                    break;
                case 3:
                    Util.alert(mainActivity.getString(R.string.bracelet_registered_someone), mainActivity);
                    break;
                default:
                    Util.alert(mainActivity.getString(R.string.server_problem), mainActivity);
            }
        }
    }

    public void loadPictures(int start, boolean reload) {
		toggleLoading(true);
		// display saved pics if it shouldn't reload and if there are pics saved
		String savedPics = mainActivity.prefs.getString("communityPics", "null");
		if (!savedPics.equals("null") && !reload) {
			loadSavedPics(savedPics);
		} else {
			start = picnr;
		}
		// load new pics from the internet
		if (Util.notifyIfOffline(mainActivity)) {
			Pictures pics = new Pictures(start);
			pics.execute();
		} else {
			toggleLoading(false);
		}
	}

	private class Pictures extends AsyncTask<String, String, JSONObject> {
		public int start = 0;

        public Pictures(int start) {
            this.start = start;
        }

		@Override
		protected JSONObject doInBackground(String... params) {
			JSONObject content;
			User user = new User(mainActivity.prefs);
			content = user.getCommunityPictures(picnr);
			return content;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
            if(Webserver.checkConnection(result)) {
                toggleLoading(false);
                picnr = PIC_COUNT;
            }
			updateListView(result, start);
			String jsonString = result.toString();
			Util.saveData(mainActivity.prefs, "communityPics", jsonString);
		}
	}

	private void loadMore(boolean reload) {
		if (!reload)
			picnr += PIC_COUNT;
		loadPictures(picnr, false);
	}

	private void updateListView(JSONObject input, int start) {
		pictureList.clear();
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
					picture.loadImage = mainActivity.settingsPrefs.getBoolean("pref_download_pics", true);
					pictureList.add(picture);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} catch (JSONException e) {
            }
		}
		Collections.sort(pictureList);
		adapter.notifyDataSetChanged();
		toggleLoading(false);
	}

    private void showNews(JSONObject input) {
        JSONObject news = null;
        String type = "";
        String content = "";
        int snooze = 3600;
        try {
            news = input.getJSONObject("news");
            type = news.getString("type");
            content = news.getString("content");
        } catch (JSONException e1) {
            return;
        }
        try {
            snooze = news.getInt("snooze");
        } catch (JSONException ignored) {
        }
        Long lastNewsNotified = mainActivity.prefs.getLong("newsLater", 0);
        if(!newsDialogDisplayed && lastNewsNotified + snooze < (System.currentTimeMillis() / 1000L)) {
            if(type.equals("toast")) {// Show Toast
                Util.alert(content, mainActivity);
            }else if(type.equals("dialog")) {// Show Dialog
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

                if(content.equals("")) content = "http://placelet.de";
                final String destination = content;


                final AlertDialog dialog = new AlertDialog.Builder(mainActivity)
                    .setTitle(title)
                    .setPositiveButton(positiveLabel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            if(action.equals("URL")) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(destination));
                                startActivity(intent);
                            }else if(action.equals("Activity")){
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
                            editor.commit();
                            dialog.cancel();
                        }
                    }).create();
                dialog.show();
            }
        }
        newsDialogDisplayed = true;
    }

    private void showUpdateDialog(JSONObject input) {
        boolean update = false;
        try {
            update = input.getBoolean("u");
        } catch (JSONException e1) {
            return;
        }
        Long lastNotified = mainActivity.prefs.getLong("updateLater", 0);
        if(!updateDialogDisplayed && update && lastNotified + 3600 < (System.currentTimeMillis() / 1000L)) {
            Util.alert("please update nigga", mainActivity);
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
                        editor.commit();
                        dialog.cancel();
                    }
                }).create();
            dialog.show();
        }
        updateDialogDisplayed = true;
    }

    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	private void loadSavedPics(String result) {
		JSONObject jArray = null;
		try {
			jArray = new JSONObject(result);
		} catch (JSONException e) {
			Log.e("log_tag", "Error parsing data " + e.toString());
		}
		if (jArray != null)
			updateListView(jArray, 0);
	}

	private void toggleLoading(boolean start) {
		if (start) {
			//mainActivity.setProgressBarIndeterminateVisibility(true);
			if (swipeLayout != null)
				swipeLayout.setRefreshing(true);
		} else {
			//mainActivity.setProgressBarIndeterminateVisibility(false);
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
					loadMore(false);
					preLast = lastItem;
				}
			}
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
		}
	}

}