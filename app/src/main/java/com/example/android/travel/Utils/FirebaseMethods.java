package com.example.android.travel.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.android.travel.Home.HomeActivity;
import com.example.android.travel.Profile.AccountSettingsActivity;
import com.example.android.travel.Profile.EditProfileFragment;
import com.example.android.travel.R;
import com.example.android.travel.Share.NextActivity;
import com.example.android.travel.models.Photo;
import com.example.android.travel.models.User;
import com.example.android.travel.models.UserAccountSettings;
import com.example.android.travel.models.UserSettings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by user on 06-02-2018.
 */

public class FirebaseMethods {

    private static final String TAG = "FirebaseMethods";

    // vars
    private double photoUploadProgress = 0;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private StorageReference mStorageReference;
    private String userID;

    private Context mContext;

    public FirebaseMethods(Context context) {
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mContext = context;

        if (mAuth.getCurrentUser() != null){
            userID = mAuth.getCurrentUser().getUid();
        }
    }

    public int getImageCount(DataSnapshot dataSnapshot) {
        int count = 0;
        for (DataSnapshot ds: dataSnapshot
                .child(mContext.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .getChildren()) {
            count++;
        }
        return count;
    }

    public void uploadNewPhoto(String photoType, final String caption, int imageCount, String imgUrl, Bitmap bm) {
        Log.d(TAG, "uploadNewPhoto: attempting to upload a new photo");

        FilePaths filePaths = new FilePaths();
        
        // case 1: new post photo
        if (photoType.equals(mContext.getString(R.string.new_photo))) {
            Log.d(TAG, "uploadNewPhoto: uploading new photo");

            StorageReference storageReference = mStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + userID + "/photo" + (imageCount + 1));

            // Convert image url to bitmap
            if (bm == null) {
                bm = ImageManager.getBitmap(imgUrl);
            }
            byte[] bytes = ImageManager.getByteFromBitmap(bm, 100);

            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri firebaseUrl = taskSnapshot.getDownloadUrl();

                    NextActivity.progressBarImgShare.setVisibility(View.GONE);
                    Toast.makeText(mContext, "Photo upload success", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onSuccess: download url: " + firebaseUrl);

                    // Add the new photo to 'photos' and 'user_photos' node
                    addPhotoToDatabase(caption, firebaseUrl.toString());

                    // Navigate to the main feed so the user can see their photo
                    Intent intent = new Intent(mContext, HomeActivity.class);
                    mContext.startActivity(intent);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: Photo upload failed");
                    NextActivity.progressBarImgShare.setVisibility(View.GONE);
                    Toast.makeText(mContext, "Photo Upload failed", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    NextActivity.progressBarImgShare.setVisibility(View.VISIBLE);
                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    if (progress - 15 > photoUploadProgress) {
                        Toast.makeText(mContext, "Photo Upload progress: " + String.format("%.0f", progress) + "%", Toast.LENGTH_SHORT).show();
                        photoUploadProgress = progress;
                    }

                    Log.d(TAG, "onProgress: Upload progress: " + progress + "% done");
                }
            });

        }
        // case 2: new profile photo
        else if (photoType.equals(mContext.getString(R.string.profile_photo))) {
            Log.d(TAG, "uploadNewPhoto: uploading new profile photo");
            EditProfileFragment.photoProgressBar.setVisibility(View.VISIBLE);

            StorageReference storageReference = mStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + userID + "/profile_photo");

            // Convert image url to bitmap
            if (bm == null) {
                bm = ImageManager.getBitmap(imgUrl);
            }byte[] bytes = ImageManager.getByteFromBitmap(bm, 100);

            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri firebaseUrl = taskSnapshot.getDownloadUrl();

                    EditProfileFragment.photoProgressBar.setVisibility(View.GONE);
                    Toast.makeText(mContext, "Photo upload success", Toast.LENGTH_SHORT).show();

