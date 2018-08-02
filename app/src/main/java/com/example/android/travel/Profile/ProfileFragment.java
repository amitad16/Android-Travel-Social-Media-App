package com.example.android.travel.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.travel.R;
import com.example.android.travel.Utils.BottomNavigationViewHelper;
import com.example.android.travel.Utils.FirebaseMethods;
import com.example.android.travel.Utils.GridImageAdapter;
import com.example.android.travel.Utils.UniversalImageLoader;
import com.example.android.travel.models.Photo;
import com.example.android.travel.models.UserAccountSettings;
import com.example.android.travel.models.UserSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by user on 26-03-2018.
 */

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    public interface OnGridImageSelectedListener {
        void onGridImageSelected(Photo photo, int activityNumber);
    }

    OnGridImageSelectedListener onGridImageSelectedListener;

    private static final int ACTIVITY_NUM = 4;
    private static final int NUM_GRID_COLUMNS = 3;

    private Context context;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    private TextView posts, followers, following, displayName, username, location;
    private ProgressBar progressBar;
    private CircleImageView profilePhoto;
    private GridView gridView;
    private Toolbar toolbar;
    private ImageView profileMenu;
    private BottomNavigationViewEx bottomNavigationViewEx;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        username = view.findViewById(R.id.profile_toolbar_username);
        displayName = view.findViewById(R.id.display_name);
        location = view.findViewById(R.id.display_location);
        profilePhoto = view.findViewById(R.id.profile_image);
        posts = view.findViewById(R.id.tvPosts);
        followers = view.findViewById(R.id.tvFollowers);
        following = view.findViewById(R.id.tvFollowing);
        progressBar = view.findViewById(R.id.profile_progress_bar);
        gridView = view.findViewById(R.id.grid_view);
        toolbar = view.findViewById(R.id.profile_tool_bar);
        profileMenu = view.findViewById(R.id.profile_menu);
        bottomNavigationViewEx = view.findViewById(R.id.bottom_nav_view_bar);

        context = getActivity();

        mFirebaseMethods = new FirebaseMethods(context);

        Log.d(TAG, "onCreateView: Started.");

        setupBottomNavigationView();
        setupToolBar();

        setupFirebaseAuth();
        setupGridView();

//        Button editProfile = view.findViewById(R.id.btnEditProfile);
//        editProfile.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.d(TAG, "onClick: navigating to " + context.getString(R.string.edit_profile_fragment));
//                Intent intent = new Intent(context, AccountSettingsActivity.class);
//                intent.putExtra(getString(R.string.calling_activity), getString(R.string.profile_activity));
//                startActivity(intent);
//            }
//        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        try {
            onGridImageSelectedListener = (OnGridImageSelectedListener)getActivity();
        } catch (ClassCastException e) {
            Log.e(TAG, "onAttach: ClassCastException" + e.getMessage());
        }
        super.onAttach(context);
    }

    private void setupGridView() {
        Log.d(TAG, "setupGridView: setting up image grid view");

        final ArrayList<Photo> photos = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference
                .child(getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    photos.add(singleSnapshot.getValue(Photo.class));
                }

                // Setup image grid
                int gridWidth = getResources().getDisplayMetrics().widthPixels;
                int imageWidth = gridWidth / NUM_GRID_COLUMNS;
                gridView.setColumnWidth(imageWidth);

                ArrayList<String> imgUrls = new ArrayList<>();
                for (int i = 0; i < photos.size(); i++) {
                    imgUrls.add(photos.get(i).getImage_path());
                }

                GridImageAdapter adapter = new GridImageAdapter(context, R.layout.layout_grid_imageview, "", imgUrls);
                gridView.setAdapter(adapter);

                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        onGridImageSelectedListener.onGridImageSelected(photos.get(i), ACTIVITY_NUM);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled");
            }
        });
    }


    private void setProfileWidgets(UserSettings userSettings) {
        Log.d(TAG, "setProfileWidgets: setting widgets with retrieving data from firebase database: " + userSettings.toString());

//        User user = userSettings.getUser();
        UserAccountSettings settings = userSettings.getSettings();

        UniversalImageLoader.setImage(settings.getProfile_photo(), profilePhoto, null, "");

        displayName.setText(settings.getDisplay_name());
        username.setText(settings.getUsername());
        location.setText(settings.getLocation());
        posts.setText(String.valueOf(settings.getPosts()));
        followers.setText(String.valueOf(settings.getFollowes()));
        following.setText(String.valueOf(settings.getFollowing()));
        progressBar.setVisibility(View.GONE);
    }

    /**
     * Responsible for setting up the profile toolbar
     */
    private void setupToolBar() {

        if (getActivity() != null) {
            ((ProfileActivity)getActivity()).setSupportActionBar(toolbar);
        }

        profileMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Navigating to account settings.");
                Intent intent = new Intent(context, AccountSettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Bottom navigation setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: Setting up Bottom Navigation View");
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(context, getActivity(), bottomNavigationViewEx);

        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
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

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Retrieve user information from database
                setProfileWidgets(mFirebaseMethods.getUserSttings(dataSnapshot));
                // Retrieve images for the user in question
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
