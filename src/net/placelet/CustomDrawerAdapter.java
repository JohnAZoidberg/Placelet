package net.placelet;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomDrawerAdapter extends ArrayAdapter<DrawerItem> {

	Context context;
	List<DrawerItem> drawerItemList;
	int layoutResID;

	public CustomDrawerAdapter(Context context, int layoutResourceID, List<DrawerItem> listItems) {
		super(context, layoutResourceID, listItems);
		this.context = context;
		this.drawerItemList = listItems;
		this.layoutResID = layoutResourceID;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		DrawerItemHolder drawerHolder;
		View view = convertView;

		if (view == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			drawerHolder = new DrawerItemHolder();

			view = inflater.inflate(layoutResID, parent, false);
			drawerHolder.ItemName = (TextView) view.findViewById(R.id.title);
			drawerHolder.icon = (ImageView) view.findViewById(R.id.icon);

			view.setTag(drawerHolder);

		} else {
			drawerHolder = (DrawerItemHolder) view.getTag();

		}

		DrawerItem dItem = (DrawerItem) this.drawerItemList.get(position);
		if (position == 0)
			try {
				drawerHolder.icon.setImageDrawable(context.getPackageManager().getApplicationIcon("net.placelet"));
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		else
			drawerHolder.icon.setImageDrawable(view.getResources().getDrawable(dItem.getImgResID()));
		drawerHolder.ItemName.setText(dItem.getItemName());

		return view;
	}

	private static class DrawerItemHolder {
		TextView ItemName;
		ImageView icon;
	}
}