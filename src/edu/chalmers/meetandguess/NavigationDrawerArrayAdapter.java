package edu.chalmers.meetandguess;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NavigationDrawerArrayAdapter extends ArrayAdapter<String> {
	
	private Context context;
	private int layout;
	
	public NavigationDrawerArrayAdapter(Context context, int layout,
			String[] menuItems) {
		super(context, layout, menuItems);
		this.context = context;
		this.layout = layout;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		View view = convertView;
		
		if(view == null){
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(layout, null);
		}
		String menuItem = getItem(position);
		if(menuItem != null){
			TextView menuItemDescription = (TextView) view.findViewById(R.id.menu_item_description);
			menuItemDescription.setText(menuItem);
			ImageView menuItemIcon = (ImageView) view.findViewById(R.id.menu_item_icon);
			Bitmap icon = null;
			switch (position) {
			case 0:
				icon = BitmapFactory.decodeResource(context.getResources(), android.R.drawable.ic_menu_crop);
				break;
			case 1:
				icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_media_play);
				break;
			case 2:
				icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_menu_add);
				break;
			default:
				break;
			}
			menuItemIcon.setImageBitmap(icon);
		}
		
		return view;
	}

}
