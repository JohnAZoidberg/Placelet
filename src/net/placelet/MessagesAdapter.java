package net.placelet;

import java.util.List;

import net.placelet.data.Message;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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
		Message message = messageList.get(position);
		message.html_entity_decode();
		if (element == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			element = inflater.inflate(R.layout.message_element, null);
		}
		if (position % 2 == 1) {
	    element.setBackgroundColor(context.getResources().getColor(R.color.light_grey));  
		}else {
	    element.setBackgroundColor(Color.WHITE); 
		}
		TextView sender = (TextView) element.findViewById(R.id.name);
		TextView messageContent = (TextView) element.findViewById(R.id.message);
		TextView date = (TextView) element.findViewById(R.id.date);
		ImageView imgView = (ImageView) element.findViewById(R.id.status_image);
		if(message.seen != 0) imgView.setImageResource(R.drawable.tick);
		
		String displayMessage = message.message.length() > 20 ? message.message.replaceAll("\n", " ").substring(0, 20).trim()  + "..." : message.message;
		sender.setText(message.sender);
		messageContent.setText(displayMessage);
		date.setText(Util.timestampToTime(message.sent));

		return element;
	}

	@Override
	public int getCount() {
		messageList.size();
		return super.getCount();
	}

}