package edu.chalmers.meetandguess;

public class Player {

	private String username;
	private String firstname;
	private String country;
	private byte[] image;
	private int score;
	private Answer answer;	
	
	public Player(String username, String firstname, String country, byte[] image) {
		super();
		this.username = username;
		this.firstname = firstname;
		this.country = country;
		this.image = image;
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getFirstname() {
		return firstname;
	}
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public byte[] getImage() {
		return image;
	}
	public void setImageUrl(byte[] image) {
		this.image = image;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public Answer getAnswer() {
		return answer;
	}
	public void setAnswer(Answer answer) {
		this.answer = answer;
	}
	
}
