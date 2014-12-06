package edu.chalmers.meetandguess;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class QuestionActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.question);
		
		Question currentQuestion = new Question("Have you been to Asia?", "Yes", "No"); // TODO replace this with a question from the server
		
		TextView questionLabel = (TextView) findViewById(R.id.question_label);
		questionLabel.setText(currentQuestion.getQuestion());
		Button anwer1Button = (Button) findViewById(R.id.answer1_button);
		anwer1Button.setText(currentQuestion.getAnswer1());
		Button anwer2Button = (Button) findViewById(R.id.answer2_button);
		anwer2Button.setText(currentQuestion.getAnswer1());
		Button skipQuestionButton = (Button) findViewById(R.id.skip_question_button);
		skipQuestionButton.setText(getResources().getText(R.string.skip_question));
	}
	
	public void answer1Selected(View view) {
		// TODO pass answer to server
	}
	
	public void answer2Selected(View view) {
		// TODO pass answer to server
	}
	
	public void skipQuestion(View view) {
		// TODO handle skipping the question
	}
}
