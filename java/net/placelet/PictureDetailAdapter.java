package net.placelet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.placelet.data.Picture;

import java.util.List;

public class PictureDetailAdapter extends BaseExpandableListAdapter {
	private Context context;
	private List<Picture> communityList;

	public PictureDetailAdapter(Context ctx, int resource, List<Picture> objects) {
		//super(ctx, resource, objects);
		context = ctx;
		communityList = objects;
	}

	@Override
	public View getGroupView(int position, boolean isExpanded, View convertView, ViewGroup parent) {
		View element = convertView;
		ViewHolderItem viewHolder;
		final Picture picture = communityList.get(position);
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
        viewHolder.imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BraceletActivity braceletActivity = (BraceletActivity) context;
                Util.showPopup(picture, braceletActivity.settingsPrefs, braceletActivity);
            }
        });

		return element;
	}

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View element = convertView;
        ViewHolderItem viewHolder;
        String childText = (String) getChild(groupPosition, childPosition);

        if (element == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            element = inflater.inflate(R.layout.listview_header, null);
        }

        TextView txtListChild = (TextView) element.findViewById(R.id.list_header);

        txtListChild.setText(childText);
        return element;
    }

    @Override
    public int getGroupCount() {
        return communityList.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        //return this._listDataChild.get(this._listDataHeader.get(groupPosition)).size();
        return 5;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return communityList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        //return this._listDataChild.get(this._listDataHeader.get(groupPosition)).get(childPosititon);
        return groupPosition + " - " + childPosititon;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
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