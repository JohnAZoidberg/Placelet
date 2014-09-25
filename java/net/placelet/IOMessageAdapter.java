package net.placelet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.placelet.data.Message;

import java.util.List;

public class IOMessageAdapter extends ArrayAdapter<Message> {
	private Context context;
	private List<Message> messageList;

	public IOMessageAdapter(Context ctx, int resource, List<Message> objects) {
		super(ctx, resource, objects);
		context = ctx;
		messageList = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View element = convertView;
		ViewHolderItem viewHolder;
		Message message = messageList.get(position);
		message.html_entity_decode();
		if (element == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if (message.loadImage) {
				element = inflater.inflate(R.layout.io_message_element, null);
			} else {
				element = inflater.inflate(R.layout.message_element, null);
			}
			viewHolder = new ViewHolderItem();
			viewHolder.sender = (TextView) element.findViewById(R.id.name);
			viewHolder.messageContent = (TextView) element.findViewById(R.id.message);
			viewHolder.date = (TextView) element.findViewById(R.id.date);
            viewHolder.imgView = (ImageView) element.findViewById(R.id.imageView1);
			
			element.setTag(viewHolder);
		}else {
			viewHolder = (ViewHolderItem) element.getTag();
		}

		viewHolder.sender.setText(message.sender);
		viewHolder.messageContent.setText(message.content);
		viewHolder.date.setText(Util.timestampToTime(message.sent));
		if (message.loadImage) {
            Picasso.with(context).load("http://placelet.de/pictures/profiles/" + message.senderID + ".jpg").into(viewHolder.imgView);
		}

		return element;
	}

	@Override
	public int getCount() {
		messageList.size();
		return super.getCount();
	}
	
	static class ViewHolderItem {
		TextView sender;
		TextView messageContent;
		TextView date;
        ImageView imgView;
	}
}