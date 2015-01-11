package edu.chalmers.meetandguess;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GameCollectionArrayAdapter extends BaseExpandableListAdapter {

	private Context context;
	private int layout;
	private int childLayout;
	private List<Game> games;
	
	public GameCollectionArrayAdapter(Context context, int layout, int childLayout, List<Game> games) {
		super();
		this.context = context;
		this.layout = layout;
		this.childLayout = childLayout;
		this.games = games;
	}

	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return games.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public Object getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		return games.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return games.get(groupPosition).getDetailedDescription();
	}

	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return groupPosition;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		View view = convertView;
		
		if(view == null){
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(layout, null);
		}
		
		Game game = (Game) getGroup(groupPosition);
		if(game != null) {
			ImageView ownerImage = (ImageView) view.findViewById(R.id.ownerImage);
			if(game.getOwnerImage() != null) {
				byte[] bitmapData = Base64.decode(game.getOwnerImage(), Base64.DEFAULT);
				Bitmap bm = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
				ownerImage.setImageBitmap(BitmapDecoder.getCircleBitmap(bm));
			}
			TextView locationTextView = (TextView) view.findViewById(R.id.listItemLocation);
			locationTextView.setText(game.getLocationDescription());
			TextView ownerTextView = (TextView) view.findViewById(R.id.listItemOwner);
			ownerTextView.setText("Game owner: " + game.getOwnerName());
		}
		
		return view;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		View view = convertView;
		
		if(view == null){
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(childLayout, null);
		}
		
		Game game = (Game) getGroup(groupPosition);
		if(game != null) {
			TextView locationDetailsView = (TextView) view.findViewById(R.id.location_details);
			locationDetailsView.setText(game.getDetailedDescription());
		}
		
		return view;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
