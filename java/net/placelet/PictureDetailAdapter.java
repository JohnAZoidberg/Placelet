package net.placelet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import net.placelet.data.Comment;
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
		PictureViewHolderItem viewHolder;
		final Picture picture = communityList.get(position);
		if (element == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// Load different layout if no pictures should be displayed
			element = inflater.inflate(R.layout.picture_detail_element, null);
			viewHolder = new PictureViewHolderItem();
			viewHolder.title = (TextView) element.findViewById(R.id.pic_title);
			viewHolder.description = (TextView) element.findViewById(R.id.pic_description);
			viewHolder.location = (TextView) element.findViewById(R.id.pic_location);
			viewHolder.user = (TextView) element.findViewById(R.id.pic_user);
			viewHolder.date = (TextView) element.findViewById(R.id.pic_date);
			viewHolder.imgView = (ImageView) element.findViewById(R.id.imageView1);
			
			element.setTag(viewHolder);
		}else {
			viewHolder = (PictureViewHolderItem) element.getTag();
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
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View element;
        Comment comment = getChild(groupPosition, childPosition);
            if(!isLastChild) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                element = inflater.inflate(R.layout.comment_element, null);
                TextView commentUser = (TextView) element.findViewById(R.id.username);
                TextView commentView = (TextView) element.findViewById(R.id.comment);
                TextView commentDate = (TextView) element.findViewById(R.id.date);

                commentUser.setText(comment.user);
                commentDate.setText(Util.timestampToDate(comment.date));
                commentView.setText(comment.content);
            }else {
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    element = inflater.inflate(R.layout.post_comment_footer, null);
                    final EditText commentInput = (EditText) element.findViewById(R.id.commentInput);
                    final Button sendComment = (Button) element.findViewById(R.id.send_comment);

                    sendComment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            sendComment.setEnabled(false);
                            ((BraceletActivity) context).postComment((communityList.size() - groupPosition), commentInput.getText().toString(), sendComment);
                        }
                    });
                //element.setFocusableInTouchMode(true);
            }
        return element;
    }

    @Override
    public int getGroupCount() {
        return communityList.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return communityList.get(groupPosition).hashCode();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return communityList.get(groupPosition).comments.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return communityList.get(groupPosition);
    }

    @Override
    public Comment getChild(int groupPosition, int childPosititon) {
        return communityList.get(groupPosition).comments.get(childPosititon);
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
	
	static class PictureViewHolderItem {
        TextView title;
        TextView description;
        TextView location;
        TextView user;
        TextView date;
        ImageView imgView;
    }

}