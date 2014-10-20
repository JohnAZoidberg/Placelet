package net.placelet;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.placelet.data.Picture;

import java.util.List;

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
		ViewHolderItem viewHolder;
		Picture picture = communityList.get(position);
		picture.html_entity_decode();
		// Load different layout if no pictures should be displayed
		if (element == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            element = inflater.inflate(R.layout.community_element, null);
			viewHolder = new ViewHolderItem();
			viewHolder.title = (TextView) element.findViewById(R.id.pic_title);
			viewHolder.location = (TextView) element.findViewById(R.id.pic_location);
			viewHolder.imgView = (ImageView) element.findViewById(R.id.imageView1);
			
			element.setTag(viewHolder);
		}else {
			viewHolder = (ViewHolderItem) element.getTag();
		}
		
		if (position % 2 == 1) {
	    element.setBackgroundColor(context.getResources().getColor(R.color.light_grey));  
		}else {
	    element.setBackgroundColor(Color.WHITE); 
		}
		
		viewHolder.title.setText(picture.title);
		viewHolder.location.setText(picture.city + ", " + picture.country);
		// load image
		Util.loadThumbnail(context, viewHolder.imgView, picture.id);

		return element;
	}

	@Override
	public int getCount() {
		communityList.size();
		return super.getCount();
	}
	
	static class ViewHolderItem {
		TextView title;
		TextView location;
		ImageView imgView;
	}
}