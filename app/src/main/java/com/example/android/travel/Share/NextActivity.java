package com.example.android.travel.Share;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.travel.Profile.BlockedUsersFragment;
import com.example.android.travel.Profile.ChangePasswordFragment;
import com.example.android.travel.Profile.EditProfileFragment;
import com.example.android.travel.R;
import com.example.android.travel.Utils.FirebaseMethods;
import com.example.android.travel.Utils.SectionStatePagerAdapter;
import com.example.android.travel.Utils.UniversalImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class NextActivity extends AppCompatActivity {

    private static final String TAG = "NextActivity";
    private Context context;

    // vars
    private String append = "file:/";
    private int imageCount = 0;
    private String imgUrl;
    private Bitmap bitmap;
    private Intent intent;

    private SectionStatePagerAdapter pagerAdapter;
    @SuppressLint("StaticFieldLeak")
    public static ViewPager viewPager;
    @SuppressLint("StaticFieldLeak")
    public static RelativeLayout relativeLayout;

    // widgets
    private EditText caption;
    public static ProgressBar progressBarImgShare;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);

        context = NextActivity.this;

        mFirebaseMethods = new FirebaseMethods(context);

        setupFirebaseAuth();
        setupActivityWidgets();
        setupOptionsList();
        setupFragments();

        ImageView backArrow = findViewById(R.id.iv_back_arrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: back to gallery fragment");
                finish();
            }
        });

        TextView share = findViewById(R.id.tv_share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: navigating to the final share screen");
                // Upload the image to firebase
                Toast.makeText(context, "Attempting to upload new photo", Toast.LENGTH_SHORT).show();

                // from gallery
                if (intent.hasExtra(getString(R.string.selected_image))) {
                    imgUrl = intent.getStringExtra(getString(R.string.selected_image));
                    mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo), String.valueOf(caption.getText()), imageCount, imgUrl, null);
                }
                // form camera
                else if (intent.hasExtra(getString(R.string.selected_bitmap))) {
                    bitmap = intent.getParcelableExtra(getString(R.string.selected_bitmap));
                    mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo), String.valueOf(caption.getText()), imageCount, null, bitmap);
                }


            }
        });

        setImage();
    }

    private void setupFragments() {
        pagerAdapter = new SectionStatePagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(new LocationFragment(), getString(R.string.text_location));  // Fragment 0

    }

    private void setupOptionsList() {
        Log.d(TAG, "setupOptionsList: Setting Up the Options ListView");
        ListView listView = findViewById(R.id.lv_next_activity);

        ArrayList<String> options = new ArrayList<>();
        options.add(getString(R.string.text_location));  // Fragment 0
        ArrayAdapter adapter = new ArrayAdapter(context, android.R.layout.simple_list_item_1, options);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                setViewPager(i);
            }
        });
    }

    private void setViewPager(int fragmentNumber) {
        relativeLayout.setVisibility(View.GONE);
        Log.d(TAG, "setViewPager: Navigating to fragment Number " + fragmentNumber);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(fragmentNumber);
    }

    private void setupActivityWidgets() {
        viewPager = findViewById(R.id.container);
        relativeLayout = findViewById(R.id.relLayout1);
        caption = findViewById(R.id.caption);
        progressBarImgShare = findViewById(R.id.progress_image_share);
        progressBarImgShare.setVisibility(View.GONE);
    }

    private void someMethod() {
        /*
            Step 1 :
            Create a data model for photos
            Step 2 :
            Add properties to the photo object (caption, date, imageUrl, photo_id, tags, user_id)
            Step 3 :
            Count the number of photos that user already has
            Step 4 :
                a) Upload the photos to Firebase Storage
                b) insert into 'photos' node
                c) insert into 'user_photos' node
         */
    }

    /**
     * Gets the image url from the incoming intent and displays the chosen image
     */
    private void setImage() {
        intent = getIntent();
        ImageView image =  findViewById(R.id.image_share);

        // from gallery
        if (intent.hasExtra(getString(R.string.selected_image))) {
            imgUrl = intent.getStringExtra(getString(R.string.selected_image));
            Log.d(TAG, "setImage: got new image url: " + imgUrl);
            UniversalImageLoader.setImage(imgUrl, image, null, append);
        }
        // form camera
        else if (intent.hasExtra(getString(R.string.selected_bitmap))) {
            bitmap = intent.getParcelableExtra(getString(R.string.selected_bitmap));
            Log.d(TAG, "setImage: got new bitmap");
            image.setImageBitmap(bitmap);
        }
    }

    /*
    ******************************** Firebase ************************************
     */

    private void setupFirebaseAuth() {
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        Log.d(TAG, "onDataChange: image count: " + imageCount);

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
                imageCount = mFirebaseMethods.getImageCount(dataSnapshot);
                Log.d(TAG, "onDataChange: image count: " + imageCount);
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
