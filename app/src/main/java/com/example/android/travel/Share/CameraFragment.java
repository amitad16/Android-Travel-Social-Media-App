package com.example.android.travel.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.android.travel.Profile.AccountSettingsActivity;
import com.example.android.travel.R;
import com.example.android.travel.Utils.Permissions;

/**
 * Created by user on 02-02-2018.
 */

public class CameraFragment extends Fragment {

    private static final String TAG = "CameraFragment";

    // Constants
    public static final int CAMERA_FRAGMENT_NUM = 1;
    public static final int GALLERY_FRAGMENT_NUM = 2;
    public static final int CAMERA_REQUEST_CODE = 5;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);

        Button btnLaunchCamera = view.findViewById(R.id.btn_launch_camera);
        btnLaunchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: launching camera");

                if (((ShareActivity)getActivity()).getCurrentTabNumber() == CAMERA_FRAGMENT_NUM) {
                    if (((ShareActivity)getActivity()).checkPermissions(Permissions.CAMERA_PERMISSION[0])) {
                        Log.d(TAG, "onCreateView: Starting Camera : " + ((ShareActivity)getActivity()).getCurrentTabNumber());
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, CAMERA_REQUEST_CODE);
                    } else {
                        Intent intent = new Intent(getActivity(), ShareActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                }
            }
        });

        return view;
    }

    private boolean isRootTask() {
        return ((ShareActivity) getActivity()).getTask() == 0;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE) {
            Log.d(TAG, "onActivityResult: Done taking a photo");
            Log.d(TAG, "onActivityResult: Attempting to navigate to final share screen");

            Bitmap bitmap;
            bitmap = (Bitmap) data.getExtras().get("data");

            if (isRootTask()) {
                try {
                    Log.d(TAG, "onActivityResult: Received new bitmap from the camera: " + bitmap);
                    Intent intent = new Intent(getActivity(), NextActivity.class);
                    intent.putExtra(getString(R.string.selected_bitmap), bitmap);
                    startActivity(intent);
                } catch (NullPointerException e) {
                    Log.e(TAG, "onActivityResult: NullPointerException" + e.getMessage());
                }
            } else {
                try {
                    Log.d(TAG, "onActivityResult: Received new bitmap from the camera: " + bitmap);
                    Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
                    intent.putExtra(getString(R.string.selected_bitmap), bitmap);
                    intent.putExtra(getString(R.string.return_to_fragment), getString(R.string.edit_profile_fragment));
                    startActivity(intent);
                    getActivity().finish();
                } catch (NullPointerException e) {
                    Log.e(TAG, "onActivityResult: NullPointerException" + e.getMessage());
                }
            }
        }
    }
}
