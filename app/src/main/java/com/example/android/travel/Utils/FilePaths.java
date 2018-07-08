package com.example.android.travel.Utils;

import android.os.Environment;

/**
 * Created by user on 30-03-2018.
 */

public class FilePaths {
    // "storage/emulated/0"
    public String ROOT_DIR = Environment.getExternalStorageDirectory().getPath();

//    public String CAMERA = ROOT_DIR + "/DCIM/Camera";
//    public String PICTURES = ROOT_DIR + "/Pictures";

    public String FIREBASE_IMAGE_STORAGE = "photos/users/";
}
