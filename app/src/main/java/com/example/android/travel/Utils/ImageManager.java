package com.example.android.travel.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by user on 03-04-2018.
 */

public class ImageManager {
    private static final String TAG = "ImageManager";

    public static Bitmap getBitmap(String imgUrl) {
        File imgFile = new File(imgUrl);
        FileInputStream fileInputStream = null;
        Bitmap bitmap = null;

        try {
            fileInputStream = new FileInputStream(imgFile);
            bitmap = BitmapFactory.decodeStream(fileInputStream);
        } catch (FileNotFoundException e) {
            Log.d(TAG, "getBitmap: FileNotFoundException: " + e.getMessage());
        } finally {
            try {
                fileInputStream.close();
            } catch (IOException e) {
                Log.d(TAG, "getBitmap: IOException: " + e.getMessage());
            }
        }
        return bitmap;
    }

    /**
     * Return byte array from bitmap
     * quality is greater than 0 but less than 100
     * @param bm
     * @param quality
     * @return
     */
    public static byte[] getByteFromBitmap(Bitmap bm, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }
}
