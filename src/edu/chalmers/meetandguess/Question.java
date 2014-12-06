package edu.chalmers.meetandguess;

public class Question {

	private String question;
	private String answer1;
	private String answer2;
	
	public Question(String question, String answer1, String answer2) {
		super();
		this.question = question;
		this.answer1 = answer1;
		this.answer2 = answer2;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getAnswer1() {
		return answer1;
	}

	public void setAnswer1(String answer1) {
		this.answer1 = answer1;
	}

	public String getAnswer2() {
		return answer2;
	}

	public void setAnswer2(String answer2) {
		this.answer2 = answer2;
	}
	
}
