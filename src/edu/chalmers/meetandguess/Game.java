package edu.chalmers.meetandguess;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


//import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

public class Game implements Parcelable {

	private final String gameId;
	//private final Location location;
	private final String locationDescription;
	private final String detailedDescription;
	private List<Question> questionList;
	private transient int currentQuestionNumber;
	private String owner;
	private transient Map<String, Integer> user2totalScore;
	
	public Game(String gameId, /*Location location, */String locationDescription, String detailedDescription, List<Question> questionList, String owner) {
		super();
		this.gameId = gameId;
		//this.location = location;
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
	
	//public Location getLocation() {
	//	return location;
	//}

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
	
	public Map<String, Integer> getUser2TotalScore() {
		return user2totalScore;
	}
	
	public void setUser2TotalScore(Map<String, Integer> user2totalScore) {
		this.user2totalScore = user2totalScore;
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
	
	public Question getCurrentQuestion() {
		return this.questionList.get(this.currentQuestionNumber);
	}
	
	public void addUser(String user) {
		user2totalScore.put(user, 0);
	}
	
	public void increaseScoreForUser(String user, int currentScoreToAdd) {
		int oldScore = user2totalScore.get(user);
		if(!user2totalScore.containsKey(user)) {
			user2totalScore.put(user, currentScoreToAdd);
		} else {
			user2totalScore.put(user, oldScore + currentScoreToAdd);
		}
	}

	@Override
	public int describeContents() {
		return hashCode();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(gameId);
		//location.writeToParcel(dest, flags);
		dest.writeString(locationDescription);
		dest.writeString(detailedDescription);
		dest.writeTypedList(questionList);
		dest.writeInt(currentQuestionNumber);
		dest.writeString(owner);
		final int mapSize = user2totalScore.size();
        dest.writeInt(mapSize);
        for (Map.Entry<String, Integer> entry : user2totalScore.entrySet()) {
        	dest.writeString(entry.getKey());
        	dest.writeInt(entry.getValue());
        }
	}
	
	public Game(Parcel parcel) {
		gameId = parcel.readString();
		//location = Location.CREATOR.createFromParcel(parcel);
		locationDescription = parcel.readString();
		detailedDescription = parcel.readString();
		questionList = new LinkedList<Question>();
		parcel.readTypedList(questionList, Question.CREATOR);
		currentQuestionNumber = parcel.readInt();
		owner = parcel.readString();
		int mapSize = parcel.readInt();
		user2totalScore = new HashMap<String, Integer>();
		for (int i=0; i<mapSize; i++) {
            String user = parcel.readString();
            int score = parcel.readInt(); 
            user2totalScore.put(user, score);
        }
	}
	
	public static final Parcelable.Creator<Game> CREATOR = new Parcelable.Creator<Game>() {
		@Override
		public Game createFromParcel(Parcel parcel) {
			return new Game(parcel);
		}
		@Override
		public Game[] newArray(int size) {
			return new Game[size];
		}
	};
}