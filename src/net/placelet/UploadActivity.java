package net.placelet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class UploadActivity extends Activity implements OnClickListener, LocationListener {

	private SharedPreferences prefs;
	private ImageView ivImage;
	private final int SELECT_FILE = 1;
	private final int REQUEST_CAMERA = 0;
	private String imgPath;
	private double latitude = 0.0;
	private double longitude = 0.0;

	private EditText idField;
	private EditText descField;
	private EditText titleField;
	private EditText countryField;
	private EditText cityField;
	private TextView textView;

	protected LocationManager locationManager;
	protected LocationListener locationListener;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Util.inflateActionBar(this, menu, true);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_upload);
		prefs = getSharedPreferences("net.placelet", Context.MODE_PRIVATE);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		// Set Action-Bar title
		if (User.getStatus()) {
			getActionBar().setTitle(User.username);
		} else {
			getActionBar().setTitle(R.string.app_name);
		}
		idField = (EditText) findViewById(R.id.uploadID);
		descField = (EditText) findViewById(R.id.uploadDescription);
		titleField = (EditText) findViewById(R.id.uploadTitle);
		countryField = (EditText) findViewById(R.id.uploadCountry);
		cityField = (EditText) findViewById(R.id.uploadCity);
		ivImage = (ImageView) findViewById(R.id.imageView1);
		textView = (TextView) findViewById(R.id.textView1);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
	}

	@Override
	protected void onStart() {
		Intent intent = getIntent();
		if (intent.hasExtra("logout")) {
			String logout = intent.getStringExtra("logout");
			if (logout.equals("true")) {
				User user = new User(prefs);
				user.logout();
			}
		}
		super.onStart();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.uploadSubmit:
				upload();
				break;
			case R.id.uploadFile:
				selectImage();
				break;
		}
	}

	private void selectImage() {
		final CharSequence[] items = { getString(R.string.take_photo), getString(R.string.choose_from_library), getString(R.string.cancel) };

		AlertDialog.Builder builder = new AlertDialog.Builder(UploadActivity.this);
		builder.setTitle(getString(R.string.add_photo));
		builder.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) {
				if (items[item].equals(getString(R.string.take_photo))) {
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
					intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
					startActivityForResult(intent, REQUEST_CAMERA);
				} else if (items[item].equals(getString(R.string.choose_from_library))) {
					Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					intent.setType("image/*");
					startActivityForResult(Intent.createChooser(intent, getString(R.string.select_file)), SELECT_FILE);
				} else if (items[item].equals(getString(R.string.cancel))) {
					dialog.dismiss();
				}
			}
		});
		builder.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_CAMERA) {
				File f = new File(Environment.getExternalStorageDirectory().toString());
				for (File temp : f.listFiles()) {
					if (temp.getName().equals("temp.jpg")) {
						f = temp;
						break;
					}
				}
				try {
					Bitmap bm;
					BitmapFactory.Options btmapOptions = new BitmapFactory.Options();

					bm = BitmapFactory.decodeFile(f.getAbsolutePath(), btmapOptions);
					ivImage.setImageBitmap(bm);

					String path = getExternalCacheDir().getPath();
					f.delete();
					OutputStream fOut = null;
					File file = new File(path, String.valueOf(System.currentTimeMillis()) + ".jpg");
					try {
						fOut = new FileOutputStream(file);
						bm.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
						fOut.flush();
						fOut.close();
						imgPath = file.getPath();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (requestCode == SELECT_FILE) {
				Uri selectedImageUri = data.getData();

				String tempPath = getPath(selectedImageUri);
				imgPath = tempPath;
				Bitmap bm;
				BitmapFactory.Options btmapOptions = new BitmapFactory.Options();
				bm = BitmapFactory.decodeFile(tempPath, btmapOptions);
				ivImage.setImageBitmap(bm);
			}
		}
	}

	public String getPath(Uri uri) {
		String res = null;
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
		if (cursor.moveToFirst()) {
			;
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			res = cursor.getString(column_index);
		}
		cursor.close();
		return res;
	}

	private class Upload extends AsyncTask<String, String, Integer> {
		Picture picture = new Picture();

		@Override
		protected Integer doInBackground(String... params) {
			picture.brid = idField.getText().toString();
			picture.description = descField.getText().toString();
			picture.title = titleField.getText().toString();
			picture.country = countryField.getText().toString();
			picture.city = cityField.getText().toString();
			picture.latitude = getLatitude();
			picture.longitude = getLongitude();
			User user = new User(prefs);
			return user.uploadPicture(picture, imgPath);
		}

		@Override
		protected void onPostExecute(Integer result) {
			UploadActivity.this.setProgressBarIndeterminateVisibility(false);
			handleUploadError(result, picture.brid);
		}

		@Override
		protected void onPreExecute() {
			UploadActivity.this.setProgressBarIndeterminateVisibility(true);
			super.onPreExecute();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return NavigateActivities.activitySwitchMenu(item, this);
	}

	public void handleUploadError(Integer error, String brid) {
		switch (error) {
		// Country name too short - min 2 chars
			case 0:
				alert(getString(R.string.country_short));
				break;
			// City name too short - min 2 chars
			case 1:
				alert(getString(R.string.description_short));
				break;
			// Wrong format - only JPEG, PNG and GIF permitted
			case 2:
				alert(getString(R.string.wrong_format));
				break;
			// No pic uploaded
			case 3:
				alert(getString(R.string.choose_picture));
				break;
			// Bracelet is not registered
			case 4:
				alert(getString(R.string.not_registered));
				break;
			// Bracelet does not exist
			case 5:
				alert(getString(R.string.bracelet_not_existing));
				break;
			//Picture is to big - max 8 MB
			case 6:
				alert(getString(R.string.too_big));
				break;
			// Success
			case 7:
				switchToBracelet(brid);
				break;
			default:
				alert(error + "");
				break;
		}
	}

	private void upload() {
		if (checkInput()) {
			Upload login = new Upload();
			login.execute();
		}
	}

	private boolean checkInput() {
		Boolean inputsValid = true;
		String errors = "";
		if (imgPath == null) {
			inputsValid = false;
			errors += getString(R.string.choose_picture) + "\n";
		}
		String braceletID = idField.getText().toString().trim();
		if (braceletID.length() != 6/* || !braceletID.matches("[^a-zA-Z0-9]") */) {
			inputsValid = false;
			errors += getString(R.string.incorrect_id) + "\n";
		}
		if (titleField.getText().toString().trim().length() == 0) {
			inputsValid = false;
			errors += getString(R.string.enter_title) + "\n";
		}
		if (cityField.getText().toString().trim().length() == 0) {
			inputsValid = false;
			errors += getString(R.string.city_short) + "\n";
		}
		if (countryField.getText().toString().trim().length() == 0) {
			inputsValid = false;
			errors += getString(R.string.country_short) + "\n";
		}
		if (descField.getText().toString().trim().length() < 2) {
			inputsValid = false;
			errors += getString(R.string.description_short) + "\n";
		}
		if (!errors.equals(""))
			alert(errors);
		return inputsValid;
	}

	private void alert(String content) {
		Toast.makeText(this, content, Toast.LENGTH_LONG).show();
	}

	private void switchToBracelet(String brid) {
		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra("fragment", 1);
		intent.putExtra("brid", brid);
		startActivity(intent);
	}

	@Override
	public void onLocationChanged(Location location) {
		if (latitude != 0 && longitude != 0) {
			textView.setText(getString(R.string.coords_captured));
		} else {
			latitude = location.getLatitude();
			longitude = location.getLongitude();
		}
	}

	@Override
	public void onProviderDisabled(String provider) {

	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	private double getLatitude() {
		return latitude;
	}

	private double getLongitude() {
		return longitude;
	}
}