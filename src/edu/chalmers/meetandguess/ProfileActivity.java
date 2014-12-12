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
import android.widget.EditText;
import android.widget.ImageButton;

public class ProfileActivity extends ActionBarActivity implements
		NetworkingEventHandler {

	private static final String TAG_ERROR = "ERROR";
	private static final int SELECT_IMAGE = 1;
	
	private Player player;
	private String imgData;
	private NetworkingManager manager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile);

		// Toolbar Layout
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar()
				.setTitle(getResources().getText(R.string.profile));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Access the user name
		SharedPreferences sharedPref = getSharedPreferences("edu.chalmers.meetandguess.save_app_state", MODE_PRIVATE);
		String userName = sharedPref.getString("username", null);
		if(userName != null) {
			// Create network manager if user already exists
			this.manager = new NetworkingManager(this, "G9", userName);
			this.manager.loadValueForKeyOfUser("profile", userName);		
			EditText userNameValue = (EditText) findViewById(R.id.username_edit);
			userNameValue.setEnabled(false);
			userNameValue.setFocusable(false);
			userNameValue.setFocusableInTouchMode(false);
		} else {
			EditText userNameValue = (EditText) findViewById(R.id.username_edit);
			userNameValue.setEnabled(true);
			userNameValue.setFocusable(true);
			userNameValue.setFocusableInTouchMode(true);
			userNameValue.requestFocus();
		}
	}

	public void selectImage(View view) {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(Intent.createChooser(intent, "Select Picture"),
				SELECT_IMAGE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case SELECT_IMAGE:
			if (resultCode == RESULT_OK && data != null) {
				try {
					// Read image
					Uri uri = data.getData();
					InputStream input = getContentResolver().openInputStream(uri);
					Bitmap bm = BitmapDecoder.decodeSampledBitmapFromInputStream(input, null, 48, 48);

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

	@Override
	public void savedValueForKeyOfUser(JSONObject json, String key, String user) {
		if (key.equals("profile")) {
			NavUtils.navigateUpFromSameTask(this);
		}
	}

	@Override
	public void loadedValueForKeyOfUser(JSONObject json, String key, String user) {
		if (key.equals("profile")) {
			try {
				Gson gson = new Gson();
				player = gson.fromJson(json.getString("value"), Player.class);
				if(player != null) { // TODO should not be necessary if every existing user MUST have a profile picture
					imgData = player.getImage();
					displayProfileData();
				}
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
		// Display user name
		if(player.getUsername() != null) {
			EditText userNameValue = (EditText) findViewById(R.id.username_edit);
			userNameValue.setText(player.getUsername());
		}
		// Display first name
		if(player.getFirstname() != null) {
			EditText firstNameValue = (EditText) findViewById(R.id.firstname_edit);
			firstNameValue.setText(player.getFirstname());
		}
		// Display country
		if(player.getCountry() != null) {
			EditText countryValue = (EditText) findViewById(R.id.country_edit);
			countryValue.setText(player.getCountry());
		}
	}
	
	private void savePlayer() {
		EditText userNameValue = (EditText) findViewById(R.id.username_edit);
		String userName = userNameValue.getText().toString();
		EditText firstNameValue = (EditText) findViewById(R.id.firstname_edit);
		String firstName = firstNameValue.getText().toString();
		EditText countryValue = (EditText) findViewById(R.id.country_edit);
		String country = countryValue.getText().toString();
		if(userName.equals("") || imgData == null) {
		// TODO alert: username and profile picture must exist
		} else { // if everything is okay save the user data
			if (this.player == null) { // new user
				this.manager = new NetworkingManager(this, "G9", userName);
				this.player = new Player(userName, firstName, country, this.imgData);
				SharedPreferences sharedPref = getSharedPreferences("edu.chalmers.meetandguess.save_app_state", MODE_PRIVATE);
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putString("username", "Julia");
				editor.commit();
			} else {
				this.player.setUsername(userName);
				this.player.setFirstname(firstName);
				this.player.setCountry(country);
				this.player.setImage(this.imgData);
			}
			Gson gson = new Gson();
			String playerJson = gson.toJson(this.player);
			manager.saveValueForKeyOfUser("profile", this.player.getUsername(),
					playerJson);
		}
	}

	private void enableEditing() {
		ImageButton profilePicture = (ImageButton) findViewById(R.id.profile_picture);
		profilePicture.setEnabled(true);
		EditText userNameValue = (EditText) findViewById(R.id.username_edit);
		userNameValue.setEnabled(true);
		userNameValue.setFocusable(true);
		userNameValue.setFocusableInTouchMode(true);
		userNameValue.requestFocus();
		EditText firstNameValue = (EditText) findViewById(R.id.firstname_edit);
		firstNameValue.setEnabled(true);
		firstNameValue.setFocusable(true);
		firstNameValue.setFocusableInTouchMode(true);
	}

	private void disableEditing() {
		ImageButton profilePicture = (ImageButton) findViewById(R.id.profile_picture);
		profilePicture.setEnabled(false);
		EditText userNameValue = (EditText) findViewById(R.id.username_edit);
		userNameValue.setEnabled(false);
		userNameValue.setFocusable(false);
		userNameValue.setFocusableInTouchMode(false);
		EditText firstNameValue = (EditText) findViewById(R.id.firstname_edit);
		firstNameValue.setEnabled(false);
		firstNameValue.setFocusable(false);
		firstNameValue.setFocusableInTouchMode(false);
	}
}
