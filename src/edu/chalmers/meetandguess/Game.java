package edu.chalmers.meetandguess;

import java.util.ArrayList;
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
	private String ownerName;
	private String ownerImage;
	private transient Map<String, Integer> user2totalScore;
	private ArrayList<String> playerImages;
	private ArrayList<String> userNames;
	
	public ArrayList<String> getUserNames() {
		return userNames;
	}

	public void setUserNames(ArrayList<String> userNames) {
		this.userNames = userNames;
	}

	public Game(String gameId, /*Location location, */String locationDescription, String detailedDescription, List<Question> questionList, String owner, String ownerName, String ownerImage) {
		super();
		this.gameId = gameId;
		//this.location = location;
		this.locationDescription = locationDescription;
		this.detailedDescription = detailedDescription;
		this.questionList = questionList;
		this.currentQuestionNumber = 0;
		this.owner = owner;
		this.ownerName = ownerName;
		this.ownerImage = ownerImage;
		this.user2totalScore = new HashMap<String, Integer>();
		this.playerImages = new ArrayList<String>();
		this.userNames = new ArrayList<String>();
	}

	public String getPlayerImage(int index)
	{
		return playerImages.get(index);
	}
	
	public void addPlayerImage(String image)
	{
		playerImages.add(image);
	}
	
	public String getUserName(int index)
	{
		return userNames.get(index);
	}
	
	public void addUserName(String userName)
	{
		userNames.add(userName);
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
	
	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public String getOwnerImage() {
		return ownerImage;
	}

	public void setOwnerImage(String ownerImage) {
		this.ownerImage = ownerImage;
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
	
	public ArrayList<String> getPlayerImages() {
		return playerImages;
	}

	public void setPlayerImages(ArrayList<String> playerImages) {
		this.playerImages = playerImages;
	}

	public void increaseScoreForUser(String user, int currentScoreToAdd) {
		if(!user2totalScore.containsKey(user)) {
			user2totalScore.put(user, currentScoreToAdd);
		} else {
			int oldScore = user2totalScore.get(user);
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
		dest.writeString(ownerName);
		dest.writeString(ownerImage);
		final int mapSize = user2totalScore.size();
        dest.writeInt(mapSize);
        for (Map.Entry<String, Integer> entry : user2totalScore.entrySet()) {
        	dest.writeString(entry.getKey());
        	dest.writeInt(entry.getValue());
        }
        final int imageListSize = playerImages.size();
        dest.writeInt(imageListSize);
        for(int i=0; i<playerImages.size() ; i++)
        {
        	dest.writeString(playerImages.get(i));
        }
        final int usernameSize = userNames.size();
        dest.writeInt(usernameSize);
        for(int i=0; i<userNames.size() ; i++)
        {
        	dest.writeString(userNames.get(i));
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
		ownerName = parcel.readString();
		ownerImage = parcel.readString();
		int mapSize = parcel.readInt();
		user2totalScore = new HashMap<String, Integer>();
		for (int i=0; i<mapSize; i++) {
            String user = parcel.readString();
            int score = parcel.readInt(); 
            user2totalScore.put(user, score);
        }
		int imageListSize = parcel.readInt();
	    playerImages = new ArrayList<String>();
	    for(int i=0; i<imageListSize ; i++)
	    {
	       String image = parcel.readString();
	       playerImages.add(image);
	    }
	    int userNamesSize = parcel.readInt();
	    userNames = new ArrayList<String>();
	    for(int i=0; i<userNamesSize ; i++)
	    {
	       String image = parcel.readString();
	       userNames.add(image);
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