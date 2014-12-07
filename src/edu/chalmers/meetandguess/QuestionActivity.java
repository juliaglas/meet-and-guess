package edu.chalmers.meetandguess;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import edu.chalmers.qdnetworking.NetworkingEventHandler;
import edu.chalmers.qdnetworking.NetworkingManager;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class QuestionActivity extends Activity implements NetworkingEventHandler{

	private String userName;
	private boolean owner;
	private NetworkingManager manager;
	private List<Question> questionList;
	private int questionNumber;

	@Override
	// TODO only start this activity if the questionNumber was changed before!
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.question);
		
		SharedPreferences sharedPref = getSharedPreferences("edu.chalmers.meetandguess.save_app_state", MODE_PRIVATE);
		this.userName = sharedPref.getString("username", "");
		this.owner = sharedPref.getBoolean("owner", false);
		
		this.manager = new NetworkingManager(this, "G9", this.userName);
		this.manager.loadValueForKeyOfUser("questionList", "gameLogic");
	}
	
	public void displayQuestion() {
		Question currentQuestion = this.questionList.get(this.questionNumber);
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
		Gson gson = new Gson();
		try {
			if(key.equals("questionList")) {
				this.questionList = gson.fromJson(json.getString("value"), new TypeToken<ArrayList<Question>>(){}.getType());
				manager.loadValueForKeyOfUser("questionNumber", "gameLogic");
			} else if(key.equals("questionNumber")) {
				this.questionNumber = gson.fromJson(json.getString("value"), Integer.class);
				this.displayQuestion();
			}
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unlockedKeyOfUser(JSONObject json, String key, String user) {
		// TODO Auto-generated method stub
		
	}
}
