package edu.chalmers.meetandguess;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.Drawable;
import android.util.Base64;

import com.google.gson.Gson;

import edu.chalmers.qdnetworking.NetworkingEventHandler;
import edu.chalmers.qdnetworking.NetworkingManager;

public class ServerReset implements NetworkingEventHandler {

	private static final String GROUP = "G9";
	private static final String RESET_USER = "reset";
	private static final String GAME_MANAGER_USER = "gameManager";
	private static final String GAME_DATA_USER = "gameData";
	private static final String ACTIVE_GAMES_KEY = "activeGames";
	private static final String USER_ID_KEY = "userId";
	private static final String GAME_ID_KEY = "gameId";
	private static final String QUESTION_LIST_KEY = "questionList";
	private static final String CURRENT_QUESTION_KEY = "currentQuestion";
	private static final String USER_TO_ANSWER_KEY = "userToAnswer";
	private static final String USER_TO_SCORE_KEY = "userToScore";
	private static final String USER_TO_TOTAL_SCORE_KEY = "userToTotalScoreKey";
	private static final String ANSWERING_DONE_KEY = "answeringDone";
	private static final String GUESSING_DONE_KEY = "guessingDone";
	private static final String REQUEST_JOINING_KEY = "newUser";
	private static final String GAME_KEY = "game";
	private static final String PROFILE_KEY = "profile";

	private NetworkingManager manager = new NetworkingManager(this, GROUP, RESET_USER);
	
	public void resetActiveGamesList() {
		manager.unlockKeyOfUser(ACTIVE_GAMES_KEY, GAME_MANAGER_USER);
		manager.deleteKeyOfUser(ACTIVE_GAMES_KEY, GAME_MANAGER_USER);
		manager.saveValueForKeyOfUser(ACTIVE_GAMES_KEY, GAME_MANAGER_USER, null);
	}
	
	public void resetGames() {
		for(int i = 1; i < 10; i++) {
			String gameId = "M" + i;
			manager.deleteKeyOfUser(QUESTION_LIST_KEY, gameId);
			manager.deleteKeyOfUser(CURRENT_QUESTION_KEY, gameId);
			manager.deleteKeyOfUser(USER_TO_ANSWER_KEY, gameId);
			manager.deleteKeyOfUser(USER_TO_SCORE_KEY, gameId);
			manager.deleteKeyOfUser(USER_TO_TOTAL_SCORE_KEY, gameId);
			manager.deleteKeyOfUser(ANSWERING_DONE_KEY, gameId);
			manager.deleteKeyOfUser(GUESSING_DONE_KEY, gameId);
			manager.deleteKeyOfUser(REQUEST_JOINING_KEY, gameId);
			manager.deleteKeyOfUser(GAME_KEY, gameId);
		}
		manager.saveValueForKeyOfUser(GAME_ID_KEY, GAME_MANAGER_USER, "M1");
	}
	
	public void resetUsers() {
		for(int i = 1; i < 20; i++) {
			String userId = "U" + i;
			manager.deleteKeyOfUser(PROFILE_KEY, userId);
		}
		manager.saveValueForKeyOfUser(USER_ID_KEY, GAME_MANAGER_USER, "U1");
	}
	
	public void resetQuestionList() {
		Question firstQuestion = new Question("Which animal is the cuter one?", "Dog", "Cat");
		Question secondQuestion = new Question("Have you been to Australia?", "Yes", "No");
		Question thirdQuestion = new Question("Have you been to Asia?", "Yes", "No");
		List<Question> questionList = new ArrayList<Question>();
		questionList.add(firstQuestion);
		questionList.add(secondQuestion);
		questionList.add(thirdQuestion);
		Gson gson = new Gson();
		String questionJson = gson.toJson(questionList);
		manager.saveValueForKeyOfUser(QUESTION_LIST_KEY, GAME_DATA_USER, questionJson);
	}
	
	public void setUpServer(Context context) {
		resetQuestionList();
		manager.saveValueForKeyOfUser(USER_ID_KEY, GAME_MANAGER_USER, "U1");
		manager.saveValueForKeyOfUser(GAME_ID_KEY, GAME_MANAGER_USER, "M14");
				
		// set up active games
		List<Game> activeGames = new LinkedList<Game>();
		
		Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.male);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bm.compress(CompressFormat.JPEG, 100, bos);
		byte[] byteImageData = bos.toByteArray();
		String imgData = Base64.encodeToString(byteImageData, Base64.DEFAULT);
		activeGames.add(new Game("x", "Landvetter", "I am at Gate 10 next to the small kiosk. I am wearing a red shirt and black jeans.", null, null, "John", imgData));
		
		Bitmap bm2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.female);
		ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
		bm2.compress(CompressFormat.JPEG, 100, bos2);
		byte[] byteImageData2 = bos2.toByteArray();
		String imgData2 = Base64.encodeToString(byteImageData2, Base64.DEFAULT);
		activeGames.add(new Game("y", "Landvetter", "I am at Gate 28. I am wearing a black shirt and I am probably reading a book.", null, null, "Lisa", imgData2));

		Gson gson = new Gson();
		String gameJson = gson.toJson(activeGames);
		manager.saveValueForKeyOfUser(ACTIVE_GAMES_KEY, GAME_MANAGER_USER, gameJson);
	}

	@Override
	public void savedValueForKeyOfUser(JSONObject json, String key, String user) {
		// TODO Auto-generated method stub
	}

	@Override
	public void loadedValueForKeyOfUser(JSONObject json, String key, String user) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deletedKeyOfUser(JSONObject json, String key, String user) {
		// TODO Auto-generated method stub

	}

	@Override
	public void monitoringKeyOfUser(JSONObject json, String key, String user) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ignoringKeyOfUser(JSONObject json, String key, String user) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void valueChangedForKeyOfUser(JSONObject json, String key,
			String user) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void lockedKeyofUser(JSONObject json, String key, String user) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unlockedKeyOfUser(JSONObject json, String key, String user) {
		// TODO Auto-generated method stub

	}
	
}
