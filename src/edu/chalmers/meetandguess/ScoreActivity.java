package edu.chalmers.meetandguess;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import edu.chalmers.qdnetworking.NetworkingEventHandler;
import edu.chalmers.qdnetworking.NetworkingManager;

public class ScoreActivity extends ActionBarActivity implements NetworkingEventHandler {

	private static final String TAG_ERROR = "ERROR";
	
	private static final String GROUP = "G9";
	private static final String USER_TO_SCORE_KEY = "userToScore";
	private static final String USER_TO_TOTAL_SCORE_KEY = "userToTotalScore";
	private static final String SHARED_PREF = "edu.chalmers.meetandguess.save_app_state";
	private String userName;
	private String imgData;
	private NetworkingManager manager;
	private Game game;
	private Player player;
	private Map<String, Integer> userToScore;
	private Map<String, Integer> userToTotalScore;
	private ListView scoreList;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.score);
        
        // Toolbar Layout
 		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
 		setSupportActionBar(toolbar);
 		getSupportActionBar().setTitle("Score");
     	
 		SharedPreferences sharedPref = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
		this.userName = sharedPref.getString("username", null);
		
		this.manager = new NetworkingManager(this, GROUP, this.userName);
		
		Intent intent = getIntent();
		game = (Game) intent.getParcelableExtra("game");
		
		// Load Current Score
		if(userName !=null)
			this.manager.loadValueForKeyOfUser(USER_TO_SCORE_KEY, game.getGameId());

		
    }
	
	@Override
	public void savedValueForKeyOfUser(JSONObject json, String key, String user) {
		
	}

	@Override
	public void loadedValueForKeyOfUser(JSONObject json, String key, String user) {
		// Loaded User Current Score. 
		if(key.equals(USER_TO_SCORE_KEY))
		{
			try {
				Gson gson = new Gson();
				// Load current score map from server
				userToScore = gson.fromJson(json.getString("value"), new TypeToken<Map<String, Integer>>(){}.getType());
				if(userToScore == null) 
				{
					userToScore = new HashMap<String, Integer>();
					Log.d(TAG_ERROR,"No Score");
				}
				// Load total score map from server
				this.manager.loadValueForKeyOfUser(USER_TO_TOTAL_SCORE_KEY, game.getGameId());
				
			
			}catch (JsonSyntaxException e) {
				Log.d(TAG_ERROR, "JsonSyntaxException while parsing: " + json.toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// Loaded Total Score
		else if(key.equals(USER_TO_TOTAL_SCORE_KEY)) 
		{
			try {
				Gson gson = new Gson();
				// Load total score map from server
				userToTotalScore = gson.fromJson(json.getString("value"), new TypeToken<Map<String, Integer>>(){}.getType());
				// If Total Score is empty, fill it with current score values
				if(userToTotalScore == null) 
				{
					userToTotalScore = new HashMap<String, Integer>(userToScore);
					// Add users to game total score map and update it
					for(Map.Entry<String, Integer> entry : userToTotalScore.entrySet())
					{
				      game.addUser(entry.getKey());
					  game.increaseScoreForUser(userName, entry.getValue());
					}
				}
				// Else update it
				else
				{
					// Adds new users retrieved from current score Map
					for(Map.Entry<String, Integer> entry : userToScore.entrySet())
					{
						if(!game.getUser2totalScore().containsKey(entry.getKey()))
							game.addUser(entry.getKey());					
					
						// Updates Score
						game.increaseScoreForUser(entry.getKey(), entry.getValue());
					}
				}
				
				// If user is the owner, saves total Score in the Server
				if(userName == game.getOwner())
				{
					// Saves total Score Map on the Server
					String userToTotalScoreJson = gson.toJson(game.getUser2totalScore());
					manager.saveValueForKeyOfUser(USER_TO_TOTAL_SCORE_KEY, user, userToTotalScoreJson);
				}
				
				scoreList = (ListView) findViewById(R.id.score_list);
		        scoreList.setAdapter(new ArrayAdapter<Integer>(this,
		        		android.R.layout.simple_list_item_1,userToScore.get(userName) ));
			
			}catch (JsonSyntaxException e) {
				Log.d(TAG_ERROR, "JsonSyntaxException while parsing: " + json.toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}

	@Override
	public void deletedKeyOfUser(JSONObject json, String key, String user) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void monitoringKeyOfUser(JSONObject json, String key, String user) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ignoringKeyOfUser(JSONObject json, String key, String user) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void valueChangedForKeyOfUser(JSONObject json, String key,
			String user) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void lockedKeyofUser(JSONObject json, String key, String user) {
	
	}

	@Override
	public void unlockedKeyOfUser(JSONObject json, String key, String user) {
		// TODO Auto-generated method stub
		
	}

}
