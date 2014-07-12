package net.placelet;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
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
		if (position % 2 == 1) {
	    element.setBackgroundColor(context.getResources().getColor(R.color.light_grey));  
		}else {
	    element.setBackgroundColor(Color.WHITE); 
		}
		TextView title = (TextView) element.findViewById(R.id.pic_title);
		TextView location = (TextView) element.findViewById(R.id.pic_location);

		title.setText(picture.title);
		location.setText(picture.city + ", " + picture.country);
		// load image
		if (picture.loadImage) {
			ImageView imgView = (ImageView) element.findViewById(R.id.imageView1);
			Util.loadThumbnail(context, imgView, picture.id);
		}

		return element;
	}

	@Override
	public int getCount() {
		communityList.size();
		return super.getCount();
	}
}