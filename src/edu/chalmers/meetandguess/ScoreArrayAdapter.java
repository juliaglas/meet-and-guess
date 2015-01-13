package edu.chalmers.meetandguess;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ScoreArrayAdapter extends BaseAdapter {

	private Context context;
	private int layout;
	private List<Score> scores;
	
	public ScoreArrayAdapter(Context context, int layout, List<Score> scores) {
		super();
		this.context = context;
		this.layout = layout;
		this.scores = scores;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return scores.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return scores.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view = convertView;
		
		if(view == null){
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(layout, null);
		}
		
		Score score = (Score) getItem(position);
		if(score != null) {
			ImageView playerImage = (ImageView) view.findViewById(R.id.playerImage);
			if(score.getPlayerImage() != null) {
				byte[] bitmapData = Base64.decode(score.getPlayerImage(), Base64.DEFAULT);
				Bitmap bm = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
				playerImage.setImageBitmap(BitmapDecoder.getCircleBitmap(bm));
			}
			TextView userNameTextView = (TextView) view.findViewById(R.id.UserName);
			userNameTextView.setText(score.getUserName());
			TextView currentScoreTextView = (TextView) view.findViewById(R.id.CurrentScore);
			currentScoreTextView.setText(score.getCurrentScore());
			TextView totalScoreTextView = (TextView) view.findViewById(R.id.TotalScore);
			totalScoreTextView.setText(score.getTotalScore());
			
		}
		
		return view;
	}
	
}
