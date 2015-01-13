package edu.chalmers.meetandguess;

public class Score {
	
	private String playerImage;
	private String userName;
	private int currentScore;
	private int totalScore;
	
	public Score(String playerImage, String userName, int currentScore,
			int totalScore) {
		super();
		this.playerImage = playerImage;
		this.userName = userName;
		this.currentScore = currentScore;
		this.totalScore = totalScore;
	}
	public String getPlayerImage() {
		return playerImage;
	}
	public void setPlayerImage(String playerImage) {
		this.playerImage = playerImage;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public int getCurrentScore() {
		return currentScore;
	}
	public void setCurrentScore(int currentScore) {
		this.currentScore = currentScore;
	}
	public int getTotalScore() {
		return totalScore;
	}
	public void setTotalScore(int totalScore) {
		this.totalScore = totalScore;
	}
	
	
}