                    // Insert into user_account_settings node
                    setProfilePhoto(firebaseUrl.toString());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: Photo upload failed");
                    EditProfileFragment.photoProgressBar.setVisibility(View.GONE);
                    Toast.makeText(mContext, "Photo Upload failed", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    ((AccountSettingsActivity)mContext).setViewPager(
                            ((AccountSettingsActivity)mContext).pagerAdapter
                                    .getFragmnetNumber(mContext.getString(R.string.edit_profile_fragment))
                    );
                    EditProfileFragment.photoProgressBar.setVisibility(View.VISIBLE);
                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    if (progress - 15 > photoUploadProgress) {
                        Toast.makeText(mContext, "Photo Upload progress: " + String.format("%.0f", progress) + "%", Toast.LENGTH_SHORT).show();
                        photoUploadProgress = progress;
                    }

                    Log.d(TAG, "onProgress: Upload progress: " + progress + "% done");
                }
            });

        }

    }

    private void setProfilePhoto(String url) {
        Log.d(TAG, "setProfilePhoto: Setting new profile image");

        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(mAuth.getCurrentUser().getUid())
                .child(mContext.getString(R.string.profile_photo))
                .setValue(url);
    }

    private String getTimeStamp() {
        SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CHINA);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
        return sdf.format(new Date());

    }

    private void addPhotoToDatabase(String caption, String url) {
        Log.d(TAG, "addPhotoToDatabase: Adding photo to database");

        String tags = StringManipulation.getTags(caption);
        String newPhotoKey = myRef.child(mContext.getString(R.string.dbname_photos)).push().getKey();
        Photo photo = new Photo();
        photo.setCaption(caption);
        photo.setDate_created(getTimeStamp());
        photo.setImage_path(url);
        photo.setTags(tags);
        photo.setUser_id(mAuth.getCurrentUser().getUid());
        photo.setPhoto_id(newPhotoKey);

        // Insert into database
        myRef.child(mContext.getString(R.string.dbname_user_photos))
                .child(mAuth.getCurrentUser().getUid())
                .child(newPhotoKey)
                .setValue(photo);
        myRef.child(mContext.getString(R.string.dbname_photos))
                .child(newPhotoKey)
                .setValue(photo);

    }

    /**
     * Update 'user_account_settings' node for the current user
     * @param displayName
     * @param website
     * @param description
     * @param phoneNumber
     */
    public void updateUserAccountSettings(String displayName, String website, String description, long phoneNumber) {
        Log.d(TAG, "updateUserAccountSettings: Updating user information");

        if (displayName != null) {
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_display_name))
                    .setValue(displayName);
        }
        if (website != null) {
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_website))
                    .setValue(website);
        }
        if (description != null) {
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_description))
                    .setValue(description);
        }
//        if (phoneNumber != 0) {
//            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
//                    .child(userID)
//                    .child(mContext.getString(R.string.field_phone_number))
//                    .setValue(phoneNumber);
//        }

        Toast.makeText(mContext, "Updated Profile", Toast.LENGTH_SHORT).show();
    }

    /**
     * Update the username in the 'users' and 'user_account_settings' node
     * @param username
     */
    public void updateUsername(String username) {
        Log.d(TAG, "updateUsername: updating username to: " + username);

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);

        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);
    }

    /**
     * Update the email in the 'users' node
     * @param email
     */
    public void updateEmail(String email) {
        Log.d(TAG, "updateEmail: updating email to: " + email);

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .child(mContext.getString(R.string.field_email))
                .setValue(email);
    }

    /**
     * Checks if username entered during registration already exists or not
     * @param username username value
     * @param dataSnapshot current data in database
     * @return true if exists, else false
     */
