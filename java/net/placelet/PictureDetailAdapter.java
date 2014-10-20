package net.placelet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.placelet.data.Picture;

import java.util.List;

public class PictureDetailAdapter extends ArrayAdapter<Picture> {
	private Context context;
	private List<Picture> communityList;

	public PictureDetailAdapter(Context ctx, int resource, List<Picture> objects) {
		super(ctx, resource, objects);
		context = ctx;
		communityList = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View element = convertView;
		ViewHolderItem viewHolder;
		Picture picture = communityList.get(position);
		if (element == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// Load different layout if no pictures should be displayed
			element = inflater.inflate(R.layout.picture_detail_element, null);
			viewHolder = new ViewHolderItem();
			viewHolder.title = (TextView) element.findViewById(R.id.pic_title);
			viewHolder.description = (TextView) element.findViewById(R.id.pic_description);
			viewHolder.location = (TextView) element.findViewById(R.id.pic_location);
			viewHolder.user = (TextView) element.findViewById(R.id.pic_user);
			viewHolder.date = (TextView) element.findViewById(R.id.pic_date);
			viewHolder.imgView = (ImageView) element.findViewById(R.id.imageView1);
			
			element.setTag(viewHolder);
		}else {
			viewHolder = (ViewHolderItem) element.getTag();
		}

		viewHolder.title.setText(picture.title);
		viewHolder.description.setText(picture.description);
		viewHolder.location.setText(picture.city + ", " + picture.country);
		if(picture.uploader.equals("null")) {
			viewHolder.user.setText("");
		}else {
			viewHolder.user.setText(picture.uploader);
		}
		viewHolder.date.setText(Util.timestampToDate(picture.date));
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
		TextView description;
		TextView location;
		TextView user;
		TextView date;
		ImageView imgView;
	}

}