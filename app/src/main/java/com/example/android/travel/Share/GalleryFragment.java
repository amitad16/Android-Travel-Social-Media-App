package com.example.android.travel.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.android.travel.Profile.AccountSettingsActivity;
import com.example.android.travel.R;
import com.example.android.travel.Utils.FilePaths;
import com.example.android.travel.Utils.FileSearch;
import com.example.android.travel.Utils.GridImageAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;

/**
 * Created by user on 02-02-2018.
 */

public class GalleryFragment extends Fragment {

    private static final String TAG = "GalleryFragment";
    private static final int NUM_GRID_COLUMNS = 4;

    // widgets
    private GridView gridView;
    private ImageView galleryImage;
    private ProgressBar progressBar;
    private Spinner directorySpinner;

    // vars
    private ArrayList<String> directories;
    private String append = "file:/";
    private String selectedImage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);

        galleryImage = view.findViewById(R.id.gallery_image_view);
        gridView = view.findViewById(R.id.grid_view);
        directorySpinner = view.findViewById(R.id.spinner_directory);
        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        directories = new ArrayList<>();
        Log.d(TAG, "onCreateView: started");

        ImageView shareClose = view.findViewById(R.id.iv_close_share);
        shareClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: closing the gallery fragment");
                getActivity().finish();
            }
        });

        TextView nextScreen = view.findViewById(R.id.tv_next);
        nextScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: navigating to the final share screen");
                if (isRootTask()) {
                    Intent intent = new Intent(getActivity(), NextActivity.class);
                    intent.putExtra(getString(R.string.selected_image), selectedImage);
                    startActivity(intent);
                } else {
                    Log.d(TAG, "onClick: account settings activity profile photo change");
                    Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
                    intent.putExtra(getString(R.string.selected_image), selectedImage);
                    intent.putExtra(getString(R.string.return_to_fragment), getString(R.string.edit_profile_fragment));
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        });

        init();

        return view;
    }

    private boolean isRootTask() {
        return ((ShareActivity) getActivity()).getTask() == 0;
    }

    private void init() {
        FilePaths filePaths = new FilePaths();
        // Check for other folders inside "storage/emulated/0/pictures"
        if (FileSearch.getDirectoryPaths(filePaths.ROOT_DIR) != null) {
            directories = FileSearch.getDirectoryPaths(filePaths.ROOT_DIR);
        }
//        directories.add(filePaths.CAMERA);

        ArrayList<String> directoryNames = new ArrayList<>();
        for (int i = 0; i < directories.size(); i++) {
            int index = directories.get(i).lastIndexOf("/");
            String string = directories.get(i).substring(index).replace("/", "");
            directoryNames.add(string);
        }


        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, directoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        directorySpinner.setAdapter(adapter);

        directorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemSelected: selected: " + directories.get(i));

                // Setup our image grid for the directory chosen
                if (!directories.get(i).isEmpty()) {
                    setupGridView(directories.get(i));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setupGridView(String selectedDirectory) {
        Log.d(TAG, "setupGridView: directory chosen: " + selectedDirectory);
        final ArrayList<String> imgURLs = FileSearch.getFilePaths(selectedDirectory);

        // Set the grid column width
        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imgWidth = gridWidth / NUM_GRID_COLUMNS;
        Log.d(TAG, "setupGridView: grid View = " + gridView);
        gridView.setColumnWidth(imgWidth);

        // Use the grid image adapter to adapter the images to gridview content/
        GridImageAdapter adapter = new GridImageAdapter(getActivity(), R.layout.layout_grid_imageview, append, imgURLs);
        gridView.setAdapter(adapter);

        // Set the first image to be displayed when the activity fragment view is inflated
        try {
            setImage(imgURLs.get(0), galleryImage, append);
            selectedImage = imgURLs.get(0);
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "setupGridView: ArrayIndexOutOfBoundsException: " + e.getMessage());
        }
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemClick: selected an image: " + imgURLs.get(i));

//                setImage(imgURLs.get(i), galleryImage, append);
                try {
                    setImage(imgURLs.get(i), galleryImage, append);
                    selectedImage = imgURLs.get(i);
                } catch (ArrayIndexOutOfBoundsException e) {
                    Log.e(TAG, "setupGridView: ArrayIndexOutOfBoundsException: " + e.getMessage());
                }
            }
        });
    }

    private void setImage(String imgURL, ImageView img, String append) {
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(append + imgURL, img, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }
        });
    }
}
