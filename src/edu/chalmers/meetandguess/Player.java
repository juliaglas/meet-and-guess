package edu.chalmers.meetandguess;

public class Player {

	private String username;
	private String firstname;
	private int age;
	private String city;
	private String country;
	private String image;
	private int score;
	
	public Player(String username, String firstname, int age, String city, String country, String image) {
		super();
		this.username = username;
		this.firstname = firstname;
		this.age = age;
		this.city = city;
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
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
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
