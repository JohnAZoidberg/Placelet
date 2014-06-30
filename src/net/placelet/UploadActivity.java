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
import android.provider.MediaStore.MediaColumns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class UploadActivity extends Activity implements OnClickListener,
	LocationListener {

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
	if (!User.username.equals(User.NOT_LOGGED_IN)) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.action_bar, menu);
	}
	return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	setContentView(R.layout.activity_upload);
	// getActionBar().setDisplayHomeAsUpEnabled(true);
	prefs = this.getSharedPreferences("net.placelet", Context.MODE_PRIVATE);
	getActionBar().setDisplayHomeAsUpEnabled(true);
	getActionBar().setTitle(User.username);
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
	final CharSequence[] items = {getString(R.string.take_photo), getString(R.string.choose_from_library),
		getString(R.string.cancel) };

	AlertDialog.Builder builder = new AlertDialog.Builder(
		UploadActivity.this);
	builder.setTitle(getString(R.string.select_picture));
	builder.setItems(items, new DialogInterface.OnClickListener() {
	    @Override
	    public void onClick(DialogInterface dialog, int item) {
		if (items[item].equals(getString(R.string.take_photo))) {
		    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		    File f = new File(android.os.Environment
			    .getExternalStorageDirectory(), "temp.jpg");
		    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
		    startActivityForResult(intent, REQUEST_CAMERA);
		} else if (items[item].equals(R.string.choose_from_library)) {
		    Intent intent = new Intent(
			    Intent.ACTION_PICK,
			    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		    intent.setType("image/*");
		    startActivityForResult(
			    Intent.createChooser(intent, "Select File"),
			    SELECT_FILE);
		} else if (items[item].equals("Cancel")) {
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
		File f = new File(Environment.getExternalStorageDirectory()
			.toString());
		for (File temp : f.listFiles()) {
		    if (temp.getName().equals("temp.jpg")) {
			f = temp;
			break;
		    }
		}
		try {
		    Bitmap bm;
		    BitmapFactory.Options btmapOptions = new BitmapFactory.Options();

		    bm = BitmapFactory.decodeFile(f.getAbsolutePath(),
			    btmapOptions);

		    // bm = Bitmap.createScaledBitmap(bm, 70, 70, true);
		    //ivImage.setImageBitmap(bm);

		    String path = android.os.Environment
			    .getExternalStorageDirectory()
			    + File.separator
			    + "Phoenix" + File.separator + "default";
		    f.delete();
		    OutputStream fOut = null;
		    File file = new File(path, String.valueOf(System
			    .currentTimeMillis()) + ".jpg");
		    try {
			fOut = new FileOutputStream(file);
			bm.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
			fOut.flush();
			fOut.close();
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

		String tempPath = getPath(selectedImageUri, UploadActivity.this);
		Bitmap bm;
		BitmapFactory.Options btmapOptions = new BitmapFactory.Options();
		bm = BitmapFactory.decodeFile(tempPath, btmapOptions);
		imgPath = tempPath;
		Toast.makeText(this, imgPath, Toast.LENGTH_LONG).show();
		//ivImage.setImageBitmap(bm);
	    }
	}
    }

    public String getPath(Uri uri, Activity activity) {
	String[] projection = { MediaColumns.DATA };
	Cursor cursor = activity
		.managedQuery(uri, projection, null, null, null);
	int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
	cursor.moveToFirst();
	return cursor.getString(column_index);
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
	    switch (result) {
	    case 7:
		switchToBracelet(picture.brid);
		break;
	    default:
		alert("" + result);
	    }
	}

	@Override
	protected void onPreExecute() {
	    UploadActivity.this.setProgressBarIndeterminateVisibility(true);
	    super.onPreExecute();
	}
    }

    private void switchToProfile() {
	if (User.username != User.NOT_LOGGED_IN) {
	    Intent profileIntent = new Intent(this, ProfileActivity.class);
	    startActivity(profileIntent);
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
	    switchToProfile();
	    break;
	case android.R.id.home:
	    switchToMainActivity();
	    return true;
	case R.id.action_upload:
	    switchToUpload();
	    break;
	case R.id.action_options:
	    switchToOptions();
	    break;
	case R.id.action_about:
	    Intent aboutIntent;
	    aboutIntent = new Intent(this, AboutActivity.class);
	    startActivity(aboutIntent);
	    break;

	default:
	    return super.onOptionsItemSelected(item);
	}
	return true;
    }

    private void switchToMainActivity() {
	Intent mainIntent = new Intent(this, MainActivity.class);
	startActivity(mainIntent);
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
	if (braceletID.length() != 6/* || !braceletID.matches("[^a-zA-Z0-9]")*/) {
	    inputsValid = false;
	    errors += getString(R.string.incorrect_id) + "\n";
	}
	if (titleField.getText().toString().trim().length() == 0) {
	    inputsValid = false;
	    errors += getString(R.string.enter_title) + "\n";
	}
	if (cityField.getText().toString().trim().length() == 0) {
	    inputsValid = false;
	    errors += getString(R.string.enter_city) + "\n";
	}
	if (countryField.getText().toString().trim().length() == 0) {
	    inputsValid = false;
	    errors += getString(R.string.enter_country) + "\n";
	}
	if (descField.getText().toString().trim().length() < 2) {
	    inputsValid = false;
	    errors += getString(R.string.enter_desc) + "\n";
	}
	if(!errors.equals("")) alert(errors);
	return inputsValid;
    }

    private void switchToUpload() {
	if (User.username != User.NOT_LOGGED_IN) {
	    Intent uploadIntent = new Intent(this, UploadActivity.class);
	    startActivity(uploadIntent);
	}
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

    private void switchToOptions() {
	if (User.username != User.NOT_LOGGED_IN) {
	    Intent profileIntent = new Intent(this, OptionsActivity.class);
	    startActivity(profileIntent);
	}
    }

    @Override
    public void onLocationChanged(Location location) {
	if(latitude != 0 && longitude != 0) {
	    textView.setText(getString(R.string.coords_captured));
	}else {
		latitude = location.getLatitude();
		longitude = location.getLongitude();
	}
	//alert("Latitude:" + latitude + ", Longitude:" + longitude);
	
    }

    @Override
    public void onProviderDisabled(String provider) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void onProviderEnabled(String provider) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
	// TODO Auto-generated method stub
	
    }
    
    private double getLatitude() {
	return latitude;
    }
    
    private double getLongitude() {
	return longitude;
    }
}