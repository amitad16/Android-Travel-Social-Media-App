package com.example.android.travel.Profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.android.travel.Login.LoginActivity;
import com.example.android.travel.R;
import com.example.android.travel.Utils.BottomNavigationViewHelper;
import com.example.android.travel.Utils.FirebaseMethods;
import com.example.android.travel.Utils.SectionStatePagerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;

public class AccountSettingsActivity extends AppCompatActivity {

    private static final String TAG = "AccountSettingsActivity";
    private static final int ACTIVITY_NUM = 4;
    private Context context;

    public SectionStatePagerAdapter pagerAdapter;
    private ViewPager viewPager;
    private RelativeLayout relativeLayout;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        context = AccountSettingsActivity.this;

        setupFirebaseAuth();
        setupActivityWidgets();
        setupSettingsList();
        setupFragments();
        getIncomingIntent();
        setupBottomNavigationView();

        // Setup back arrow for navigating back to Profile Activity
        ImageView backToProfile = findViewById(R.id.back_to_profile);
        backToProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Navigating back to Profile Activity");
                finish();
            }
        });
    }

    private void getIncomingIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(getString(R.string.selected_image)) ||
                intent.hasExtra(getString(R.string.selected_bitmap))) {
            // if there is an imageUrl attached as an extra, then it was chosen from the gallery/photo fragment
            Log.d(TAG, "getIncomingIntent: New incoming image url");

            if (intent.getStringExtra(getString(R.string.return_to_fragment))
                    .equals(getString(R.string.edit_profile_fragment))) {

                if (intent.hasExtra(getString(R.string.selected_image))) {
                    // Set the new profile picture
                    FirebaseMethods firebaseMethods = new FirebaseMethods(AccountSettingsActivity.this);
                    firebaseMethods.uploadNewPhoto(
                            getString(R.string.profile_photo),
                            null,
                            0,
                            intent.getStringExtra(getString(R.string.selected_image)),
                            null
                    );
                } else if (intent.hasExtra(getString(R.string.selected_bitmap))) {
                    // Set the new profile picture
                    FirebaseMethods firebaseMethods = new FirebaseMethods(AccountSettingsActivity.this);
                    firebaseMethods.uploadNewPhoto(
                            getString(R.string.profile_photo),
                            null,
                            0,
                            null,
                            (Bitmap) intent.getParcelableExtra(getString(R.string.selected_bitmap))
                    );
                }

            }
        }


    }

    private void setupFragments() {
        pagerAdapter = new SectionStatePagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(new EditProfileFragment(), getString(R.string.text_edit_profile));  // Fragment 0
        pagerAdapter.addFragment(new ChangePasswordFragment(), getString(R.string.text_change_password));  // Fragment 1
        pagerAdapter.addFragment(new BlockedUsersFragment(), getString(R.string.text_blocked_users));  // Fragment 2

    }

    private void setupSettingsList() {
        Log.d(TAG, "setupSettingsList: Setting Up the Options ListView");
        ListView listView = findViewById(R.id.lv_account_settings);

        ArrayList<String> options = new ArrayList<>();
        options.add(getString(R.string.text_edit_profile));  // Fragment 0
        options.add(getString(R.string.text_change_password));  // Fragment 1
        options.add(getString(R.string.text_blocked_users));  // Fragment 2
        options.add(getString(R.string.text_activate_blog));  // Fragment 3
        options.add(getString(R.string.text_private_account));  // Fragment 4
        options.add(getString(R.string.text_signout));  // Fragment 5

        ArrayAdapter adapter = new ArrayAdapter(context, android.R.layout.simple_list_item_1, options);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemClick: Navigating to the fragment number: " + i + " : " + l);
                if (i < 5) {
                    setViewPager(i);
                } else if (i == 5) {
                    mAuth.signOut();
                    finish();
                }
            }
        });
    }

    public void setViewPager(int fragmentNumber) {
        relativeLayout.setVisibility(View.GONE);
        Log.d(TAG, "setViewPager: Navigating to fragment Number " + fragmentNumber);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(fragmentNumber);
    }

    private void setupActivityWidgets() {
        viewPager = findViewById(R.id.container);
        relativeLayout = findViewById(R.id.relLayout1);
    }

    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: Setting Up Bottom Navigation view");

        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottom_nav_view_bar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(context, this, bottomNavigationViewEx);

        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    /*
    ******************************** Firebase ************************************
     */
    private void setupFirebaseAuth() {
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged: signed_in" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged: signed_out");

                    Log.d(TAG, "onAuthStateChanged: Navigating back to login activity after signout");
                    Intent intent = new Intent(context, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


}
