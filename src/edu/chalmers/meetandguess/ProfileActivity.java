package edu.chalmers.meetandguess;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import edu.chalmers.qdnetworking.NetworkingEventHandler;
import edu.chalmers.qdnetworking.NetworkingManager;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class ProfileActivity extends ActionBarActivity implements
		NetworkingEventHandler {
	
	private static final String GROUP = "G9";
	private static final String GAME_MANAGER_USER = "gameManager";
	private static final String USER_ID_KEY = "userId";
	private static final String PROFILE_KEY = "profile";
	private static final String SHARED_PREF = "edu.chalmers.meetandguess.save_app_state";

	private static final int SELECT_IMAGE_REQUEST_CODE = 1;
	private static final String TAG_ERROR = "ERROR";
	
	private NetworkingManager manager;
	private Gson gson = new Gson();
	
	private Player player;
	private String userId;
	private String imgData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile);

		// Toolbar Layout
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setTitle(getResources().getText(R.string.profile));

		// Access the user name
		SharedPreferences sharedPref = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
		userId = sharedPref.getString("username", null);
		
		if(userId != null) { // show profile of an existing user
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			Button createButton = (Button) findViewById(R.id.create_profile_button);
			createButton.setVisibility(View.INVISIBLE);
			createButton.setEnabled(false);
			this.manager = new NetworkingManager(this, GROUP, userId);
			this.manager.loadValueForKeyOfUser(PROFILE_KEY, userId);
		} else { // create profile of a new user
			getSupportActionBar().setDisplayHomeAsUpEnabled(false); // Hide back button
			EditText firstNameValue = (EditText) findViewById(R.id.firstname_edit);
			firstNameValue.requestFocus();
			enableEditing();
			this.manager = new NetworkingManager(this, GROUP, GAME_MANAGER_USER);
			this.manager.loadValueForKeyOfUser(USER_ID_KEY, GAME_MANAGER_USER);
		}
	}

	public void selectImage(View view) {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE_REQUEST_CODE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case SELECT_IMAGE_REQUEST_CODE:
			if (resultCode == RESULT_OK && data != null) {
				try {
					// Read image
					Uri uri = data.getData();
					InputStream input = getContentResolver().openInputStream(uri);
					Bitmap bm = BitmapDecoder.decodeSampledBitmapFromInputStream(input, null, 96, 96);

					// Set image in view
					ImageButton profilePicture = (ImageButton) findViewById(R.id.profile_picture);
					profilePicture.setImageBitmap(bm);
					
					// Compress it for sending it to the server
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					bm.compress(CompressFormat.JPEG, 100, bos);
					byte[] byteImageData = bos.toByteArray();
					this.imgData = Base64.encodeToString(byteImageData, Base64.DEFAULT);
				} catch (FileNotFoundException e) {
					Log.d(TAG_ERROR, "Could not read input stream of selected profile image");
				}
			} else {
				Log.d(TAG_ERROR, "Could not read input stream of selected profile image");
			}
			break;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			savePlayer();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void createProfile(View view) {
		// Respond to create profile button
		savePlayer();
	}

	@Override
	public void savedValueForKeyOfUser(JSONObject json, String key, String user) {
		if(key.equals(USER_ID_KEY)) {
			this.manager = new NetworkingManager(this, GROUP, userId);
		} else if (key.equals(PROFILE_KEY)) {
			Intent intent = NavUtils.getParentActivityIntent(this);
			intent.putExtra("userName", player.getUsername());
			setResult(RESULT_OK, intent);
			NavUtils.navigateUpTo(this, intent);	
		}
	}

	@Override
	public void loadedValueForKeyOfUser(JSONObject json, String key, String user) {
		if(key.equals(USER_ID_KEY)) {
			try {
				userId = gson.fromJson(json.getString("value"), String.class);
				userId = json.getString("value");
				if(userId.equals("null")) {
					userId = "U1";
				}
				String copyOfUserId = new String(userId);
				int nextUserId = Integer.parseInt(copyOfUserId.replaceAll(
						"[^\\d.]", ""));
				nextUserId++;
				String nextUserIdString = "U" + nextUserId;
				manager.saveValueForKeyOfUser(key, user, nextUserIdString);
			} catch (JsonSyntaxException e) {
				Log.d(TAG_ERROR, "JsonSyntaxException while parsing: " + json.toString());
			} catch (JSONException e) {
				Log.d(TAG_ERROR, "JsonSyntaxException while parsing: " + json.toString());
			}
		} else if (key.equals(PROFILE_KEY)) {
			try {
				player = gson.fromJson(json.getString("value"), Player.class);
				imgData = player.getImage();
				displayProfileData();
			} catch (JsonSyntaxException e) {
				Log.d(TAG_ERROR, "JsonSyntaxException while parsing: " + json.toString());
			} catch (JSONException e) {
				Log.d(TAG_ERROR, "JsonSyntaxException while parsing: " + json.toString());
			}
		}
	}

	@Override
	public void deletedKeyOfUser(JSONObject json, String key, String user) {
	}

	@Override
	public void monitoringKeyOfUser(JSONObject json, String key, String user) {
	}

	@Override
	public void ignoringKeyOfUser(JSONObject json, String key, String user) {
	}

	@Override
	public void valueChangedForKeyOfUser(JSONObject json, String key,
			String user) {
	}

	@Override
	public void lockedKeyofUser(JSONObject json, String key, String user) {
	}

	@Override
	public void unlockedKeyOfUser(JSONObject json, String key, String user) {
	}
	
	private void displayProfileData() {
		// Display profile picture
		if(player.getImage() != null) {
			byte[] bitmapData = Base64.decode(player.getImage(), Base64.DEFAULT);
			Bitmap bm = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
			ImageButton profilePicture = (ImageButton) findViewById(R.id.profile_picture);
			profilePicture.setImageBitmap(bm);
		}
		// Display first name
		if(player.getFirstname() != null) {
			EditText firstNameValue = (EditText) findViewById(R.id.firstname_edit);
			firstNameValue.setText(player.getFirstname());
		}
		// Display age
		if(player.getAge() != -1) {
			EditText ageValue = (EditText) findViewById(R.id.age_edit);
			ageValue.setText(String.valueOf(player.getAge()));
		}
		// Display city
		if(player.getCity() != null) {
			EditText cityValue = (EditText) findViewById(R.id.city_edit);
			cityValue.setText(player.getCity());
		}
		// Display country
		if(player.getCountry() != null) {
			EditText countryValue = (EditText) findViewById(R.id.country_edit);
			countryValue.setText(player.getCountry());
		}
		// Display score
		if(player.getScore() != -1) {
			EditText scoreValue = (EditText) findViewById(R.id.score_display);
			scoreValue.setText(String.valueOf(player.getScore()));
		}
	}
	
	private void savePlayer() {
		if(imgData != null) { // if everything is okay save the user data
			EditText firstNameValue = (EditText) findViewById(R.id.firstname_edit);
			String firstName = firstNameValue.getText().toString();
			EditText ageValue = (EditText) findViewById(R.id.age_edit);
			String ageString = ageValue.getText().toString();
			int age;
			try {
				age = Integer.parseInt(ageString);
			} catch (NumberFormatException e) {
				age = -1;
			}
			EditText cityValue = (EditText) findViewById(R.id.city_edit);
			String city = cityValue.getText().toString();
			EditText countryValue = (EditText) findViewById(R.id.country_edit);
			String country = countryValue.getText().toString();
			if (this.player == null) { // new user
				this.player = new Player(userId, firstName, age, city, country, this.imgData);
				boolean demoSetUp = true;
				if(demoSetUp) {
					this.player.setScore(23);
				}
				SharedPreferences sharedPref = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putString("username", userId);
				editor.commit();
			} else { // existing user
				this.player.setFirstname(firstName);
				this.player.setAge(age);
				this.player.setCity(city);
				this.player.setCountry(country);
				this.player.setImage(this.imgData);
			}
			String playerJson = gson.toJson(this.player);
			manager.saveValueForKeyOfUser("profile", this.player.getUsername(), playerJson);
		} else {
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle(getResources().getText(R.string.create_profile_error));
			alert.setMessage(getResources().getText(R.string.profile_picture_missing_description));
			alert.setPositiveButton(getResources().getText(R.string.ok), null);
			alert.show();
		}
	}

	private void enableEditing() {
		EditText firstNameValue = (EditText) findViewById(R.id.firstname_edit);
		setKeyBoardOnFocusChangeListener(firstNameValue);
		EditText ageValue = (EditText) findViewById(R.id.age_edit);
		setKeyBoardOnFocusChangeListener(ageValue);
		EditText cityValue = (EditText) findViewById(R.id.city_edit);
		setKeyBoardOnFocusChangeListener(cityValue);
		EditText countryValue = (EditText) findViewById(R.id.country_edit);
		setKeyBoardOnFocusChangeListener(countryValue);
	}

	private void setKeyBoardOnFocusChangeListener(EditText view) {
		view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				if (hasFocus) {
				imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
				} else {
				imm.hideSoftInputFromWindow(v.getWindowToken(),0);
			}
			}
		});
	}
}
