package edu.chalmers.meetandguess;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

public class ProfileActivity extends ActionBarActivity {

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
		Intent select = new Intent(Intent.ACTION_GET_CONTENT, null);
		select.setType("image/*");
		select.putExtra("return-data", true);
		startActivityForResult(select, 1);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case 1:
			Uri uri;
			if (requestCode == 1 && data != null && (uri = data.getData()) != null) { // TODO handle else
				
				Cursor cursor = getContentResolver()
						.query(uri,
								new String[] { android.provider.MediaStore.Images.ImageColumns.DATA },
								null, null, null);
				cursor.moveToFirst();

				// Link to the image
				final String imageFilePath = cursor.getString(0);
				Log.v("imageFilePath", imageFilePath);
				File photo = new File(imageFilePath);

				byte[] imgData = new byte[(int) photo.length()];
				FileInputStream pdata = null;
				try {
					pdata = new FileInputStream(photo);
					pdata.read(imgData); // save image data in a byte array
				} catch (FileNotFoundException exception) {
					// TODO Auto-generated catch block
					exception.printStackTrace();
				} catch (IOException exception) {
					// TODO Auto-generated catch block
					exception.printStackTrace();
				}

				cursor.close();
				
				// Display the new image
				ImageButton profilePicture = (ImageButton) findViewById(R.id.profile_picture);
				Bitmap bm = BitmapFactory.decodeByteArray(imgData, 0, imgData.length);
				profilePicture.setImageBitmap(bm);
			}
		}
	}
}
