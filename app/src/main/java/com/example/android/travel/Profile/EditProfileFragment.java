package com.example.android.travel.Profile;

import android.content.Context;
import android.content.Intent;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.travel.Home.HomeActivity;
import com.example.android.travel.R;
import com.example.android.travel.Share.GalleryFragment;
import com.example.android.travel.Share.ShareActivity;
import com.example.android.travel.Utils.FirebaseMethods;
import com.example.android.travel.Utils.UniversalImageLoader;
import com.example.android.travel.dialog.ConfirmPasswordDialog;
import com.example.android.travel.models.User;
import com.example.android.travel.models.UserAccountSettings;
import com.example.android.travel.models.UserSettings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by user on 03-02-2018.
 */

public class EditProfileFragment extends Fragment implements ConfirmPasswordDialog.OnConfirmPasswordListener {

    @Override
    public void onConfirmPassword(String password) {
        Log.d(TAG, "onConfirmPassword: got the password");

        final FirebaseUser user = mAuth.getCurrentUser();
        Log.d(TAG, "onConfirmPassword: Current User: " + user + " : " + user.getEmail());

        // Get auth credentials from the user for re-authentication. The example below shows
        // email and password credentials but there are multiple possible providers,
        // such as GoogleAuthProvider or FacebookAuthProvider.
        AuthCredential credential = EmailAuthProvider
                .getCredential(user.getEmail(), password);

        // ************* Prompt the user to re-provide their sign-in credentials
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User re-authenticated.");
                            // *************** Check to see if email is not already present in the database
                            mAuth.fetchProvidersForEmail(email.getText().toString()).addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                                @Override
                                public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                                    if (task.isSuccessful()) {
                                        try {
                                            if (task.getResult().getProviders().size() == 1) {
                                                Log.d(TAG, "onComplete: That email is already in use");
                                                Toast.makeText(getActivity(), "Email already in use", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Log.d(TAG, "onComplete: Email is available");

                                                // *************** The email is available, so update it
                                                user.updateEmail(email.getText().toString())
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.d(TAG, "onComplete: User email address updated");
                                                                    Toast.makeText(getActivity(), "Email updated", Toast.LENGTH_SHORT).show();
                                                                    mFirebaseMethods.updateEmail(email.getText().toString());
                                                                }
                                                            }
                                                        });
                                            }
                                        } catch (NullPointerException e) {
                                            Log.e(TAG, "onComplete: NullPointerException: " + e.getMessage());
                                        }
                                    }
                                }
                            });
                        } else {
                            Log.d(TAG, "User re-authentication failed.");
                        }
                    }
                });
    }

    private static final String TAG = "EditProfileFragment";
    private Context context;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;
    private String userID;

    // Edit profile fragment widgets
    private EditText displayName, userName, website, description, email, phoneNumber;
    private TextView changeProfilePhoto;
    private CircleImageView profilePhoto;
    public static ProgressBar photoProgressBar;

    //vars
    private UserSettings mUserSettings;
//    private User mUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        profilePhoto = view.findViewById(R.id.profile_image);
        changeProfilePhoto = view.findViewById(R.id.change_profile_image);
        displayName = view.findViewById(R.id.display_name);
        userName = view.findViewById(R.id.display_username);
        website = view.findViewById(R.id.display_website);
        description = view.findViewById(R.id.display_bio);
        email = view.findViewById(R.id.display_email);
        phoneNumber = view.findViewById(R.id.display_phone);
        photoProgressBar = view.findViewById(R.id.progress_profile_image);
        photoProgressBar.setVisibility(View.GONE);

        context = getActivity();

        mFirebaseMethods = new FirebaseMethods(context);

        setupFirebaseAuth();

        // Back Arrow for navigating back to "ProfileActivity"
        ImageView backArrow = view.findViewById(R.id.back_to_account_settings);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Navigating back to the profile activity");
                getActivity().finish();
            }
        });

        ImageView checkmark = view.findViewById(R.id.save_changes);
        checkmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Attempting to save the cahges");
                saveProfileSettings();
            }
        });

        return view;
    }

    /**
     * Retrieves the data contained in the widgets and submits it to the database
     * Before doing so it checks to make sure that the username is unique
     */
    private void saveProfileSettings() {
        final String newDisplayName = displayName.getText().toString();
        final String newUsername = userName.getText().toString();
        final String newWebsite = website.getText().toString();
        final String newDescription = description.getText().toString();
        final String newEmail = email.getText().toString();
        final long newPhoneNumber = Long.parseLong(phoneNumber.getText().toString());

        // Case 1: if user made a change to their username
        if (!mUserSettings.getUser().getUsername().equals(newUsername)) {
            checkIfUsernameExists(newUsername);
        }
        // Case 2: if a user made a change to their email
        if (!mUserSettings.getUser().getEmail().equals(newEmail)) {
            // step 1: Re-authenticate
            //          -Confirm the password and email
            ConfirmPasswordDialog confirmPasswordDialog = new ConfirmPasswordDialog();
            confirmPasswordDialog.show(getFragmentManager(), getString(R.string.confirm_password_dialog));
            confirmPasswordDialog.setTargetFragment(EditProfileFragment.this, 1);

            // step 2: Check if email already registered
            //          -'fetchProvidersForEmail(String email)'
            // step 3: change the email
            //          -Submit the new email to the database and authentication
        }

         /*
         * Changes rest of the user settings
         */
        if (!mUserSettings.getSettings().getDisplay_name().equals(newDisplayName)) {
            // Update display name
            mFirebaseMethods.updateUserAccountSettings(newDisplayName, null, null, 0);
        }
        if (!mUserSettings.getSettings().getWebsite().equals(newWebsite)) {
            // Update Website
            mFirebaseMethods.updateUserAccountSettings(null, newWebsite, null, 0);
        }
        if (!mUserSettings.getSettings().getDescription().equals(newDescription)) {
            // Update Description
            mFirebaseMethods.updateUserAccountSettings(null, null, newDescription, 0);
        }
    }

    /**
     * Check if @param username already exists in database
     * @param username
     */
    private void checkIfUsernameExists(final String username) {
        Log.d(TAG, "checkIfUsernameExists: Checking if: " + username + " already exists.");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_username))
                .equalTo(username);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // Add the username
                    mFirebaseMethods.updateUsername(username);
                    Toast.makeText(context, "Saved Username: " + username, Toast.LENGTH_SHORT).show();
                }
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()) {
                    if (singleSnapshot.exists()) {
                        Log.d(TAG, "checkIfUsernameExists: FOUND A MATCH: " + singleSnapshot.getValue(User.class).getUsername());
                        Toast.makeText(context, "That username already exists", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setProfileWidgets(UserSettings userSettings) {
        Log.d(TAG, "setProfileWidgets: setting widgets with retrieving data from firebase database: " + userSettings.toString());

        mUserSettings = userSettings;
        UserAccountSettings settings = userSettings.getSettings();

        UniversalImageLoader.setImage(settings.getProfile_photo(), profilePhoto, null, "");

        displayName.setText(settings.getDisplay_name());
        userName.setText(settings.getUsername());
        website.setText(settings.getWebsite());
        description.setText(settings.getDescription());
        email.setText(userSettings.getUser().getEmail());
        phoneNumber.setText(String.valueOf(userSettings.getUser().getPhone_number()));

        changeProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ShareActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 26,84,35,456
                getActivity().startActivity(intent);
                getActivity().finish();
            }
        });
    }

    /*
    ******************************** Firebase ************************************
     */

    private void setupFirebaseAuth() {
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        userID = mAuth.getCurrentUser().getUid();

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
