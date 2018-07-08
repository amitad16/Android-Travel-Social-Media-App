package com.example.android.travel.Login;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.travel.Home.HomeActivity;
import com.example.android.travel.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private Context context;
    private TextView linkSignup;
    private ProgressBar progressBar;
    private EditText email, password;
    private TextView status;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(TAG, "onCreate: Started Login Activity");

        context = LoginActivity.this;

        progressBar = findViewById(R.id.progressBar);
        email = findViewById(R.id.inputEmail);
        password = findViewById(R.id.inputPassword);
        status = findViewById(R.id.status);
        linkSignup = findViewById(R.id.linkSignup);

        progressBar.setVisibility(View.GONE);
        status.setVisibility(View.GONE);

        linkSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, RegisterActivity.class);
                startActivity(intent);
            }
        });

        setupFirebaseAuth();
        init();
    }

    /*
    ******************************** Firebase ************************************
     */
    // Checks if the string passed is null
    private boolean isStringNull(String string) {
        return string.equals("");
    }

    private void init() {
        Button btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: attempting to login");

                String inputEmail = email.getText().toString().trim();
                String inputPassword = password.getText().toString();

                if (isStringNull(inputEmail) || isStringNull(inputPassword)) {
                    Toast.makeText(context, "Empty Fields", Toast.LENGTH_SHORT).show();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    mAuth.signInWithEmailAndPassword(inputEmail, inputPassword)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "signInWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        updateUI(user);

                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                                        Toast.makeText(context, R.string.auth_failed,
                                                Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                        updateUI(null);
                                    }
                                }
                            });
                }
            }
        });
    }

    private void updateUI(FirebaseUser user) {
        // If the user is logged in then navigate to 'HomeFragment' and call 'finish()'
        if (user != null) {
            try {
                if (user.isEmailVerified()) {
                    Toast.makeText(context, R.string.auth_success,
                            Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onComplete: success, email verified");
                    Intent intent = new Intent(context, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(context, "Email is not verified \n Check your inbox", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                    mAuth.signOut();
                }
            } catch (NullPointerException e) {
                Log.e(TAG, "onComplete: NullPointerException" + e.getMessage());
            }
        }
    }

    /*
    Setup Firebase auth object
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
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
