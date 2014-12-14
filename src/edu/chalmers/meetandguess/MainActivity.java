package edu.chalmers.meetandguess;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.google.gson.Gson;

import edu.chalmers.qdnetworking.NetworkingEventHandler;
import edu.chalmers.qdnetworking.NetworkingManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends ActionBarActivity implements NetworkingEventHandler{

	private DrawerLayout drawer;

	private ActionBarDrawerToggle drawerToggle;
	private ListView drawerList;
	private Navigation navigation;

	private NetworkingManager manager;
	
	private Game game;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		InitViews();
		
		// Access the user name
		SharedPreferences sharedPref = getSharedPreferences("edu.chalmers.meetandguess.save_app_state", MODE_PRIVATE);
		String userName = sharedPref.getString("username", null);

		this.manager = new NetworkingManager(this, "G9", userName);
		//setUp();
		
		Intent intent = getIntent();
		game = (Game) intent.getParcelableExtra("game");
	}
	
	public void InitViews(){
		
		// Toolbar Layout
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		  
		setSupportActionBar(toolbar);
		 // Inflate a menu to be displayed in the toolbar
	    toolbar.inflateMenu(R.menu.main);
	    
	    // Drawer Layout
	 	drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
	    // Drawer Toggle
	    drawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar ,  R.string.drawer_open, R.string.drawer_close) { 
	  
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
		//drawer.setStatusBarBackgroundColor(R.color.StatusGreen);
		
		navigation = new Navigation();
		 // Set the adapter for the list view
		drawerList = (ListView) findViewById(R.id.left_drawer);
        drawerList.setAdapter(new ArrayAdapter<String>(this,
        		android.R.layout.simple_list_item_1, navigation.getNavList()));
        // Set the list's click listener
        //drawerList.setOnItemClickListener(new DrawerItemClickListener());
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
		
		switch(id)
		{
			case R.id.action_createGame: 
				Intent intent = new Intent(this, CreateGameActivity.class);
				this.startActivityForResult(intent, 0);
				return true;
			case R.id.action_joinGame:	 return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void loadQuestionActivity(View view) {
		Intent intent = new Intent(this, QuestionActivity.class);
		if(game != null) {
			intent.putExtra("game", game);
			this.startActivityForResult(intent, 0);
		} else {
			// TODO: Alert that there is no game
		}
	}

	@Override
	public void savedValueForKeyOfUser(JSONObject json, String key, String user) {
		if(key.equals("questionList")) {
			Gson gson = new Gson();
			String groupIdJson = gson.toJson("M0");
			manager.saveValueForKeyOfUser("gameId", "gameManager", groupIdJson);
		} else if(key.equals("gameId")) {
			Gson gson = new Gson();
			String questionNumberJson = gson.toJson(0);
			manager.saveValueForKeyOfUser("questionNumber", "gameLogic", questionNumberJson);
		}
	}

	@Override
	public void loadedValueForKeyOfUser(JSONObject json, String key, String user) {
		// TODO Auto-generated method stub
		
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

	public void loadProfileActivity(View view) {
		Intent intent = new Intent(this, ProfileActivity.class);
		this.startActivityForResult(intent, 0);
	}
	
	private void setUp() {
		// TODO this is what should already be on the server: a list of questions
		Question firstQuestion = new Question("Which animal is the cuter one?", "Dog", "Cat");
		Question secondQuestion = new Question("Have you been to Australia?", "Yes", "No");
		Question thirdQuestion = new Question("Have you been to Asia?", "Yes", "No");
		List<Question> questionList = new ArrayList<Question>();
		questionList.add(firstQuestion);
		questionList.add(secondQuestion);
		questionList.add(thirdQuestion);
		Gson gson = new Gson();
		String questionJson = gson.toJson(questionList);
		manager.saveValueForKeyOfUser("questionList", "gameData", questionJson);
	}
	
}
