package net.placelet;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import uk.co.senab.photoview.PhotoViewAttacher;

public class PictureViewerActivity extends ActionBarActivity implements GestureDetector.OnGestureListener {
    ImageView mImageView;
    PhotoViewAttacher mAttacher;
    SlideOut textView;
    boolean hidden = true;

    int picid = -1;
    String username = "";
    String braceName = "";
    String title = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xff557616));
        setContentView(R.layout.activity_picture_viewer);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE
        );

        // Any implementation of ImageView can be used!
        mImageView = (ImageView) findViewById(R.id.imageView);

        // Set the Drawable displayed
        final Intent intent = getIntent();
        if(intent.hasExtra("username")) {
            username = intent.getStringExtra("username");
        }
        if(intent.hasExtra("braceName")) {
            braceName = intent.getStringExtra("braceName");
        }
        if(intent.hasExtra("title")) {
            title = intent.getStringExtra("title");
        }
        if(intent.hasExtra("picid")) {
            picid = Integer.parseInt(intent.getStringExtra("picid"));
            Picasso.with(PictureViewerActivity.this).load("http://placelet.de/pictures/bracelets/thumb-" + Integer.parseInt(intent.getStringExtra("picid")) + ".jpg").into(mImageView, new Callback() {
                @Override
                public void onSuccess() {
                    // Attach a PhotoViewAttacher, which takes care of all of the zooming functionality.
                    mAttacher = new PhotoViewAttacher(mImageView);
                    mAttacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
                        @Override
                        public void onViewTap(View view, float v, float v2) {
                            toggleHideyBar();
                        }
                    });
                }

                @Override
                public void onError() {

                }
            });
        }
        textView = (SlideOut) findViewById(R.id.textView);
        textView.setText(
                title + "\n"
                        + username + ", " + braceName
        );
        textView.toggle(false, true, false);
        //textView.toggle(true, false, true);
        //textView.toggle(false, false, true);
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

    public void toggleHideyBar() {
        // The UI options currently enabled are represented by a bitfield.
        // getSystemUiVisibility() gives us that bitfield.
        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;
        boolean isImmersiveModeEnabled = ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);

        // Navigation bar hiding:  Backwards compatible to ICS.
        if (Build.VERSION.SDK_INT >= 14) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        // Status bar hiding: Backwards compatible to Jellybean
        if (Build.VERSION.SDK_INT >= 16) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }

        // Immersive mode: Backward compatible to KitKat.
        // Note that this flag doesn't do anything by itself, it only augments the behavior
        // of HIDE_NAVIGATION and FLAG_FULLSCREEN.  For the purposes of this sample
        // all three flags are being toggled together.
        // Note that there are two immersive mode UI flags, one of which is referred to as "sticky".
        // Sticky immersive mode differs in that it makes the navigation and status bars
        // semi-transparent, and the UI flag does not get cleared when the user interacts with
        // the screen.
        if (Build.VERSION.SDK_INT >= 18) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        getWindow().getDecorView().setSystemUiVisibility(newUiOptions);

        textView.toggle(hidden, true, false);
        hidden = !hidden;
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
        if(motionEvent.getEdgeFlags() == MotionEvent.EDGE_TOP) {
            hidden = true;
            toggleHideyBar();
            textView.toggle(hidden, true, false);
        }
        return true;
    }
}