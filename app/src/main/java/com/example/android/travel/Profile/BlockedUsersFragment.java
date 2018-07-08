package com.example.android.travel.Profile;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.travel.R;

/**
 * Created by user on 03-02-2018.
 */

public class BlockedUsersFragment extends Fragment {

    private static final String TAG = "BlockedUsersFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blocked_users, container, false);

        return view;
    }
}
