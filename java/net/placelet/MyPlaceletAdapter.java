package net.placelet;

import java.util.ArrayList;

import net.placelet.data.Bracelet;
import net.placelet.data.Picture;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MyPlaceletAdapter extends ArrayAdapter<Bracelet> {
	private Context context;
	private ArrayList<Bracelet> braceletsList;

	public MyPlaceletAdapter(Context ctx, int resource, ArrayList<Bracelet> objects) {
		super(ctx, resource, objects);
		context = ctx;
		braceletsList = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View element = convertView;
		ViewHolderItem viewHolder;
		Bracelet bracelet = braceletsList.get(position);
		Picture picture = bracelet.pictures.get(0);
		picture.html_entity_decode();
		if (picture.stringData == null) {
			// Load different layout if no pictures should be displayed
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if (picture.loadImage) {
				element = inflater.inflate(R.layout.community_element, null);
			} else {
				element = inflater.inflate(R.layout.community_nopic_element, null);
			}
			viewHolder = new ViewHolderItem();
			viewHolder.title = (TextView) element.findViewById(R.id.pic_title);
			viewHolder.location = (TextView) element.findViewById(R.id.pic_location);
			viewHolder.imgView = (ImageView) element.findViewById(R.id.imageView1);

			element.setTag(viewHolder);

			if ((position - 1) % 2 == 1) {
				element.setBackgroundColor(context.getResources().getColor(R.color.light_grey));
			} else {
				element.setBackgroundColor(Color.WHITE);
			}
			viewHolder.title.setText(picture.title);
			viewHolder.location.setText(picture.city + ", " + picture.country);
			// load image
			if (picture.loadImage) {
				Util.loadThumbnail(context, viewHolder.imgView, picture.id);
			}
		} else {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			element = inflater.inflate(R.layout.listview_header, null);
			TextView headerView = (TextView) element.findViewById(R.id.list_header);
			headerView.setText(picture.stringData);
		}
		return element;
	}

	@Override
	public int getCount() {
		braceletsList.size();
		return super.getCount();
	}

	static class ViewHolderItem {
		TextView title;
		TextView location;
		ImageView imgView;
	}
}