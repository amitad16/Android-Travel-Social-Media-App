package com.example.android.travel.Utils;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by user on 30-03-2018.
 */

public class FileSearch {

    private static final String TAG = "FileSearch";
//    private static ArrayList<String> pathArray;
    public static final  ArrayList<String> dirPathArray = new ArrayList<>();
//    public static final  ArrayList<String> imgDirPathArray = new ArrayList<>();

//    public static final  ArrayList<String> imgPathArray = new ArrayList<>();


    private static String containsImg(String directory){
        File file = new File(directory);
        File[] listFiles = file.listFiles();
        // Iterate over the contents of the given file list
        for(File listFile : listFiles){
            if (listFile.getName().startsWith(".") ||
                    listFile.getName().startsWith("com.")) {
                continue;
            }
            if (listFile.isFile()) {
                // If you were given a file, return true if it's a jpg
                if (listFile.getName().toLowerCase().endsWith("jpg") ||
                        listFile.getName().toLowerCase().endsWith("jpeg") ||
                        listFile.getName().toLowerCase().endsWith("png")) {
                    Log.d(TAG, "containsImg: listfile: " + directory + " 111111111111 " + listFile.getName());
                    if (!dirPathArray.contains(directory)) {
                        dirPathArray.add(directory);
                    }
                    return directory;
                }
            } else if (listFile.isDirectory()){
                // If it is a directory, check its contents recursively
                directory = String.valueOf(listFile.getAbsolutePath());
                containsImg(directory);
//                if (containsImg(directory) != null) {
//                    Log.d(TAG, "containsImg: listfile: " + directory + " 222222222222222222 " + listFile.getName());
//                    dirPathArray.add(directory);
//                  //  return directory;
//                    continue;
//                }
            }
        }
        // If none of the files were jpgs, and none of the directories contained jpgs, return false
        return null;
    }

    /**
     * Search a directory and return a list of all **directories** contained inside
     * @param directory
     * @return
     */
    public static ArrayList<String> getDirectoryPaths(String directory) {
//        ArrayList<String> imgDirPathArray = new ArrayList<>();
        File file = new File(directory);
        File[] listFiles = file.listFiles();
        for (File listFile : listFiles) {
            if (listFile.isDirectory()) {
                if (listFile.getName().equals("Android") ||
                        listFile.getName().startsWith(".") ||
                        listFile.getName().startsWith("com.")) {
                    Log.d(TAG, "getDirectoryPaths: List file name exceptions: " + listFile.getName()
                    );
                    continue;
                }
                Log.d(TAG, "getDirectoryPaths: listFile " + String.valueOf(listFile));
                containsImg(String.valueOf(listFile.getAbsolutePath()));
//                if (imgDir != null) {
//                    Log.d(TAG, "getDirectoryPaths: imgDir(2): " + imgDir);
//                    imgDirPathArray.add(imgDir);
//                }
//                dirPathArray.add(listFile.getAbsolutePath());
//                getDirectoryPaths(String.valueOf(listFile.getAbsolutePath()));
            }
        }
        Log.d(TAG, "getDirectoryPaths: imgDirPathArray: " + dirPathArray);
        Log.d(TAG, "getDirectoryPaths: count imgDirPathArray: " + dirPathArray.size());
        return dirPathArray;
    }

    /**
     * Search a directory and return a list of all **file** contained inside
     * @param directory
     * @return
     */
    public static ArrayList<String> getFilePaths(String directory) {
        ArrayList<String> imgPathArray = new ArrayList<>();
        File file = new File(directory);
        File[] listFiles = file.listFiles();
        for (File listFile : listFiles) {
            if (listFile.isFile()) {
                if (listFile.getName().toLowerCase().endsWith("jpg") ||
                        listFile.getName().toLowerCase().endsWith("jpeg") ||
                        listFile.getName().toLowerCase().endsWith("png") ||
                        listFile.getName().toLowerCase().endsWith("JPG") ||
                        listFile.getName().toLowerCase().endsWith("JPEG") ||
                        listFile.getName().toLowerCase().endsWith("PNG")) {
                    imgPathArray.add(listFile.getAbsolutePath());
                } else {
                    continue;
                }
            }
        }
        return imgPathArray;
    }
}
