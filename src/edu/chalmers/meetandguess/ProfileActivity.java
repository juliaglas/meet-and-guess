package edu.chalmers.meetandguess;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class ProfileActivity extends ActionBarActivity {
	
	private static final int SELECT_IMAGE = 1;
	private byte[] imgData;
	private Player player;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile);

		// Toolbar Layout
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar()
				.setTitle(getResources().getText(R.string.profile));
		
	}

	public void selectImage(View view) {
		Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case SELECT_IMAGE:
			if (resultCode == RESULT_OK && data != null) { // TODO handle else		
				try {
					// Read image
					Uri uri = data.getData();
					InputStream input = getContentResolver().openInputStream(uri);
					Bitmap bm = BitmapFactory.decodeStream(input);
					// Set image in view
					ImageButton profilePicture = (ImageButton) findViewById(R.id.profile_picture);
					profilePicture.setImageBitmap(bm);
					// Convert to byte array for sending it to the server
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					bm.compress(CompressFormat.JPEG, 10, bos);
					this.imgData = bos.toByteArray();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
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
	
	private void savePlayer() {
		EditText userNameValue = (EditText) findViewById(R.id.username_edit);
		String userName = userNameValue.getText().toString();
		EditText firstNameValue = (EditText) findViewById(R.id.username_edit);
		String firstName = firstNameValue.getText().toString();
		EditText countryValue = (EditText) findViewById(R.id.country_edit);
		String country = countryValue.getText().toString();
		if(this.player==null) {
			
		}
	}
}
