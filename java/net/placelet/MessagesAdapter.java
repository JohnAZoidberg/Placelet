package net.placelet;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.placelet.connection.User;
import net.placelet.data.Message;

import java.util.List;

public class MessagesAdapter extends ArrayAdapter<Message> {
	private Context context;
	private List<Message> messageList;

	public MessagesAdapter(Context ctx, int resource, List<Message> objects) {
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
			element = inflater.inflate(R.layout.message_element, null);
            viewHolder = new ViewHolderItem();
            viewHolder.sender = (TextView) element.findViewById(R.id.name);
            viewHolder.messageContent = (TextView) element.findViewById(R.id.message);
            viewHolder.date = (TextView) element.findViewById(R.id.date);
            viewHolder.imgView = (ImageView) element.findViewById(R.id.status_image);

            element.setTag(viewHolder);
		}else {
            viewHolder = (ViewHolderItem) element.getTag();
        }
		if (position % 2 == 1) {
	        element.setBackgroundColor(context.getResources().getColor(R.color.light_grey));
		}else {
	        element.setBackgroundColor(Color.WHITE);
		}

        if(message.seen > 0 && !message.recipient.equals(User.username)) viewHolder.imgView.setImageResource(R.drawable.tick);
		String displayMessage = message.content.length() > 20 ? message.content.replaceAll("\n", " ").substring(0, 20).trim()  + "..." : message.content;
		viewHolder.sender.setText(message.sender);
		viewHolder.messageContent.setText(displayMessage);
		viewHolder.date.setText(Util.timestampToTime(message.sent));
        viewHolder.imgView.setImageDrawable(null);

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