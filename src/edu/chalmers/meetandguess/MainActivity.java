package edu.chalmers.meetandguess;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import edu.chalmers.qdnetworking.NetworkingEventHandler;
import edu.chalmers.qdnetworking.NetworkingManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class MainActivity extends ActionBarActivity implements
		NetworkingEventHandler {

	private static final String GROUP = "G9";
	private static final String GAME_KEY = "game";
	private static final String CURRENT_QUESTION_NUMBER_KEY = "currentQuestion";
	private static final String USER_TO_TOTAL_SCORE_KEY = "userToTotalScoreKey";
	private static final String REQUEST_JOINING_KEY = "newUser";

	private static final String SHARED_PREF = "edu.chalmers.meetandguess.save_app_state";

	private static final int PROFILE_ACTIVITY_REQUEST_CODE = 0;
	private static final int CREATE_GAME_REQUEST_CODE = 1;

	private DrawerLayout drawer;

	private ActionBarDrawerToggle drawerToggle;
	private ListView drawerList;
	private Navigation navigation;

	private NetworkingManager manager;

	private Game game;
	private String userName;
	private static int nextRoundNumberOfPlayers = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initViews();

		// methods to reset the app/server
		// resetApp();
		// ServerReset resetter = new ServerReset();
		// resetter.resetUsers();
		// resetter.resetGames();
		// resetter.resetQuestionList();

		// Access the user name
		SharedPreferences sharedPref = getSharedPreferences(SHARED_PREF,
				MODE_PRIVATE);
		userName = sharedPref.getString("username", null);
		if (userName == null) {
			loadProfileActivity(null);
		} else {
			this.manager = new NetworkingManager(this, GROUP, userName);
		}
	}

	public void initViews() {

		// Toolbar Layout
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

		setSupportActionBar(toolbar);
		// Inflate a menu to be displayed in the toolbar
		toolbar.inflateMenu(R.menu.main);

		// Drawer Layout
		drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		// Drawer Toggle
		drawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar,
				R.string.drawer_open, R.string.drawer_close) {

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
				getSupportActionBar().setTitle("Close");

			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				getSupportActionBar().setTitle("Open");

			}
		};

		// Set the drawer toggle as the DrawerListener
		drawer.setDrawerListener(drawerToggle);

		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		// Status Bar Color
		// drawer.setStatusBarBackgroundColor(R.color.StatusGreen);

		navigation = new Navigation();
		// Set the adapter for the list view
		drawerList = (ListView) findViewById(R.id.left_drawer);
		drawerList.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, navigation.getNavList()));
		// Set the list's click listener
		// drawerList.setOnItemClickListener(new DrawerItemClickListener());
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		drawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
		if (drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		switch (id) {
		case R.id.action_createGame:
			Intent intent = new Intent(this, CreateGameActivity.class);
			this.startActivityForResult(intent, CREATE_GAME_REQUEST_CODE);
			return true;
		case R.id.action_joinGame:
			displayJoinAlertDialog();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void loadQuestionActivity(View view) {
		Intent intent = new Intent(this, QuestionActivity.class);
		if (game != null) {
			intent.putExtra("game", game);
			this.startActivity(intent);
		} else {
			// TODO: Alert that there is no game
		}
	}

	@Override
	public void savedValueForKeyOfUser(JSONObject json, String key, String user) {
		if (key.equals(REQUEST_JOINING_KEY)) { // successfully notified to be a new
										// user
			Intent intent = new Intent(this, QuestionActivity.class);
			intent.putExtra("game", game);
			this.startActivity(intent);
		}
	}

	@Override
	public void loadedValueForKeyOfUser(JSONObject json, String key, String user) {
		if (key.equals(GAME_KEY)) {
			Gson gson = new Gson();
			try {
				game = gson.fromJson(json.getString("value"), Game.class);
				manager.loadValueForKeyOfUser(USER_TO_TOTAL_SCORE_KEY,
						game.getGameId());
			} catch (JsonSyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (key.equals(USER_TO_TOTAL_SCORE_KEY)) {
			Gson gson = new Gson();
			try {
				Map<String, Integer> userToTotalScore = gson.fromJson(
						json.getString("value"),
						new TypeToken<HashMap<String, Integer>>() {
						}.getType());
				if (userToTotalScore == null) {
					userToTotalScore = new HashMap<String, Integer>();
				}
				userToTotalScore.put(user, 0);
				game.setUser2TotalScore(userToTotalScore);
				manager.loadValueForKeyOfUser(CURRENT_QUESTION_NUMBER_KEY,
						game.getGameId());
			} catch (JsonSyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (key.equals(CURRENT_QUESTION_NUMBER_KEY)) {
			Gson gson = new Gson();
			try {
				int currentQuestion = gson.fromJson(json.getString("value"),
						Integer.class);
				game.setCurrentQuestionNumber(currentQuestion);
				manager.saveValueForKeyOfUser(REQUEST_JOINING_KEY, game.getGameId(),
						userName);
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
		if (key.equals(REQUEST_JOINING_KEY)) {
			nextRoundNumberOfPlayers++;
			Intent intent = new Intent(this, QuestionActivity.class);
			intent.putExtra("game", game);
			this.startActivity(intent);
		}
	}

	@Override
	public void lockedKeyofUser(JSONObject json, String key, String user) {
	}

	@Override
	public void unlockedKeyOfUser(JSONObject json, String key, String user) {
	}

	public void loadProfileActivity(View view) {
		Intent intent = new Intent(this, ProfileActivity.class);
		this.startActivityForResult(intent, PROFILE_ACTIVITY_REQUEST_CODE);
	}

	private void displayJoinAlertDialog() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(getResources().getText(R.string.join_game));
		alert.setMessage(getResources().getText(R.string.join_game_description));

		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton(getResources().getText(R.string.ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String gameId = input.getText().toString();
						manager.loadValueForKeyOfUser("game", gameId);
					}
				});

		alert.setNegativeButton(getResources().getText(R.string.cancel), null);
		alert.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case (PROFILE_ACTIVITY_REQUEST_CODE):
			if (manager == null) {
				userName = data.getStringExtra("userName");
				this.manager = new NetworkingManager(this, "G9", userName);
			}
		case (CREATE_GAME_REQUEST_CODE):
			if (game == null) {
				game = (Game) data.getParcelableExtra("game");
				nextRoundNumberOfPlayers++;
				if (game != null) { // wait for another player
					manager.monitorKeyOfUser(REQUEST_JOINING_KEY, game.getGameId());
					AlertDialog.Builder alert = new AlertDialog.Builder(this);
					alert.setTitle(game.getGameId());
					alert.setMessage(getResources().getText(R.string.game_id_information));
					alert.setPositiveButton(getResources().getText(R.string.ok),null);
					alert.show();
				}
			}
		}
	}

	private void resetApp() {
		SharedPreferences sharedPref = getSharedPreferences(
				"edu.chalmers.meetandguess.save_app_state", MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString("username", null);
		editor.commit();
	}
	
	public static int getNextRoundNumberOfPlayers() {
		return nextRoundNumberOfPlayers;
	}
	
	public static void addPlayerForNextRound() {
		nextRoundNumberOfPlayers++;
	}
}
