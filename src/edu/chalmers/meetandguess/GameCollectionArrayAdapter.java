package edu.chalmers.meetandguess;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class GameCollectionArrayAdapter extends ArrayAdapter<Game>{

	private Context context;
	private int layout;
	
	public GameCollectionArrayAdapter(Context context, int layout, List<Game> games) {
		super(context, layout, games);
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
		
		Game game = getItem(position);
		if(game != null){
			TextView locationTextView = (TextView) view.findViewById(R.id.listItemLocation);
			locationTextView.setText(game.getLocationDescription());
		}
		
		return view;
		
	}
	
	
}
