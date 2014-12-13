package edu.chalmers.meetandguess;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

public class BitmapDecoder {
	
	private static final String ERROR_TAG = "BITMAP";

	public static Bitmap decodeSampledBitmapFromInputStream(InputStream input,
			Rect outPadding, int reqWidth, int reqHeight) {
		input = new BufferedInputStream(input);
		
		// Decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		input.mark(Integer.MAX_VALUE);
		BitmapFactory.decodeStream(input, outPadding, options);
		try {
			input.reset();
		} catch (IOException e) {
			Log.d(ERROR_TAG, "Could not reset input stream!");
		}

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeStream(input, outPadding, options);
	}

	private static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}
	
	// Creates Circular Picture
    public static Bitmap getCircleBitmap(Bitmap bitmap) 
    {
    	Bitmap dstBmp;
     // Convert rectangle to square
     if (bitmap.getWidth() >= bitmap.getHeight()){

       dstBmp = Bitmap.createBitmap(
       bitmap, 
       bitmap.getWidth()/2 - bitmap.getHeight()/2,
          0,
          bitmap.getHeight(), 
          bitmap.getHeight()
          );

     }
     else
     {

       dstBmp = Bitmap.createBitmap(
       bitmap,
          0, 
          bitmap.getHeight()/2 - bitmap.getWidth()/2,
          bitmap.getWidth(),
          bitmap.getWidth() 
          );
     }
     // Create circle
     Bitmap output = Bitmap.createBitmap(dstBmp.getWidth(),
     dstBmp.getHeight(), Config.ARGB_8888);
     Canvas canvas = new Canvas(output);

     final int color = 0xff424242;
     final Paint paint = new Paint();

     final Rect rect1 = new Rect(0, 0, dstBmp.getWidth(), dstBmp.getHeight());
     final RectF rectF1 = new RectF(rect1);


     paint.setAntiAlias(true);
     canvas.drawARGB(0, 0, 0, 0);
     paint.setColor(color);
     canvas.drawOval(rectF1, paint);

     paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
     canvas.drawBitmap(dstBmp, rect1, rect1, paint);

     return output;
    }

	
}
