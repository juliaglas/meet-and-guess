package edu.chalmers.meetandguess;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import edu.chalmers.qdnetworking.NetworkingEventHandler;
import edu.chalmers.qdnetworking.NetworkingManager;

public class ScoreActivity extends ActionBarActivity implements NetworkingEventHandler {

	private static final String TAG_ERROR = "ERROR";
	
	private static final String GROUP = "G9";
	private static final String USER_TO_SCORE_KEY = "userToScore";
	private static final String USER_TO_TOTAL_SCORE_KEY = "userToTotalScore";
	private static final String SHARED_PREF = "edu.chalmers.meetandguess.save_app_state";
	private static final String GAME_MANAGER_USER = "gameManager";
	private static final String ACTIVE_GAMES_KEY = "activeGames";
	private String userName;
	private String imgData;
	private NetworkingManager manager;
	private Game game;
	private Player player;
	private Map<String, Integer> userToScore;
	private Map<String, Integer> userToTotalScore;
	private LinkedList<Score> scoreList = new LinkedList<Score>();
	private ScoreArrayAdapter adapter;	
	
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
		if(userName !=null)
			this.manager.loadValueForKeyOfUser(USER_TO_SCORE_KEY, game.getGameId());
		
		this.adapter = new ScoreArrayAdapter(this, R.layout.score_list_item, scoreList);
		ListView listView = (ListView) findViewById(R.id.scoreListView);
		listView.setAdapter(adapter);

    }
	
	@Override
	public void savedValueForKeyOfUser(JSONObject json, String key, String user) {	
		if(key.equals(ACTIVE_GAMES_KEY)) {
			manager.unlockKeyOfUser(key, user);
			// Intent to Main Activity for Owner 
			Intent intent = new Intent(this, MainActivity.class);
			intent.putExtra("game", game);
			this.startActivity(intent);
		}		
		else if(key.equals("profile")){

			// Owner removes game from active game list
			if(game.getOwner().equals(this.userName)) 
			{
				manager.lockKeyOfUser(ACTIVE_GAMES_KEY, GAME_MANAGER_USER);
			}
			else
			{
		      Intent intent = new Intent(this, MainActivity.class);
		      intent.putExtra("game", game);
		      this.startActivity(intent);
			}
		}
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
				Map<String, Integer> userToTotalScore = game.getUser2totalScore();
				for(String userId : userToScore.keySet()) 
				{
					int currentScore = userToScore.get(userId);
				
					game.increaseScoreForUser(userId, currentScore);
					Score score = new Score(game.getPlayerImage(userId), game.getUserName(userId), currentScore, userToTotalScore.get(userId));
					scoreList.add(score);
					adapter.notifyDataSetChanged();
				}
				
				// Load total score map from server
				if(game.getOwner().equals(this.userName)) {
					// Saves total Score Map on the Server
					String userToTotalScoreJson = gson.toJson(game.getUser2totalScore());
					manager.saveValueForKeyOfUser(USER_TO_TOTAL_SCORE_KEY, user, userToTotalScoreJson);
				}
			
			}catch (JsonSyntaxException e) {
				Log.d(TAG_ERROR, "JsonSyntaxException while parsing: " + json.toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(key.equals("profile"))
		{
			try {
				Gson gson = new Gson();
				player = gson.fromJson(json.getString("value"), Player.class);
				// Update players profile score
				if (player != null) {
					player.setScore(player.getScore()+game.getUser2totalScore().get(this.userName));
				}
				String playerJson = gson.toJson(this.player);
				manager.saveValueForKeyOfUser("profile", this.userName, playerJson);
			} catch (JsonSyntaxException e) {
				Log.d(TAG_ERROR,
						"JsonSyntaxException while parsing: " + json.toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		}
		// Owner removes game from active game list
		else if(key.equals(ACTIVE_GAMES_KEY)) {
			Gson gson = new Gson();
			try {
				LinkedList<Game> gameList = gson.fromJson(json.getString("value"),
						new TypeToken<LinkedList<Game>>() {
						}.getType());
				if(gameList == null) {
					gameList = new LinkedList<Game>();
				}
				gameList.remove(game);
				String gameListString = gson.toJson(gameList);
				manager.saveValueForKeyOfUser(key, user, gameListString);
			} catch (JsonSyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
		if(key.equals(ACTIVE_GAMES_KEY))
			manager.loadValueForKeyOfUser(key, user);
	}

	@Override
	public void unlockedKeyOfUser(JSONObject json, String key, String user) {
		// TODO Auto-generated method stub
		
	}
	
	// User Pressed Continue button to proceed to Score Activity
	public void goTonextQuestion(View view) {
		if(game.updateCurrentQuestionNumber())
		{
		   Intent intent = new Intent(this, QuestionActivity.class);
		   intent.putExtra("game", game);
		   this.startActivity(intent);
		   finish();	
		}
		else
		{   
			// Load user profile to update score
			manager.loadValueForKeyOfUser("profile", this.userName);

		}

	}

}
