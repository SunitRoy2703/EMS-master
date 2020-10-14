package com.sunit.ems.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sunit.ems.R;

import java.util.Arrays;
import java.util.List;

/**
 * LoginActivity for Users.
 * Implemented using FireBaseUI And FireBaseAuth.
 * Checking for previous login if found UserData Activity fires else the login intent is fired.
 * Finally the listener listens to a change in AuthState and fires the next activity.
 */
public class LoginActivity extends AppCompatActivity {
    //FireBase imports
    FirebaseAuth mAuth;
    List<AuthUI.IdpConfig> providers;
    final int RC_SIGN_IN=12;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        mAuth=FirebaseAuth.getInstance();
        //Current FireBase user.
        currentUser =mAuth.getCurrentUser();
        //List of providers for authentication
        providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build()
        );
        checkUser();

//Listener for authentication changes
        FirebaseAuth.AuthStateListener firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth auth) {
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    Intent loginIntent = new Intent(LoginActivity.this, UserData.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                    Animatoo.animateFade(LoginActivity.this);
                    finish();
                }
            }
        };
        mAuth.addAuthStateListener(firebaseAuthListener);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {

                currentUser = FirebaseAuth.getInstance().getCurrentUser();
            } else {

            }
        }

    }

    //Method to check for previous login
    private void checkUser()
    {
        if(currentUser != null) {
            startActivity(new Intent(LoginActivity.this, UserData.class));
            Animatoo.animateSlideUp(LoginActivity.this);
        }


        else {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .setIsSmartLockEnabled(false)
                            .setTheme(R.style.FullscreenTheme)
                            .build(),
                    RC_SIGN_IN);


        }


    }



}
