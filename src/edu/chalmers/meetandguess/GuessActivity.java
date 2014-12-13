package edu.chalmers.meetandguess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.*;

public class GuessActivity extends ActionBarActivity { 
    
    private GestureDetectorCompat mDetector; 
    View.OnTouchListener mListener;
    private ImageView mImage;
    private int numOfPlayers = 4; // TODO: Get number of players, draw slots for swiping icons
    private static final int SWIPE_THRESHOLD= 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;
    private enum Pos {UP, MIDDLE, DOWN};
    private Pos pos;
    private HashMap<Integer, Boolean> guessSlot;  // Slots: <id, empty>
    private HashMap<Integer, Pos> playerGuess;   // Player Guess: <id, pos>
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
     
        // Create Slots for the Answers and User Images
        guessSlot = new HashMap<Integer, Boolean>(numOfPlayers-1);
        playerGuess = new HashMap<Integer, Pos>(numOfPlayers-1);
        LinearLayout firstAnswer = (LinearLayout) findViewById(R.id.First_Answer_Slot);
        LinearLayout secondAnswer = (LinearLayout) findViewById(R.id.Second_Answer_Slot);
        LinearLayout players = (LinearLayout) findViewById(R.id.Players);
        createSlots(firstAnswer, secondAnswer, players);
 
        
    }
    
    // Creates Profile Slots and Images
    private void createSlots(LinearLayout firstAnswer, LinearLayout secondAnswer, LinearLayout players)
    {  
        for(int i=0; i< numOfPlayers -1 ; i++)
        {
        	// Fill them with views 
        	fillSlots(firstAnswer, i+11);  
        	fillSlots(secondAnswer, i+21);
        	fillPlayerIcons(players, i+1);
        }
    }
    
    // Fills an answer with profile slots
    private void fillSlots(LinearLayout answer, int id)
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
    
    // Fills with players Images 
    private void fillPlayerIcons(LinearLayout players, int id)
    {
    	// Get Image Dimensions (currently 68x68)
    	int dimen = (int)getResources().getDimension(R.dimen.image_dimen);
    	// FrameLayout for each image 
    	FrameLayout slot = new FrameLayout(this);
    	slot.setLayoutParams(new LinearLayout.LayoutParams(0, dimen, 1f));
    	
    	// ImageView for images 
    	ImageView profileSlot = new ImageView(this);
    	
    	// Create bitmap from resource TODO: It will fetch images from server
        Bitmap bm = BitmapFactory.decodeResource(this.getResources(),
        R.drawable.profile); 
        
    	profileSlot.setId(id); // 1, 2 ,3 ,4
    	profileSlot.setImageBitmap(getCircleBitmap(bm));
    	profileSlot.setOnTouchListener(mListener);
    	profileSlot.setLayoutParams(new FrameLayout.LayoutParams(dimen, dimen, Gravity.CENTER_HORIZONTAL));
    	players.addView(slot);
    	slot.addView(profileSlot);	
    	// Player Image in the middle on Creation
    	playerGuess.put(id, Pos.MIDDLE);
    
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
        	   // Swipe Up
        	   if(yDistance > 0 && pos!=Pos.UP )
        	   {			   
        		   movePlayer(Pos.UP);
        	   }
        	   // Swipe Down
        	   else if(yDistance < 0 && pos != Pos.DOWN)
        	   {
        		   movePlayer(Pos.DOWN);
        	   }
           }
            
            Log.d(DEBUG_TAG, "onFling: " + event1.toString()+event2.toString());
            return true;
        }
       
    }
    
    // Moves a player Icon to an Empty Slot depending on the choice of the user
    private void movePlayer(Pos choice)
    {
    	// Find The Image that is swiped by the User
	    mImage = new ImageView(GuessActivity.this);
	    mImage = (ImageView)findViewById(viewTouched);
    	// Get Parent of Image
		ViewGroup oldParent = (ViewGroup) mImage.getParent();
		// Get Current Position of player image
		Pos imagePos = playerGuess.get(viewTouched);
		
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
        guessSlot.put(emptySlot, true);
        if(imagePos != Pos.MIDDLE)
     	   guessSlot.put(oldParent.getChildAt(0).getId(), false);
        playerGuess.put(viewTouched, choice);
    
    }
    // Finds empty slot depending on the answer that the user picked
    private int findEmptySlot(Pos pos)
    {
    	Set<Entry<Integer, Boolean>> set = guessSlot.entrySet();
        Iterator<Entry<Integer, Boolean>> i = set.iterator();
        // Find Empty slot and return id depending on the Answer
        while(i.hasNext())
        {
           Map.Entry<Integer, Boolean> me = (Map.Entry<Integer, Boolean>)i.next();
           // Empty Slot
    	   boolean empty = (Boolean) me.getValue();  
           if(pos == Pos.UP)  
           {   
        	   if(empty == false && (Integer)me.getKey()<20)
        		   return (Integer) me.getKey(); 
           }
           else if(pos == Pos.DOWN)
           {
        	   if(empty == false && (Integer)me.getKey()>20)
        		   return (Integer) me.getKey(); 
           }
        }
        
        return 0;
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
    
    // Creates Circular Picture
    private Bitmap getCircleBitmap(Bitmap bitmap) 
    {
    	 final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
    	  bitmap.getHeight(), Bitmap.Config.ARGB_8888);
    	 final Canvas canvas = new Canvas(output);

    	 final int color = Color.RED;
    	 final Paint paint = new Paint();
    	 final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
    	 final RectF rectF = new RectF(rect);

    	 paint.setAntiAlias(true);
    	 canvas.drawARGB(0, 0, 0, 0);
    	 paint.setColor(color);
    	 canvas.drawOval(rectF, paint);

    	 paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
    	 canvas.drawBitmap(bitmap, rect, rect, paint);

    	 bitmap.recycle();

    	 return output;
    }
}
