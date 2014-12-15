package edu.chalmers.meetandguess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import edu.chalmers.qdnetworking.NetworkingEventHandler;
import edu.chalmers.qdnetworking.NetworkingManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class CreateGameActivity extends ActionBarActivity implements
		NetworkingEventHandler {
	
	private static final String GROUP = "G9";
	private static final String GAME_MANAGER_USER = "gameManager";
	private static final String GAME_DATA_USER = "gameData";
	private static final String GAME_ID_KEY = "gameId";
	private static final String QUESTION_LIST_KEY = "questionList";
	private static final String GAME_MAP_KEY = "gameMap";
	private static final String USER_TO_ANSWER_KEY = "userToAnswer";
	private static final String USER_TO_SCORE_KEY = "userToScore";
	private static final String SHARED_PREF = "edu.chalmers.meetandguess.save_app_state";

	private String userName;
	private NetworkingManager manager;

	private String gameId;
	private String locationDescription;
	private String detailDescription;
	private List<Question> questionList;

	private Game game;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_game);

		// Toolbar Layout
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setTitle(
				getResources().getText(R.string.create_game));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Access the user name
		SharedPreferences sharedPref = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
		userName = sharedPref.getString("username", null);
		manager = new NetworkingManager(this, GROUP, userName);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void createGame(View view) {
		EditText locationEdit = (EditText) findViewById(R.id.location_edit);
		locationDescription = locationEdit.getText().toString();
		if (locationDescription != null) {
			EditText detailEdit = (EditText) findViewById(R.id.detail_edit);
			detailDescription = detailEdit.getText().toString();
			manager.loadValueForKeyOfUser(GAME_ID_KEY, GAME_MANAGER_USER);
		} else {
			// TODO Alert: you need to provide a location
		}
	}

	@Override
	public void savedValueForKeyOfUser(JSONObject json, String key, String user) {
		if (key.equals(GAME_ID_KEY)) { // after increasing gameId on the server
			manager.loadValueForKeyOfUser(QUESTION_LIST_KEY, GAME_DATA_USER);
		} else if (key.equals(QUESTION_LIST_KEY)) {
			game = new Game(gameId, locationDescription, detailDescription,
					questionList, userName);
			game.addUser(userName);
			manager.loadValueForKeyOfUser(GAME_MAP_KEY, GAME_MANAGER_USER);
		} else if (key.equals(GAME_MAP_KEY)) {
			manager.saveValueForKeyOfUser(USER_TO_ANSWER_KEY, game.getGameId(), null);
			manager.saveValueForKeyOfUser(USER_TO_SCORE_KEY, game.getGameId(), null);
		} else if(key.equals(USER_TO_ANSWER_KEY)) {
			Intent intent = NavUtils.getParentActivityIntent(this);
			intent.putExtra("game", game);
			NavUtils.navigateUpTo(this, intent);
		}
	}

	@Override
	public void loadedValueForKeyOfUser(JSONObject json, String key, String user) {
		if (key.equals(GAME_ID_KEY)) { // get the id of the new game
			Gson gson = new Gson();
			try {
				gameId = gson.fromJson(json.getString("value"), String.class);
				String copyOfGameId = new String(gameId);
				int nextGameId = Integer.parseInt(copyOfGameId.replaceAll(
						"[^\\d.]", ""));
				nextGameId++;
				String nextGameIdString = "M" + nextGameId;
				manager.saveValueForKeyOfUser(key, user, nextGameIdString);
			} catch (JsonSyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (key.equals(QUESTION_LIST_KEY)) { 
			Gson gson = new Gson();
			try {
				// obtain complete questionList
				List<Question> completeQuestionList = gson.fromJson(
						json.getString("value"),
						new TypeToken<ArrayList<Question>>() {
						}.getType());
				// set questionList for current game
				if (completeQuestionList.size() < 15) {
					questionList = completeQuestionList;
				} else {
					Random r = new Random();
					int firstQuestion = r
							.nextInt(completeQuestionList.size() - 15 + 1);
					questionList = completeQuestionList.subList(firstQuestion,
							firstQuestion + 15);
				}
				// save new questionList on the server
				String questionListString = gson.toJson(questionList);
				manager.saveValueForKeyOfUser(key, gameId, questionListString);
			} catch (JsonSyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (key.equals(GAME_MAP_KEY)) {
			Gson gson = new Gson();
			try {
				Map<String, Game> gameMap = gson.fromJson(
						json.getString("value"),
						new TypeToken<HashMap<String, Game>>() {
						}.getType());
				if (gameMap == null) {
					gameMap = new HashMap<String, Game>();
				}
				gameMap.put(gameId, game);
				String gameMapString = gson.toJson(gameMap);
				manager.saveValueForKeyOfUser(key, user, gameMapString);
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
		// TODO Auto-generated method stub

	}

	@Override
	public void unlockedKeyOfUser(JSONObject json, String key, String user) {
		// TODO Auto-generated method stub

	}
}
