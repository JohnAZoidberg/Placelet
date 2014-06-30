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
	ImageView imgView = null;
	Message message = messageList.get(position);
	message.html_entity_decode();
	if (element == null) {
	    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    if (message.loadImage) {
		element = inflater.inflate(R.layout.io_message_element, null);
		imgView = (ImageView) element.findViewById(R.id.imageView1);
	    }else {
		element = inflater.inflate(R.layout.message_element, null);
	    }
	}
	TextView sender = (TextView) element.findViewById(R.id.name);
	TextView messageContent = (TextView) element.findViewById(R.id.message);
	TextView date = (TextView) element.findViewById(R.id.date);

	sender.setText(message.sender);
	messageContent.setText(message.message);
	date.setText(Util.timestampToTime(message.sent));
	if(message.loadImage && imgView != null) {
	    Picasso.with(context)
		    .load("http://placelet.de/pictures/profiles/" + message.senderID + ".jpg")
		    .into(imgView);
	}

	return element;// super.getView(position, convertView, parent);
    }

    @Override
    public int getCount() {
	messageList.size();
	return super.getCount();
    }

}