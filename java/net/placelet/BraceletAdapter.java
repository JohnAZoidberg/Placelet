package net.placelet;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.placelet.data.Bracelet;
import net.placelet.data.Picture;

import java.util.List;

public class BraceletAdapter extends ArrayAdapter<Bracelet> {
    private Context context;
    private List<Bracelet> communityList;

    public BraceletAdapter(Context ctx, int resource, List<Bracelet> objects) {
        super(ctx, resource, objects);
        context = ctx;
        communityList = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View element = convertView;
        ViewHolderItem viewHolder;
        Bracelet bracelet = communityList.get(position);
        bracelet.html_entity_decode();
        // Load different layout if no pictures should be displayed
        if (element == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            element = inflater.inflate(R.layout.own_bracelet_element, null);
            viewHolder = new ViewHolderItem();
            viewHolder.name = (TextView) element.findViewById(R.id.braceletName);
            viewHolder.lastLocation = (TextView) element.findViewById(R.id.braceletLastLocation);
            viewHolder.distance = (TextView) element.findViewById(R.id.braceletDistance);
            viewHolder.thumbnail = (ImageView) element.findViewById(R.id.imageView1);

            element.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolderItem) element.getTag();
        }

        if (position % 2 == 1) {
            element.setBackgroundColor(context.getResources().getColor(R.color.light_grey));
        }else {
            element.setBackgroundColor(Color.WHITE);
        }

        viewHolder.name.setText(bracelet.name);
        Picture lastPic = bracelet.pictures.get(0);
        viewHolder.lastLocation.setText(lastPic.city + ", " + lastPic.country);
        viewHolder.distance.setText(bracelet.getDistance() + " km");
        if (lastPic.loadImage) {
            Util.loadThumbnail(context, viewHolder.thumbnail, lastPic.id);
        }

        return element;
    }

    @Override
    public int getCount() {
        communityList.size();
        return super.getCount();
    }

    static class ViewHolderItem {
        TextView name;
        TextView lastLocation;
        TextView distance;
        ImageView thumbnail;
    }
}