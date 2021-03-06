package edu.chalmers.meetandguess;

import java.util.HashMap;
import java.util.LinkedList;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity implements
		NetworkingEventHandler {

	private static final String GROUP = "G9";
	private static final String GAME_MANAGER_USER = "gameManager";
	private static final String ACTIVE_GAMES_KEY = "activeGames";
	private static final String PROFILE_KEY = "profile";
	private static final String GAME_KEY = "game";
	private static final String CURRENT_QUESTION_NUMBER_KEY = "currentQuestion";
	private static final String USER_TO_TOTAL_SCORE_KEY = "userToTotalScoreKey";
	private static final String REQUEST_JOINING_KEY = "newUser";

	private static final String SHARED_PREF = "edu.chalmers.meetandguess.save_app_state";

	private static final int PROFILE_ACTIVITY_REQUEST_CODE = 0;
	private static final int CREATE_GAME_REQUEST_CODE = 1;

	private DrawerLayout drawerLayout;
	private LinearLayout linearDrawerLayout;
	private ActionBarDrawerToggle drawerToggle;
	private ListView drawerList;
	private String[] drawerMenuItems;
	
	
	private LinkedList<Game> gameList = new LinkedList<Game>();
	private BaseExpandableListAdapter adapter;

	private NetworkingManager manager;

	private Game game;
	private String userName;
	private Player userProfile;
	private Bitmap profilePicture;
	private static int nextRoundNumberOfPlayers = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		boolean setUpDemoServer = false;
		if(setUpDemoServer) {
			ServerReset sr = new ServerReset();
			sr.setUpServer(this);
		}
		initViews();
		 
		// Access the user name
		SharedPreferences sharedPref = getSharedPreferences(SHARED_PREF,
				MODE_PRIVATE);
		userName = sharedPref.getString("username", null);
		if (userName == null) {
			Intent intent = new Intent(this, ProfileActivity.class);
			this.startActivityForResult(intent, PROFILE_ACTIVITY_REQUEST_CODE);
		} else {
			this.manager = new NetworkingManager(this, GROUP, this.userName);
			loadGameList();
			manager.loadValueForKeyOfUser(PROFILE_KEY, userName);
		}
	}

	public void initViews() {

		// Toolbar Layout
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

		setSupportActionBar(toolbar);
		// Inflate a menu to be displayed in the toolbar
		toolbar.inflateMenu(R.menu.main);

		// Drawer Layout
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		linearDrawerLayout = (LinearLayout) findViewById(R.id.left_drawer_view);
		// Drawer Toggle
		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
				R.string.drawer_open, R.string.drawer_close) {

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);

			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				if(profilePicture != null) {
					ImageView profilePictureView = (ImageView) drawerView.findViewById(R.id.drawer_header_image);
					profilePictureView.setImageBitmap(profilePicture);
				}
				if(userProfile != null) {
					TextView userNameView = (TextView) drawerView.findViewById(R.id.drawer_user_name);
					userNameView.setText(userProfile.getFirstname());
					TextView scoreView = (TextView) drawerView.findViewById(R.id.drawer_score_display);
					scoreView.setTag(userProfile.getScore());
				}
			}
			
		};

		// Set the drawer toggle as the DrawerListener
		drawerLayout.setDrawerListener(drawerToggle);

		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		// Status Bar Color
		// drawer.setStatusBarBackgroundColor(R.color.StatusGreen);

		drawerMenuItems = getResources().getStringArray(R.array.drawer_items_array);

        // Set the list's click listener
		drawerList = (ListView) findViewById(R.id.left_drawer);
		drawerList.setAdapter(new NavigationDrawerArrayAdapter(this,
				R.layout.navigation_drawer_item, drawerMenuItems));
		drawerList.setOnItemClickListener(new DrawerItemClickListener());
	
		this.adapter = new GameCollectionArrayAdapter(this, R.layout.game_list_item, R.layout.game_list_item_detail, gameList);
		ExpandableListView listView = (ExpandableListView) findViewById(R.id.gameListView);
		listView.setAdapter(adapter);
		listView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
			
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {
				ImageView collapseExpandView = (ImageView) v.findViewById(R.id.collapseExpandImage);
				if(parent.isGroupExpanded(groupPosition)) {
					collapseExpandView.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_expand));
				} else {
					collapseExpandView.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_collapse));
				}
				return false;
			}
		});
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
	
	private void loadGameList() {
		manager.loadValueForKeyOfUser(ACTIVE_GAMES_KEY, GAME_MANAGER_USER);
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
		if(key.equals(ACTIVE_GAMES_KEY)) {
			Gson gson = new Gson();
			try {
				LinkedList<Game> loadedList = gson.fromJson(json.getString("value"),
						new TypeToken<LinkedList<Game>>() {
						}.getType());
				if(loadedList != null) {
					gameList.clear();
					gameList.addAll(loadedList);
					adapter.notifyDataSetChanged();
				} else {
					manager.saveValueForKeyOfUser(ACTIVE_GAMES_KEY, GAME_MANAGER_USER, null);
				}
			} catch (JsonSyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if(key.equalsIgnoreCase(PROFILE_KEY)) {
			Gson gson = new Gson();
			try {
				userProfile = gson.fromJson(json.getString("value"), Player.class);
				if(userProfile.getImage() != null) {
					byte[] bitmapData = Base64.decode(userProfile.getImage(), Base64.DEFAULT);
					Bitmap bm = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
					profilePicture = BitmapDecoder.getCircleBitmap(bm);
					
					TextView scoreView = (TextView) findViewById(R.id.drawer_score_display);
					scoreView.setText(String.valueOf(userProfile.getScore()));
				}
			} catch (JsonSyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (key.equals(GAME_KEY)) {
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
	}

	@Override
	public void valueChangedForKeyOfUser(JSONObject json, String key,
			String user) {
		if (key.equals(REQUEST_JOINING_KEY)) {
			manager.ignoreKeyOfUser(key, user);
			nextRoundNumberOfPlayers++;
			if(nextRoundNumberOfPlayers == 2) {
				Intent intent = new Intent(this, QuestionActivity.class);
				intent.putExtra("game", game);
				this.startActivity(intent);
			}
		}
	}

	@Override
	public void lockedKeyofUser(JSONObject json, String key, String user) {
	}

	@Override
	public void unlockedKeyOfUser(JSONObject json, String key, String user) {
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
				this.manager = new NetworkingManager(this, "G9", this.userName);
			}
		loadGameList();
		manager.loadValueForKeyOfUser(PROFILE_KEY, userName);
		case (CREATE_GAME_REQUEST_CODE):
			if (game == null && data.getParcelableExtra("game") != null) {
				game = (Game) data.getParcelableExtra("game");
				loadGameList();
				manager.monitorKeyOfUser(REQUEST_JOINING_KEY, game.getGameId());
				nextRoundNumberOfPlayers++;
				if (game != null) { // wait for another player
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
	
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
	    @Override
	    public void onItemClick(AdapterView parent, View view, int position, long id) {
	        selectItem(position);
	    }
	}

	private void selectItem(int position) {
	    switch (position) {
		case 0:
			drawerList.setItemChecked(position, true);
			drawerLayout.closeDrawer(linearDrawerLayout);
			Intent intent = new Intent(this, CreateGameActivity.class);
			this.startActivityForResult(intent, CREATE_GAME_REQUEST_CODE);
			break;
		case 1:
			drawerList.setItemChecked(position, true);
			drawerLayout.closeDrawer(linearDrawerLayout);
			displayJoinAlertDialog();
			break;
		case 2:
			drawerList.setItemChecked(position, true);
			drawerLayout.closeDrawer(linearDrawerLayout);
			Intent intent2 = new Intent(this, ProfileActivity.class);
			this.startActivityForResult(intent2, PROFILE_ACTIVITY_REQUEST_CODE);
			break;
		default:
			break;
		}
	}
	
}
