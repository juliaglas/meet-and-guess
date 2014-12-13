package edu.chalmers.meetandguess;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.chalmers.qdnetworking.NetworkingEventHandler;
import edu.chalmers.qdnetworking.NetworkingManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class QuestionActivity extends ActionBarActivity implements NetworkingEventHandler{
	
	private static final String GROUP = "G9";
	private static final String USER_TO_ANSWER_KEY = "userToAnswer";
	private static final String SHARED_PREF = "edu.chalmers.meetandguess.save_app_state";

	private String userName;
	private NetworkingManager manager;
	private Game game;
	private Answer answer;

	@Override
	// TODO everybody has to update the current question number on his own
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.question);
		
		// Toolbar Layout
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setTitle(getResources().getText(R.string.question));
		
		SharedPreferences sharedPref = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
		this.userName = sharedPref.getString("username", null);
		
		this.manager = new NetworkingManager(this, GROUP, this.userName);
		
		Intent intent = getIntent();
		game = (Game) intent.getParcelableExtra("game");
		displayQuestion();
	}
	
	public void displayQuestion() {
		Question currentQuestion = game.getCurrentQuestion();
		TextView questionLabel = (TextView) findViewById(R.id.question_label);
		questionLabel.setText(currentQuestion.getQuestion());
		Button anwer1Button = (Button) findViewById(R.id.answer1_button);
		anwer1Button.setText(currentQuestion.getAnswer1());
		Button anwer2Button = (Button) findViewById(R.id.answer2_button);
		anwer2Button.setText(currentQuestion.getAnswer2());
		Button skipQuestionButton = (Button) findViewById(R.id.skip_question_button);
		skipQuestionButton.setText(getResources().getText(R.string.skip_question));
	}
	
	public void answer1Selected(View view) {
		answer = Answer.ANSWER1;
		manager.lockKeyOfUser(USER_TO_ANSWER_KEY, game.getGameId());
	}
	
	public void answer2Selected(View view) {
		answer = Answer.ANSWER2;
		manager.lockKeyOfUser(USER_TO_ANSWER_KEY, game.getGameId());
	}
	
	public void skipQuestion(View view) {
		answer = Answer.SKIPQUESTION;
		manager.lockKeyOfUser(USER_TO_ANSWER_KEY, game.getGameId());
	}
	
	@Override
	public void savedValueForKeyOfUser(JSONObject json, String key, String user) {
		manager.unlockKeyOfUser(key, user);
	}

	@Override
	public void loadedValueForKeyOfUser(JSONObject json, String key, String user) {
		try {
				Gson gson = new Gson();
				Map<String, Answer> userToAnswer = gson.fromJson(json.getString("value"), new TypeToken<Map<String, Answer>>(){}.getType());
				if(userToAnswer == null) {
					userToAnswer = new HashMap<String, Answer>();
				}
				userToAnswer.put(userName, answer);
				String userToAnswerJson = gson.toJson(userToAnswer);
				manager.saveValueForKeyOfUser(key, user, userToAnswerJson);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		try {
			if(json.getString("code").equals("1")) {
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
		Intent intent = new Intent(this, GuessActivity.class);
		intent.putExtra("game", game);
		this.startActivityForResult(intent, 0);
	}
}