//    public boolean checkIfUsernameExists(String username, DataSnapshot dataSnapshot) {
//        Log.d(TAG, "checkIfUsernameExists: Check if username: " + username + " already exists");
//
//        User user = new User();
//
//        Log.d(TAG, "checkIfUsernameExists: *************: " + userID + " ****************: " + user);
//        for (DataSnapshot ds: dataSnapshot.child(userID).getChildren()) {
//            Log.d(TAG, "checkIfUsernameExists: datasnapshot: " + ds);
//
//            user.setUsername(ds.getValue(User.class).getUsername());
//            Log.d(TAG, "checkIfUsernameExists: username: " + user.getUsername());
//
//            if (StringManipulation.expandUsername(user.getUsername()).equals(username)) {
//                Log.d(TAG, "checkIfUsernameExists: FOUND A MATCH: " + user.getUsername());
//                return true;
//            }
//        }
//        return false;
//    }

    /**
     * Register a new email and password to Firebase Authentication
     * @param email
     * @param password
     */
    public void registerNewEmail(final String email, String password) {
        Log.d(TAG, "registerNewEmail: ");
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "onComplete: create new user with email and password");
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");

                            // Send verification email
                            sendVerificationEmail();
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                userID = user.getUid();
                                Log.d(TAG, "onComplete: Authstate Changed : " + user + "*********" + userID);
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(mContext, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                            } else {
                                Toast.makeText(mContext, "Couldn't send verfication email.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    /**
     * Add info to the uses node
     * Add info to the users_account_settings node
     * @param email
     * @param username
     * @param description
     * @param profile_photo
     */
    public void addNewUser(String email, String username, String description, String profile_photo) {
        User user = new User(userID, 1, email, StringManipulation.condenseUsername(username));

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .setValue(user);

        UserAccountSettings settings = new UserAccountSettings(
                description,
                username,
                0,
                0,
                0,
                profile_photo,
                StringManipulation.condenseUsername(username),
                "",
                "",
                userID
        );

        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .setValue(settings);

    }

    public UserSettings getUserSttings(DataSnapshot dataSnapshot) {
        Log.d(TAG, "getUserAccountSettings: Retrieving user account settings form firebase");

        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();

        for (DataSnapshot ds: dataSnapshot.getChildren()) {

            // user_account_settings node
            if (ds.getKey().equals(mContext.getString(R.string.dbname_user_account_settings))) {
                Log.d(TAG, "getUserAccountSettings: datasnapshot: " + ds);

                try {
                    settings.setDisplay_name(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getDisplay_name()
                    );
                    settings.setUsername(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getUsername()
                    );
                    settings.setLocation(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getLocation()
                    );
                    settings.setWebsite(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getWebsite()
                    );
                    settings.setDescription(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getDescription()
                    );
                    settings.setProfile_photo(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getProfile_photo()
                    );
                    settings.setPosts(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getPosts()
                    );
                    settings.setFollowing(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getFollowing()
                    );
                    settings.setFollowes(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getFollowes()
                    );

                    Log.e(TAG, "getUserAccountSettings: Retrieved user_account_settings information: " + settings.toString());

                } catch (NullPointerException e) {
                    Log.e(TAG, "getUserAccountSettings: Null point exception : " + e.getMessage());
                }
            }

            // user_account_settings node
            if (ds.getKey().equals(mContext.getString(R.string.dbname_users))) {
                Log.d(TAG, "getUserAccountSettings: datasnapshot: " + ds);

                try {
                    user.setUsername(
                            ds.child(userID)
                                    .getValue(User.class)
                                    .getUsername()
                    );
                    user.setEmail(
                            ds.child(userID)
                                    .getValue(User.class)
                                    .getEmail()
                    );
                    user.setPhone_number(
                            ds.child(userID)
                                    .getValue(User.class)
                                    .getPhone_number()
                    );
                    user.setUser_id(
                            ds.child(userID)
                                    .getValue(User.class)
                                    .getUser_id()
                    );

                    Log.e(TAG, "getUserAccountSettings: Retrieved users information: " + user.toString());

                } catch (NullPointerException e) {
                    Log.e(TAG, "getUserAccountSettings: Null point exception : " + e.getMessage());
                }
            }
        }

        return new UserSettings(user, settings);

    }
}
