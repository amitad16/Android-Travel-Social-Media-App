package com.example.android.travel.Share;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.android.travel.R;

/**
 * Created by user on 31-03-2018.
 */

public class LocationFragment extends Fragment {

    private static final String TAG = "LocationFragment";

    public EditText location;

    // vars
    public String currentLocation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location, container, false);

        Log.d(TAG, "onCreateView: opening");

        location = view.findViewById(R.id.et_find_location);
        ImageView getLocation = view.findViewById(R.id.get_location);


        getLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentLocation = location.getText().toString();

                NextActivity.viewPager.removeViewAt(0);
                NextActivity.relativeLayout.setVisibility(View.VISIBLE);

            }
        });




        return view;
    }
}
