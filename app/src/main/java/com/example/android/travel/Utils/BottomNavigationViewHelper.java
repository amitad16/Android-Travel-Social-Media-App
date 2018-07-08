package com.example.android.travel.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.util.Printer;
import android.view.MenuItem;

import com.example.android.travel.Home.HomeActivity;
import com.example.android.travel.Likes.LikesActivity;
import com.example.android.travel.Profile.ProfileActivity;
import com.example.android.travel.R;
import com.example.android.travel.Search.SearchActivity;
import com.example.android.travel.Share.ShareActivity;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

/**
 * Created by user on 02-02-2018.
 */

public class BottomNavigationViewHelper {

    private static final String TAG = "BottomNavigationViewHel";

    public static void setupBottomNavigationView(BottomNavigationViewEx bottomNavigationViewEx) {
        Log.d(TAG, "setupBottomNavigationView: Setting up bottom navigation view helper class");

        bottomNavigationViewEx.enableAnimation(false);
        bottomNavigationViewEx.enableItemShiftingMode(false);
        bottomNavigationViewEx.enableShiftingMode(false);
        bottomNavigationViewEx.setTextVisibility(false);
    }

    public static void enableNavigation(final Context context, final Activity callingActivity, BottomNavigationViewEx viewEx) {
        viewEx.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.ic_home:
                        Intent homeIntent = new Intent(context, HomeActivity.class);  // ACTIVITY_NUM = 0
                        context.startActivity(homeIntent);
                        callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;
                    case R.id.ic_search:
                        Intent searchIntent = new Intent(context, SearchActivity.class);  // ACTIVITY_NUM = 0
                        context.startActivity(searchIntent);
                        callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;
                    case R.id.ic_plus:
                        Intent addIntent = new Intent(context, ShareActivity.class);  // ACTIVITY_NUM = 0
                        context.startActivity(addIntent);
                        callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;
                    case R.id.ic_heart:
                        Intent likesIntent = new Intent(context, LikesActivity.class);  // ACTIVITY_NUM = 0
                        context.startActivity(likesIntent);
                        callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;
                    case R.id.ic_person:
                        Intent profileIntent = new Intent(context, ProfileActivity.class);  // ACTIVITY_NUM = 0
                        context.startActivity(profileIntent);
                        callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;
                }
                return false;
            }
        });
    }

}
