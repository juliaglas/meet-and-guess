package edu.chalmers.meetandguess;

import org.json.JSONObject;

import edu.chalmers.qdnetworking.NetworkingEventHandler;
import edu.chalmers.qdnetworking.NetworkingManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class QuestionActivity extends ActionBarActivity implements NetworkingEventHandler{

	private String userName;
	private NetworkingManager manager;
	private Game game;

	@Override
	// TODO everybody has to update the current question number on his own
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.question);
		
		// Toolbar Layout
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setTitle(getResources().getText(R.string.question));
		
		SharedPreferences sharedPref = getSharedPreferences("edu.chalmers.meetandguess.save_app_state", MODE_PRIVATE);
		this.userName = sharedPref.getString("username", "");
		
		this.manager = new NetworkingManager(this, "G9", this.userName);
		
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
		// TODO pass answer to server
		Log.d("QuestionActivity Test", "User selected answer 1");
	}
	
	public void answer2Selected(View view) {
		// TODO pass answer to server
		Log.d("QuestionActivity Test", "User selected answer 2");
	}
	
	public void skipQuestion(View view) {
		// TODO handle skipping the question
		Log.d("QuestionActivity Test", "User wants to skip question");
	}

	@Override
	public void savedValueForKeyOfUser(JSONObject json, String key, String user) {
		// TODO Auto-generated method stub
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
}
