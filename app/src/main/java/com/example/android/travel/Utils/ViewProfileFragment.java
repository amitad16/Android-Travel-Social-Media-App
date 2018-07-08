package com.example.android.travel.Utils;

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
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.travel.Profile.AccountSettingsActivity;
import com.example.android.travel.Profile.ProfileActivity;
import com.example.android.travel.R;
import com.example.android.travel.models.Likes;
import com.example.android.travel.models.Photo;
import com.example.android.travel.models.User;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by user on 26-03-2018.
 */

public class ViewProfileFragment extends Fragment {

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

    // widgets
    private TextView posts, followers, following, displayName, username, location, follow, unfollow;
    private ProgressBar progressBar;
    private CircleImageView profilePhoto;
    private GridView gridView;
    private ImageView backArrow;
    private BottomNavigationViewEx bottomNavigationViewEx;

    // vars
    private User user;
    private int followersCount = 0;
    private int followingCount = 0;
    private int postCount = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_profile, container, false);

        username = view.findViewById(R.id.profile_toolbar_username);
        displayName = view.findViewById(R.id.display_name);
        location = view.findViewById(R.id.display_location);
        profilePhoto = view.findViewById(R.id.profile_image);
        posts = view.findViewById(R.id.tvPosts);
        followers = view.findViewById(R.id.tvFollowers);
        following = view.findViewById(R.id.tvFollowing);
        progressBar = view.findViewById(R.id.profile_progress_bar);
        gridView = view.findViewById(R.id.grid_view);
        follow = view.findViewById(R.id.text_follow);
        unfollow = view.findViewById(R.id.text_unfollow);
        backArrow = view.findViewById(R.id.iv_back_arrow);
        bottomNavigationViewEx = view.findViewById(R.id.bottom_nav_view_bar);

        context = getActivity();

        Log.d(TAG, "onCreateView: Started.");

        try {
            user = getUserFromBundle();
            init();
        } catch (NullPointerException e) {
            Log.e(TAG, "onCreateView: NullPointerException: " + e.getMessage());
            Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
            getActivity().getSupportFragmentManager().popBackStack();
        }

        setupBottomNavigationView();
        setupFirebaseAuth();

        isFollowing();
        getFollowingCount();
        getFollowersCount();
        getPostsCount();

        follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Now following username: " + user.getUsername());
                myRef.child(getString(R.string.dbname_following))
                        .child(mAuth.getCurrentUser().getUid())
                        .child(user.getUser_id())
                        .child(getString(R.string.field_user_id))
                        .setValue(user.getUser_id());

                myRef.child(getString(R.string.dbname_followers))
                        .child(user.getUser_id())
                        .child(mAuth.getCurrentUser().getUid())
                        .child(getString(R.string.field_user_id))
                        .setValue(mAuth.getCurrentUser().getUid());

                setFollowing();
            }
        });

        unfollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Now unfollowing username: " + user.getUsername());

                myRef.child(getString(R.string.dbname_following))
                        .child(mAuth.getCurrentUser().getUid())
                        .child(user.getUser_id())
                        .removeValue();

                myRef.child(getString(R.string.dbname_followers))
                        .child(user.getUser_id())
                        .child(mAuth.getCurrentUser().getUid())
                        .removeValue();

                setUnfollowing();
            }
        });


//        setupGridView();

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

    private void init() {
        // set the profile widgets
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference();
        Query query1 = reference1.child(getString(R.string.dbname_user_account_settings))
                .orderByChild(getString(R.string.field_user_id)).equalTo(user.getUser_id());
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found user:" + singleSnapshot.getValue(UserAccountSettings.class).toString());

                    UserSettings settings = new UserSettings();
                    settings.setUser(user);
                    settings.setSettings(singleSnapshot.getValue(UserAccountSettings.class));
                    setProfileWidgets(settings);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // get the users profile photos
        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference();

        Query query2 = reference2
                .child(getString(R.string.dbname_user_photos))
                .child(user.getUser_id());

        query2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Photo> photos = new ArrayList<>();
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
//                    photos.add(singleSnapshot.getValue(Photo.class));
                    Photo photo = new Photo();
                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                    try {
                        photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                        photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                        photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                        photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                        photo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                        photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());

                        List<Likes> likesList = new ArrayList<>();
                        for (DataSnapshot ds : singleSnapshot
                                .child(getString(R.string.field_likes)).getChildren()) {
                            Likes likes = new Likes();
                            likes.setUser_id(ds.getValue(Likes.class).getUser_id());
                            likesList.add(likes);
                        }
                        photo.setLikes(likesList);
                        photos.add(photo);
                    } catch (NullPointerException e) {
                        Log.e(TAG, "onDataChange: NullPointerException: " + e.getMessage());
                    }
                }
                setupImageGrid(photos);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled");
            }
        });
    }

    private void isFollowing() {
        Log.d(TAG, "isFollowing: checking if following this user");
        setUnfollowing();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_following))
                .child(mAuth.getCurrentUser().getUid())
                .orderByChild(getString(R.string.field_user_id)).equalTo(user.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found user:" + singleSnapshot.getValue());

                    setFollowing();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getFollowersCount() {
        Log.d(TAG, "getFollowersCount: getting number of followers");
        followersCount = 0;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_followers))
                .child(user.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found follower:" + singleSnapshot.getValue());
                    followersCount++;
                }
                followers.setText(String.valueOf(followersCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getFollowingCount() {
        Log.d(TAG, "getFollowingCount: getting number of following");
        followingCount = 0;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_following))
                .child(user.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found following: " + singleSnapshot.getValue());
                    followingCount++;
                }
                following.setText(String.valueOf(followingCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getPostsCount() {
        Log.d(TAG, "getPostsCount: getting number of posts");
        postCount = 0;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbname_user_photos))
                .child(user.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found post count :" + singleSnapshot.getValue());
                    postCount++;
                }
                posts.setText(String.valueOf(postCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setFollowing() {
        Log.d(TAG, "setFollowing: Updating UI for following this user");
        follow.setVisibility(View.GONE);
        unfollow.setVisibility(View.VISIBLE);
    }

    private void setUnfollowing() {
        Log.d(TAG, "setFollowing: Updating UI for unfollowing this user");
        follow.setVisibility(View.VISIBLE);
        unfollow.setVisibility(View.GONE);
    }

    private void setCurrentUsersProfile() {
        Log.d(TAG, "setFollowing: Updating UI for this users own profile");
        follow.setVisibility(View.GONE);
        unfollow.setVisibility(View.GONE);
    }

    private void setupImageGrid(final ArrayList<Photo> photos) {
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

    private User getUserFromBundle() {
        Log.d(TAG, "getUserFromBundle: arguements: "+ getArguments());

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getParcelable(getString(R.string.intent_user));
        } else {
            return null;
        }
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

    private void setProfileWidgets(UserSettings userSettings) {
        Log.d(TAG, "setProfileWidgets: setting widgets with data retrieving from firebase database: " + userSettings.toString());

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

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: navigating back");

                getActivity().getSupportFragmentManager().popBackStack();
                getActivity().finish();
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
