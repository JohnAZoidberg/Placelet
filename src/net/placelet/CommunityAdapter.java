package net.placelet;

import java.util.List;

import com.squareup.picasso.Picasso;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CommunityAdapter extends ArrayAdapter<Picture> {
	private Context context;
	private List<Picture> communityList;

	public CommunityAdapter(Context ctx, int resource, List<Picture> objects) {
		super(ctx, resource, objects);
		context = ctx;
		communityList = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View element = convertView;
		Picture picture = communityList.get(position);
		picture.html_entity_decode();
		// Load different layout if no pictures should be displayed
		if (element == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if (picture.loadImage) {
				element = inflater.inflate(R.layout.community_element, null);
			} else {
				element = inflater.inflate(R.layout.community_nopic_element, null);
			}
		}
		TextView title = (TextView) element.findViewById(R.id.pic_title);
		TextView description = (TextView) element.findViewById(R.id.pic_description);
		TextView location = (TextView) element.findViewById(R.id.pic_location);
		TextView user = (TextView) element.findViewById(R.id.pic_user);
		TextView date = (TextView) element.findViewById(R.id.pic_date);

		title.setText(picture.title);
		description.setText(picture.description);
		location.setText(picture.city + ", " + picture.country);
		user.setText(picture.uploader);
		date.setText(Util.timestampToDate(picture.date));
		// load image
		if (picture.loadImage) {
			ImageView imgView = (ImageView) element.findViewById(R.id.imageView1);
			int orientation = Util.display.getRotation();
			int picWidth = (int) (Util.width * 0.3);
			int picHeight = (int) (Util.height * 0.15);
			// different width and height ratio if in landscape orientation
			if (orientation == 1 || orientation == 3) {
				picWidth = (int) (Util.width * 0.15);
				picHeight = (int) (Util.height * 0.3);
			}
			Picasso picasso = Picasso.with(context);
			// picasso.setIndicatorsEnabled(true);
			picasso.load("http://placelet.de/pictures/bracelets/thumb-" + picture.id + ".jpg").resize(picWidth, picHeight).into(imgView);
		}

		return element;
	}

	@Override
	public int getCount() {
		communityList.size();
		return super.getCount();
	}
}