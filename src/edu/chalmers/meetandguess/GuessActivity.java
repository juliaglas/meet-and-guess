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
import android.app.ProgressDialog;
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
import android.view.animation.TranslateAnimation;
import android.widget.*;

public class GuessActivity extends ActionBarActivity implements
		NetworkingEventHandler {

	private static final String TAG_ERROR = "ERROR";
	private static final String GROUP = "G9";
	private static final String USER_TO_ANSWER_KEY = "userToAnswer";
	private static final String USER_TO_SCORE_KEY = "userToScore";
	private static final String ANSWERING_DONE_KEY = "answeringDone";
	private static final String SHARED_PREF = "edu.chalmers.meetandguess.save_app_state";
	private int numberOfPlayers;
	private int numberOfFinishedPlayers;
	private String userName;
	private String imgData;
	private NetworkingManager manager;
	private Game game;
	private Player player;
	private Answer guess;
	private Map<String, Answer> userToAnswer;
	private Map<String, Integer> userToScore;
	private int id = 1;
	private int score = 0;

	private GestureDetectorCompat mDetector;
	View.OnTouchListener mListener;
	private ImageView mImage;
	private static final int SWIPE_THRESHOLD = 100;
	private static final int SWIPE_VELOCITY_THRESHOLD = 100;
	private Map<Integer, Boolean> guessSlot; // Slots: <id, empty>
	private Map<String, Answer> playerGuess; // Player Guess: <User, Answer>
	private Map<Integer, String> idToUser; // Id to User: <id, User>
	private int viewTouched;
	private boolean finished = false;
	private boolean goToScoreSelected = false;

	ProgressDialog progress;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.guess);

		// Toolbar Layout
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setTitle("Guess");

		// mImage = new ImageView(this);
		mDetector = new GestureDetectorCompat(this, new MyGestureListener());
		mListener = new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {

				if(!finished)
				{
					viewTouched = v.getId();
					return mDetector.onTouchEvent(event);
				}
				
				return false;
			}
		};

		SharedPreferences sharedPref = getSharedPreferences(SHARED_PREF,
				MODE_PRIVATE);
		this.userName = sharedPref.getString("username", null);

		this.manager = new NetworkingManager(this, GROUP, this.userName);

		progress = new ProgressDialog(this);
		
		Intent intent = getIntent();
		game = (Game) intent.getParcelableExtra("game");

		numberOfPlayers = MainActivity.getNextRoundNumberOfPlayers();
		numberOfFinishedPlayers = 0;
		
		// Load Profile Images
		if (userName != null)
		{
			progress.setTitle("Loading");
			progress.setMessage("Wait while player icons...");
			progress.show();
			// User fetches images from server
			this.manager.loadValueForKeyOfUser(USER_TO_ANSWER_KEY,
					game.getGameId());
		}
		
		// Load Question and Answers
		if(game!=null)
			displayQuestion();
		
		// Owner monitors User to Score Key
		if(userName.equals(game.getOwner())) {
			manager.monitorKeyOfUser(USER_TO_SCORE_KEY, game.getGameId());
		} else {
			manager.monitorKeyOfUser(ANSWERING_DONE_KEY, game.getGameId());
		}

	}
	
	public void displayQuestion() {
		Question currentQuestion = game.getCurrentQuestion();
		TextView question = (TextView) findViewById(R.id.guess_label);
		question.setText(currentQuestion.getQuestion());
		TextView firstAnswer = (TextView) findViewById(R.id.Answer1);
		firstAnswer.setText(currentQuestion.getAnswer1());
		TextView secondAnswer = (TextView) findViewById(R.id.Answer2);
		secondAnswer.setText(currentQuestion.getAnswer2());		
	}

	// Create empty Slots for an Answer
	private void emptySlots(LinearLayout answer, int id) {
		// Get Image Dimensions (currently 68x68)
		int dimen = (int) getResources().getDimension(R.dimen.image_dimen);
		// FrameLayout for each slot to allow overlay
		FrameLayout slot = new FrameLayout(this);
		slot.setLayoutParams(new LinearLayout.LayoutParams(0, dimen, 1f));
		// ImageView empty profile slots
		ImageView profileSlot = new ImageView(this);
		profileSlot.setId(id); // 11, 12 ,13, 14 - 21, 22, 23, 24
		profileSlot.setImageResource(R.drawable.slot_shadow);
		profileSlot.setLayoutParams(new FrameLayout.LayoutParams(dimen, dimen,
				Gravity.CENTER_HORIZONTAL));
		answer.addView(slot);
		slot.addView(profileSlot);
		// Empty Slot on creation
		guessSlot.put(id, false);

	}

	// Create Player Icons
	private void playerIcons(LinearLayout players, int id) {
		// Get Image Dimensions (currently 68x68)
		int dimen = (int) getResources().getDimension(R.dimen.image_dimen);
		// FrameLayout for each image
		FrameLayout slot = new FrameLayout(this);
		slot.setLayoutParams(new LinearLayout.LayoutParams(0, dimen, 1f));

		// ImageView for images
		ImageView profileSlot = new ImageView(this);
		Bitmap bm;

		if (imgData != null) {
			byte[] bitmapData = Base64.decode(imgData, Base64.DEFAULT);
			bm = BitmapFactory
					.decodeByteArray(bitmapData, 0, bitmapData.length);
		} else {
			// Create bitmap from resource TODO: It will fetch images from
			// server
			bm = BitmapFactory.decodeResource(this.getResources(),
					R.drawable.profile);
		}

		profileSlot.setId(id); // 1, 2 ,3 ,4
		profileSlot.setImageBitmap(BitmapDecoder.getCircleBitmap(bm));
		profileSlot.setOnTouchListener(mListener);
		profileSlot.setLayoutParams(new FrameLayout.LayoutParams(dimen, dimen,
				Gravity.CENTER_HORIZONTAL));
		players.addView(slot);
		slot.addView(profileSlot);

		// Player guesses are "Skip Question" in the beginning
		playerGuess.put(idToUser.get(id), Answer.SKIPQUESTION);

	}

	// OverLay Icon
	private void overlayIcon(int id, boolean correct) {
		// Get Image Dimensions (currently 68x68)
		int dimen = (int) getResources().getDimension(R.dimen.image_dimen);
		ImageView playerImage = (ImageView) findViewById(id);
		ViewGroup parent = (ViewGroup) playerImage.getParent();
		ImageView overlay = new ImageView(this);

		overlay.setLayoutParams(new FrameLayout.LayoutParams(dimen, dimen,
				Gravity.CENTER_HORIZONTAL));
		if (correct)
			overlay.setImageResource(R.drawable.correct);
		else
			overlay.setImageResource(R.drawable.incorrect);
		parent.addView(overlay);

	}

	private class MyGestureListener extends
			GestureDetector.SimpleOnGestureListener {

		private static final String DEBUG_TAG = "Gestures";

		@Override
		public boolean onDown(MotionEvent event) {
			Log.d(DEBUG_TAG, "onDown: " + event.toString());
			return true;
		}

		@Override
		public boolean onFling(MotionEvent event1, MotionEvent event2,
				float velocityX, float velocityY) {

			final float yDistance = event1.getY() - event2.getY();
			// Swipe Up or Down
			if (Math.abs(yDistance) > SWIPE_THRESHOLD
					&& Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) 
			{
				guess = playerGuess.get(idToUser.get(viewTouched));
				// Swipe Up
				if (yDistance > 0 && guess != Answer.ANSWER1) {
					movePlayer(Answer.ANSWER1);
				}
				// Swipe Down
				else if (yDistance < 0 && guess != Answer.ANSWER2) {
					movePlayer(Answer.ANSWER2);
				}
			}

			Log.d(DEBUG_TAG,
					"onFling: " + event1.toString() + event2.toString());
			return true;
		}

	}

	// Moves a player Icon to an Empty Slot depending on the choice of the user
	private void movePlayer(Answer choice) {
		// Find The Image that is swiped by the User
		mImage = new ImageView(GuessActivity.this);
		mImage = (ImageView) findViewById(viewTouched);
		// Get Parent of Image
		ViewGroup oldParent = (ViewGroup) mImage.getParent();
		// Get Answer that player image is placed
		Answer guess = playerGuess.get(idToUser.get(viewTouched));

		int emptySlot = findEmptySlot(choice);
		ImageView profile = (ImageView) findViewById(emptySlot);
		ViewGroup newParent = (ViewGroup) profile.getParent();

		int[] mImagePos = new int[2];
		int[] profilePos = new int[2];
		profile.getLocationOnScreen(profilePos);
		mImage.getLocationOnScreen(mImagePos);
		mImage.setVisibility(View.GONE);
		mImage.bringToFront();
		LinearLayout slayout = (LinearLayout)findViewById(R.id.Second_Answer);
		((View)slayout).requestLayout();
		((View)slayout).invalidate();
		mImage.requestLayout();
		mImage.invalidate();
	
		// Animate movement to new position	
		TranslateAnimation anim = new TranslateAnimation( 0, profilePos[0]-mImagePos[0] , 0, profilePos[1]-mImagePos[1]);
		anim.setRepeatMode(0);
		anim.setDuration(500); 
		anim.setFillAfter( true );
		//anim.setZAdjustment(Animation.ZORDER_TOP);
		anim.setAnimationListener(new MyAnimationListener(newParent, profile));
		mImage.startAnimation(anim);
		

		// Mark Slot full
		guessSlot.put(emptySlot, true);
		// Mark previous Slot as empty
		if (guess != Answer.SKIPQUESTION)
			guessSlot.put(oldParent.getChildAt(0).getId(), false);

		// Map player to answer that the user guesses
		playerGuess.put(idToUser.get(viewTouched), choice);

	}

	// Finds empty slot depending on the answer that the user picked
	private int findEmptySlot(Answer ans) {
		Set<Entry<Integer, Boolean>> set = guessSlot.entrySet();
		Iterator<Entry<Integer, Boolean>> i = set.iterator();
		// Find Empty slot and return id depending on the Answer
		while (i.hasNext()) {
			Map.Entry<Integer, Boolean> me = (Map.Entry<Integer, Boolean>) i
					.next();
			// Empty Slot
			boolean empty = (Boolean) me.getValue();
			if (ans == Answer.ANSWER1) {
				if (empty == false && (Integer) me.getKey() < 20)
					return (Integer) me.getKey();
			} else if (ans == Answer.ANSWER2) {
				if (empty == false && (Integer) me.getKey() > 20)
					return (Integer) me.getKey();
			}
		}

		return 0;
	}

	// User Pressed Done button
	public void guessResults(View view) {
		// Compare Answers to User Guesses
		for (Map.Entry<String, Answer> entry : userToAnswer.entrySet()) {
			// Correct Guess
			if (entry.getValue() == playerGuess.get(entry.getKey())) {
				// Overlay correct Feedback
				for (Map.Entry<Integer, String> idEntry : idToUser.entrySet()) {
					// Get The Id of the player image to overlay
					if (idEntry.getValue() == entry.getKey()) {
						overlayIcon(idEntry.getKey(), true);
						break;
					}
				}
				// Increment score
				score++;
			}
			// Incorrect Guess
			else {
				// Get The Id of the player image to move
				for (Map.Entry<Integer, String> idEntry : idToUser.entrySet()) {
					if (idEntry.getValue() == entry.getKey()) {
						viewTouched = idEntry.getKey();
						// Move view to correct spot
						movePlayer(entry.getValue());
						// Overlay incorrect Feedback
						overlayIcon(idEntry.getKey(), false);
					}
				}

				// Animate

			}
		}
		finished = true;
		// Remove Done Button
		ViewGroup parent = (ViewGroup) view.getParent();
		parent.removeView(view);
	}

	// User Pressed Continue button to proceed to Score Activity
	public void goToScore(View view) {
		
		if(!goToScoreSelected) {
			progress.setTitle("Loading");
			progress.setMessage("Wait while loading...");
			progress.show();
			goToScoreSelected = true;
			// Lock Score to Update User to Score Map (Current Score Map)
			manager.lockKeyOfUser(USER_TO_SCORE_KEY, game.getGameId());
		}
	
	}

	// Animating swiping
	private class MyAnimationListener implements AnimationListener {

		private ViewGroup parent;
		private ImageView slot;

		public MyAnimationListener(ViewGroup viewgroup, ImageView slot) {
			this.parent = viewgroup;
			this.slot = slot;
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			mImage.clearAnimation();
			FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) slot
					.getLayoutParams();
			ViewGroup oldparent = (ViewGroup) mImage.getParent();
			oldparent.removeView(mImage);
			mImage.setLayoutParams(layoutParams);
			parent.addView(mImage);
			mImage.setVisibility(View.VISIBLE);
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
		// Unlock after updating score
		manager.unlockKeyOfUser(key, user);
	}

	@Override
	public void loadedValueForKeyOfUser(JSONObject json, String key, String user) {
		// Load images of other players and <User, Answer> Map
		if (key.equals(USER_TO_ANSWER_KEY)) {
			try {
				Gson gson = new Gson();
				userToAnswer = gson.fromJson(json.getString("value"),
						new TypeToken<Map<String, Answer>>() {
						}.getType());
				if (userToAnswer == null) {
					userToAnswer = new HashMap<String, Answer>();
					Log.d(TAG_ERROR, "No Answers");
				}
				// Load images of other players and Answers
				else {
					// Create Slots for the Answers
					guessSlot = new LinkedHashMap<Integer, Boolean>();
					// Map for id to image icons to Users
					idToUser = new HashMap<Integer, String>();
					// Player Guess
					playerGuess = new HashMap<String, Answer>();

					// Load Images (TODO 1: Only from other players
					// TODO 2: Fetch them internally if no new players)
					for (Map.Entry<String, Answer> entry : userToAnswer
							.entrySet()) {
						// Load Images of other players
						if(!entry.getKey().equals(userName))
							manager.loadValueForKeyOfUser("profile", entry.getKey());
						idToUser.put(id, entry.getKey());
					}

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
				// Load player image
				if(player != null) {
					imgData = player.getImage();
					LinearLayout players = (LinearLayout) findViewById(R.id.Players);
					playerIcons(players, id); // 1, 2, ...
					LinearLayout firstAnswer = (LinearLayout) findViewById(R.id.First_Answer_Slot);
					emptySlots(firstAnswer, id + 10); // 11, 12, ...
					LinearLayout secondAnswer = (LinearLayout) findViewById(R.id.Second_Answer_Slot);
					emptySlots(secondAnswer, id + 20); // 21, 22, ...
					// Next id
					id++;
					progress.dismiss();
				}
				

			} catch (JsonSyntaxException e) {
				Log.d(TAG_ERROR,
						"JsonSyntaxException while parsing: " + json.toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// Put User Current Score
		else if (key.equals(USER_TO_SCORE_KEY)) {

			try {
				Gson gson = new Gson();
				// Load current score map from server
				userToScore = gson.fromJson(json.getString("value"),
						new TypeToken<Map<String, Integer>>() {
						}.getType());
				if (userToScore == null) {
					userToScore = new HashMap<String, Integer>();
					Log.d(TAG_ERROR, "No Score");
				}
				// Put Current User score in the map
				userToScore.put(userName, score);

				// Save User current score in the Server
				String userToScoreJson = gson.toJson(userToScore);
				manager.saveValueForKeyOfUser(key, user, userToScoreJson);

			} catch (JsonSyntaxException e) {
				Log.d(TAG_ERROR,
						"JsonSyntaxException while parsing: " + json.toString());
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
		// Owner increases counter when a player has guessed and updated userToScore key
		if(key.equals(USER_TO_SCORE_KEY)) {
			numberOfFinishedPlayers++;
			if(numberOfFinishedPlayers == numberOfPlayers) {
				manager.ignoreKeyOfUser(key, user);
				manager.saveValueForKeyOfUser(ANSWERING_DONE_KEY, game.getGameId(), "guessingDone");
				Intent intent = new Intent(this, ScoreActivity.class);
				intent.putExtra("game", game);
				intent.putExtra("numberOfPlayers", numberOfPlayers);
				this.startActivity(intent);
			}
		} 
		// When all players have guessed the owner changes the AnsweringDone key to transition
		// to Score Activity
		else if(key.equals(ANSWERING_DONE_KEY)) {
			manager.ignoreKeyOfUser(key, user);
			progress.dismiss();
			Intent intent = new Intent(this, ScoreActivity.class);
			intent.putExtra("game", game);
			intent.putExtra("numberOfPlayers", numberOfPlayers); // TODO check if necessary
			this.startActivity(intent);
		}

	}

	@Override
	public void lockedKeyofUser(JSONObject json, String key, String user) {
		// Load User to Score Map
		try {
			if (json.getString("code").equals("1")) {
				manager.loadValueForKeyOfUser(key, user);
			} else {
				manager.lockKeyOfUser(key, user);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void unlockedKeyOfUser(JSONObject json, String key, String user) {
	
	}
}
