package com.example.android.travel.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.travel.R;
import com.example.android.travel.models.Photo;
import com.example.android.travel.models.UserAccountSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by user on 07-04-2018.
 */

public class ViewPostFragment extends Fragment {

    private static final String TAG = "ViewPostFragment";
    private Context context;

    public ViewPostFragment() {
        super();
        setArguments(new Bundle());
    }

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;
    private UserAccountSettings userAccountSettings;

    // widgets
    private SquareImageView postImage;
    private BottomNavigationViewEx bottomNavigationViewEx;
    private TextView  backLabel1, caption, username, timestamp;
    private ImageView backArrow, ellipses, heartRed, heartWhite, profileImage;

    // vars
    private Photo photo;
    private int activityNumber = 0;
    private String phtotUsername, photoUrl;

    @SuppressLint("WrongViewCast")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_post, container, false);
        context = getActivity();
        postImage = view.findViewById(R.id.post_image);
        bottomNavigationViewEx = view.findViewById(R.id.bottom_nav_view_bar);
        backArrow = view.findViewById(R.id.ic_back_arrow);
        backLabel1 = view.findViewById(R.id.back_side_label);
        caption = view.findViewById(R.id.image_caption);
        username = view.findViewById(R.id.view_post_username);
        timestamp = view.findViewById(R.id.image_time_posted);
        ellipses = view.findViewById(R.id.view_post_menu);
        heartRed = view.findViewById(R.id.image_heart_red);
        heartWhite = view.findViewById(R.id.image_heart);
        profileImage = view.findViewById(R.id.top_bar_profile_image);

        try {
            photo = getPhotoFromBundle();
            UniversalImageLoader.setImage(photo.getImage_path(), postImage, null, "");
            activityNumber = getActivityNumFromBundle();
            setupFirebaseAuth();
            setupBottomNavigationView();
            getPhotoDetails();
        } catch (NullPointerException e) {
            Log.d(TAG, "onCreateView: NullPointerException: " + e.getMessage());
        }
        return view;
    }

    

    private void getPhotoDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_user_account_settings))
                .orderByChild(photo.getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    userAccountSettings = singleSnapshot.getValue(UserAccountSettings.class);
                }
                Log.d(TAG, "onDataChange: userAccountSettings: " + userAccountSettings);
                setupWidgets();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled");
            }
        });
    }

    private void setupWidgets() {
        String timestampDiff = getTimestampDifference();
        if (!timestampDiff.equals("0")) {
            timestamp.setText(timestampDiff + " DAYS AGO");
        } else {
            timestamp.setText("TODAY");
        }
        UniversalImageLoader.setImage(userAccountSettings.getProfile_photo(), profileImage, null, "");
        username.setText(userAccountSettings.getUsername());
    }

    /**
     * @return a string representing the number of days ago the post was made
     */
    private String getTimestampDifference() {
        Log.d(TAG, "getTimestampDifference: getting timestamp difference");

        String difference =  "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CHINA);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimestamp = photo.getDate_created();
        try {
            timestamp = sdf.parse(photoTimestamp);
            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24 )));
        } catch (ParseException e) {
            Log.e(TAG, "getTimestampDifference: ParseException: " + e.getMessage());
            difference = "0";
        }
        return difference;
    }

    /**
     * Retrieve the activity number from incoming bundle from profileActivity interface
     * @return
     */
    private int getActivityNumFromBundle() {
        Log.d(TAG, "getActivityNumFromBundle: arguments: " + getArguments());

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getInt(getString(R.string.activity_number));
        } else {
            return  0;
        }
    }

    /**
     * Retrieve the photo from incoming bundle from profileActivity interface
     * @return
     */
    private Photo getPhotoFromBundle() {
        Log.d(TAG, "getPhotoFromBundle: arguments: " + getArguments());

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getParcelable(getString(R.string.photo));
        } else {
            return  null;
        }
    }

    /**
     * Bottom navigation setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: Setting up Bottom Navigation View");
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(context, getActivity(), bottomNavigationViewEx);

        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(activityNumber);
        menuItem.setChecked(true);
    }

    /*
    ******************************** Firebase ************************************
     */

    private void setupFirebaseAuth() {
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

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
