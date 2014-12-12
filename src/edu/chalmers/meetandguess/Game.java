package edu.chalmers.meetandguess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Game {

	private final String gameId;
	private final String locationDescription;
	private final String detailedDescription;
	private List<Question> questionList;
	private int currentQuestionNumber;
	private String owner;
	private Map<String, Integer> user2totalScore;
	
	public Game(String gameId, String locationDescription, String detailedDescription, List<Question> questionList, String owner) {
		super();
		this.gameId = gameId;
		this.locationDescription = locationDescription;
		this.detailedDescription = detailedDescription;
		this.questionList = questionList;
		this.currentQuestionNumber = 0;
		this.owner = owner;
		this.user2totalScore = new HashMap<String, Integer>();
	}

	public String getGameId() {
		return gameId;
	}

	public String getLocationDescription() {
		return locationDescription;
	}

	public Map<String, Integer> getUser2totalScore() {
		return user2totalScore;
	}

	public List<Question> getQuestionList() {
		return questionList;
	}

	public void setQuestionList(List<Question> questionList) {
		this.questionList = questionList;
	}

	public int getCurrentQuestionNumber() {
		return currentQuestionNumber;
	}

	public void setCurrentQuestionNumber(int currentQuestionNumber) {
		this.currentQuestionNumber = currentQuestionNumber;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	public String getDetailedDescription() {
		return detailedDescription;
	}
	
	public boolean updateCurrentQuestionNumber() {
		currentQuestionNumber++;
		if(currentQuestionNumber < this.questionList.size()) {
			return true;
		} else {
			return false;
		}
	}
	
	public void increaseScoreForUser(String user, int currentScoreToAdd) {
		int oldScore = user2totalScore.get(user);
		user2totalScore.put(user, oldScore + currentScoreToAdd);
	}
	
}