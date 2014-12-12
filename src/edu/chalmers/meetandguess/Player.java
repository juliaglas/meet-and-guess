package edu.chalmers.meetandguess;

public class Player {

	private String username;
	private String firstname;
	private String country;
	private String image;
	private int score;
	
	public Player(String username, String firstname, String country, String image) {
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
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	
}
