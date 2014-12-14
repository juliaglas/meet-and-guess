package edu.chalmers.meetandguess;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import edu.chalmers.qdnetworking.NetworkingEventHandler;
import edu.chalmers.qdnetworking.NetworkingManager;

import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.*;

public class GuessActivity extends ActionBarActivity implements NetworkingEventHandler { 
    
	private static final String TAG_ERROR = "ERROR";
	
	private static final String GROUP = "G9";
	private static final String USER_TO_ANSWER_KEY = "userToAnswer";
	private static final String USER_TO_SCORE = "userToScore";
	private static final String SHARED_PREF = "edu.chalmers.meetandguess.save_app_state";
	private String userName;
	private String imgData;
	private NetworkingManager manager;
	private Game game;
	private Player player;
	private Answer guess;
	private Map<String, Answer> userToAnswer;
	private int id = 1;
	
    private GestureDetectorCompat mDetector; 
    View.OnTouchListener mListener;
    private ImageView mImage;
    private static final int SWIPE_THRESHOLD= 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;
    private Map<Integer, Boolean> guessSlot;  // Slots: <id, empty>
    private Map<String, Answer> playerGuess;   // Player Guess: <User, Answer>
    private Map<Integer, String> idToUser;  // Id to User: <id, User>
    private int viewTouched;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guess);
        
        // Toolbar Layout
 		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
 		setSupportActionBar(toolbar);
 		getSupportActionBar().setTitle("Guess");
     	
 		//mImage = new ImageView(this);
 		mDetector = new GestureDetectorCompat(this, new MyGestureListener());
        mListener = new View.OnTouchListener() { 
            public boolean onTouch(View v, MotionEvent event) {
 
            	viewTouched = v.getId();
                return mDetector.onTouchEvent(event);
            }
        };
      
    	SharedPreferences sharedPref = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
		this.userName = sharedPref.getString("username", null);
		
		this.manager = new NetworkingManager(this, GROUP, this.userName);
		
		Intent intent = getIntent();
		game = (Game) intent.getParcelableExtra("game");
		
		// Load Profile Images 
		if (userName!=null)
			this.manager.lockKeyOfUser(USER_TO_ANSWER_KEY, game.getGameId());
	
    }
    
    // Create empty Slots for an Answer
    private void emptySlots(LinearLayout answer, int id)
    {
    	// Get Image Dimensions (currently 68x68)
    	int dimen = (int)getResources().getDimension(R.dimen.image_dimen);
    	// FrameLayout for each slot to allow overlay
    	FrameLayout slot = new FrameLayout(this);
    	slot.setLayoutParams(new LinearLayout.LayoutParams(0, dimen, 1f));
    	// ImageView empty profile slots
    	ImageView profileSlot = new ImageView(this);
    	profileSlot.setId(id); // 11, 12 ,13, 14 - 21, 22, 23, 24
    	profileSlot.setImageResource(R.drawable.slot_shadow);
    	profileSlot.setLayoutParams(new FrameLayout.LayoutParams(dimen, dimen, Gravity.CENTER_HORIZONTAL));
    	answer.addView(slot);
    	slot.addView(profileSlot);
    	// Empty Slot on creation
    	guessSlot.put(id, false);
  	
    }
    
    // Create Player Icons
    private void playerIcons(LinearLayout players, int id)
    {
    	// Get Image Dimensions (currently 68x68)
    	int dimen = (int)getResources().getDimension(R.dimen.image_dimen);
    	// FrameLayout for each image 
    	FrameLayout slot = new FrameLayout(this);
    	slot.setLayoutParams(new LinearLayout.LayoutParams(0, dimen, 1f));
    	
    	// ImageView for images 
    	ImageView profileSlot = new ImageView(this);
    	
    	Bitmap bm;
    
    	if(imgData!=null)
    	{
    		byte[] bitmapData = Base64.decode(imgData, Base64.DEFAULT);
    		bm = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
    	}
    	else
    	{
    	// Create bitmap from resource TODO: It will fetch images from server
    	    bm = BitmapFactory.decodeResource(this.getResources(),
    		R.drawable.profile); 
    	}
        
    	profileSlot.setId(id); // 1, 2 ,3 ,4
    	profileSlot.setImageBitmap(BitmapDecoder.getCircleBitmap(bm));
    	profileSlot.setOnTouchListener(mListener);
    	profileSlot.setLayoutParams(new FrameLayout.LayoutParams(dimen, dimen, Gravity.CENTER_HORIZONTAL));
    	players.addView(slot);
    	slot.addView(profileSlot);	
    	
    	// Player guesses are "Skip Question" in the beginning
    	playerGuess.put(idToUser.get(id), Answer.SKIPQUESTION);
   
    }
    
    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
    	
        private static final String DEBUG_TAG = "Gestures"; 
        
        @Override
        public boolean onDown(MotionEvent event) { 
            Log.d(DEBUG_TAG,"onDown: " + event.toString()); 
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, 
                float velocityX, float velocityY) {
        
           final float yDistance = event1.getY() - event2.getY();
           // Swipe Up or Down
           if(Math.abs(yDistance) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD)
           {   
        	   guess = playerGuess.get(idToUser.get(viewTouched));
        	   // Swipe Up
        	   if(yDistance > 0 && guess!=Answer.ANSWER1 )
        	   {			   
        		   movePlayer(Answer.ANSWER1);
        	   }
        	   // Swipe Down
        	   else if(yDistance < 0 && guess!=Answer.ANSWER2)
        	   {
        		   movePlayer(Answer.ANSWER2);
        	   }
           }
            
            Log.d(DEBUG_TAG, "onFling: " + event1.toString()+event2.toString());
            return true;
        }
       
    }
    
    // Moves a player Icon to an Empty Slot depending on the choice of the user
    private void movePlayer(Answer choice)
    {
    	// Find The Image that is swiped by the User
	    mImage = new ImageView(GuessActivity.this);
	    mImage = (ImageView)findViewById(viewTouched);
    	// Get Parent of Image
		ViewGroup oldParent = (ViewGroup) mImage.getParent();
		// Get Answer that player image is placed
		Answer guess = playerGuess.get(idToUser.get(viewTouched));
		
		// Remove it from the Viewgroup
        if(oldParent!=null)
     	  oldParent.removeView(mImage);
	
        int emptySlot = findEmptySlot(choice);
 	    ImageView profile = (ImageView) findViewById(emptySlot);
		ViewGroup newParent = (ViewGroup) profile.getParent();
        
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams)profile.getLayoutParams();
        
        mImage.setLayoutParams(layoutParams);
       
        newParent.addView(mImage);
        mImage.setVisibility(View.VISIBLE);
        // Mark Slot full
        guessSlot.put(emptySlot, true);
        // Mark previous Slot as empty 
        if(guess != Answer.SKIPQUESTION)
     	   guessSlot.put(oldParent.getChildAt(0).getId(), false);
        
        // Map player to answer that the user guesses
    	playerGuess.put(idToUser.get(viewTouched), choice);
    
    }
    // Finds empty slot depending on the answer that the user picked
    private int findEmptySlot(Answer ans)
    {
    	Set<Entry<Integer, Boolean>> set = guessSlot.entrySet();
        Iterator<Entry<Integer, Boolean>> i = set.iterator();
        // Find Empty slot and return id depending on the Answer
        while(i.hasNext())
        {
           Map.Entry<Integer, Boolean> me = (Map.Entry<Integer, Boolean>)i.next();
           // Empty Slot
    	   boolean empty = (Boolean) me.getValue();  
           if(ans == Answer.ANSWER1)  
           {   
        	   if(empty == false && (Integer)me.getKey()<20)
        		   return (Integer) me.getKey(); 
           }
           else if(ans == Answer.ANSWER2)
           {
        	   if(empty == false && (Integer)me.getKey()>20)
        		   return (Integer) me.getKey(); 
           }
        }
        
        return 0;
    }
    
    // User Pressed Done button
    public void guessResults(View view)
    {
    	// Compare Answers to User Guesses
    	for (Map.Entry<String, Answer> entry : userToAnswer.entrySet()) 
    	{
    		// Correct Guess
    		if(entry.getValue() == playerGuess.get(entry.getKey()))
    		{
    			// Overlay correct Feedback
    			// Add Score
    		}
    		// Incorrect Guess
    		else
    		{
    			// Get The Id of the player image to move
    			for (Map.Entry<Integer, String> idEntry : idToUser.entrySet())
				{
    				if(idEntry.getValue() == entry.getKey())
    					viewTouched = idEntry.getKey();
				}
    			// Move view to correct spot
    			movePlayer(entry.getValue());
    			// Animate
    			// Overlay incorrect Feedback
    		}
    	
    	}
    	
    	// Move Views to Correct Positions, Feedback and Score counting
    
    }
    // Animating swiping
    private class MyAnimationListener implements AnimationListener{

        @Override
        public void onAnimationEnd(Animation animation) {
            mImage.clearAnimation();
           // LayoutParams lp = new LayoutParams(imageView.getWidth(), imageView.getHeight());
          //  lp.setMargins(50, 100, 0, 0);
         //   imageView.setLayoutParams(lp);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

    }
    
	@Override
	public void savedValueForKeyOfUser(JSONObject json, String key, String user) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadedValueForKeyOfUser(JSONObject json, String key, String user) {
		// Load images of other players and <User, Answer> Map
		if(key.equals(USER_TO_ANSWER_KEY))
		{
			try {
				Gson gson = new Gson();
				userToAnswer = gson.fromJson(json.getString("value"), new TypeToken<Map<String, Answer>>(){}.getType());
				if(userToAnswer == null) 
				{
					userToAnswer = new HashMap<String, Answer>();
					Log.d(TAG_ERROR,"No Answers");
				}
				// Load images of other players and Answers
				else 
				{
					// Create Slots for the Answers 
			        guessSlot = new LinkedHashMap<Integer, Boolean>();
			        // Map for id to image icons to Users
			        idToUser = new HashMap<Integer, String>();
			        // Player Guess 
			        playerGuess = new HashMap<String, Answer>();
			        
					// Load Images (TODO 1: Only from other players
					//  			TODO 2: Fetch them internally if no new players)
					for (Map.Entry<String, Answer> entry : userToAnswer.entrySet())
					{
						manager.loadValueForKeyOfUser("profile", entry.getKey());
						idToUser.put(id, entry.getKey());
					}
					
					manager.unlockKeyOfUser(USER_TO_ANSWER_KEY, game.getGameId());
					
				}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		// Load Player Image
		else if (key.equals("profile")) {
			try {
				Gson gson = new Gson();
				player = gson.fromJson(json.getString("value"), Player.class);
				// TODO Load only other player's images
				if(player != null ) { //player.getUsername()!= this.userName
					imgData = player.getImage();
					LinearLayout players = (LinearLayout) findViewById(R.id.Players);
					playerIcons(players, id);   // 1, 2, ...
					
			        LinearLayout firstAnswer = (LinearLayout) findViewById(R.id.First_Answer_Slot);
			        emptySlots(firstAnswer, id+10);  // 11, 12, ...
			        LinearLayout secondAnswer = (LinearLayout) findViewById(R.id.Second_Answer_Slot); 
		        	emptySlots(secondAnswer, id+20); // 21, 22, ...
		        	// Next id
			        id++;
				}
			
			} catch (JsonSyntaxException e) {
				Log.d(TAG_ERROR, "JsonSyntaxException while parsing: " + json.toString());
			} catch (JSONException e) {
				Log.d(TAG_ERROR, "JsonSyntaxException while parsing: " + json.toString());
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
		// User locks in the beginning to fetch images from server, and after is done guessing
		this.manager.loadValueForKeyOfUser(USER_TO_ANSWER_KEY, game.getGameId());
		
	}

	@Override
	public void unlockedKeyOfUser(JSONObject json, String key, String user) {
		// TODO Auto-generated method stub
		
	}
}
