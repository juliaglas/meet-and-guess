package edu.chalmers.meetandguess;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class QuestionActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.question);
		
		// Toolbar Layout
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setTitle("Question 1");
				
		Question currentQuestion = new Question("Have you been to Asia?", "Yes", "No"); // TODO replace this with a question from the server
		
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
}
