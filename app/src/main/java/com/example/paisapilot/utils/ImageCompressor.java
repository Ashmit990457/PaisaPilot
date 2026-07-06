package com.example.paisapilot.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ImageCompressor {
    public static Bitmap compress(Context context, Uri uri, int maxSizePx) {
        try {
            InputStream input = context.getContentResolver().openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, options);
            input.close();

            int width = options.outWidth;
            int height = options.outHeight;

            int inSampleSize = 1;
            if (width > maxSizePx || height > maxSizePx) {
                final int halfHeight = height / 2;
                final int halfWidth = width / 2;
                while ((halfHeight / inSampleSize) >= maxSizePx && (halfWidth / inSampleSize) >= maxSizePx) {
                    inSampleSize *= 2;
                }
            }

            options.inJustDecodeBounds = false;
            options.inSampleSize = inSampleSize;
            
            input = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);
            input.close();

            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }
}
