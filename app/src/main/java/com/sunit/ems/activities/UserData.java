package com.sunit.ems.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.llollox.androidprojects.compoundbuttongroup.CompoundButtonGroup;
import com.sunit.ems.EMS;
import com.sunit.ems.R;
import com.sunit.ems.ViewDialog;
import com.sunit.ems.models.Employee;

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import in.codeshuffle.typewriterview.TypeWriterView;

/**
 * UserData class is responsible handling user data, both uploading new user data and
 * fetching old users data from the server.
 * Finally the application level global variables using following setters.
 */
public class UserData extends AppCompatActivity {


    @BindView(R.id.typeWriterView)
    TypeWriterView typeWriterView;
    @BindView(R.id.designation)
    EditText designation;
    @BindView(R.id.number)
    EditText number;
    @BindView(R.id.textInputLayout)
    TextInputLayout textInputLayout;
    @BindView(R.id.textInputLayoutTwo)
    TextInputLayout textInputLayoutTwo;
    @BindView(R.id.departments)
    CardView linearLayout;
    @BindView(R.id.department)
    CompoundButtonGroup compoundButtonGroup;
    @BindView(R.id.save)
    LinearLayout save;

    StringBuilder nameString;
    String name,email,phone,post,department;
    boolean isHr=false;
    //FireBase imports
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    private FirebaseFirestore db;

    EMS ems;
    ViewDialog viewDialog;
    Activity activity;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userdata);
        getSupportActionBar().hide();
        ButterKnife.bind(this);
        activity=this;
        viewDialog=new ViewDialog(this);
        viewDialog.showDialog();
        designation.setVisibility(View.INVISIBLE);
        textInputLayout.setVisibility(View.INVISIBLE);
        textInputLayoutTwo.setVisibility(View.INVISIBLE);
        linearLayout.setVisibility(View.INVISIBLE);
        save.setVisibility(View.INVISIBLE);
        db = FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();
        ems=(EMS) getApplicationContext();

        email=currentUser.getEmail();

        //Check for new user or existing user
        checkUser();

        //EditText listeners for validation purpose
        number.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < 1) {
                    textInputLayout.setErrorEnabled(true);
                    textInputLayout.setError("Can't be empty !");
                    textInputLayoutTwo.setVisibility(View.INVISIBLE);
                }

                if (s.length() > 1) {
                    textInputLayout.setErrorEnabled(true);
                    textInputLayout.setError("Valid Number required");
                    textInputLayoutTwo.setVisibility(View.INVISIBLE);
                }

                if (s.length() == 10) {
                    textInputLayout.setError(null);
                    textInputLayout.setErrorEnabled(false);
                    textInputLayoutTwo.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        designation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < 1) {
                    textInputLayoutTwo.setErrorEnabled(true);
                    textInputLayoutTwo.setError("Can't be empty !");
                    linearLayout.setVisibility(View.INVISIBLE);
                }

                if (s.length() > 0) {
                    textInputLayoutTwo.setError(null);
                    textInputLayoutTwo.setErrorEnabled(false);
                    linearLayout.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //Group option for departments
        compoundButtonGroup.setOnButtonSelectedListener(new CompoundButtonGroup.OnButtonSelectedListener() {
            @Override
            public void onButtonSelected(int position, String value, boolean isChecked) {
                System.out.println(value + " " + position);
                //If user has selected the HR option
                if (value.equals("HR")) {
                    //global isHR true
                    isHr = true;
                    //Subscribe to the HR topic to receive Day-off requests
                    FirebaseMessaging.getInstance().subscribeToTopic("HR");

                }
                //If user has selected any other option
                if (!value.equals("HR")) {
                    //global isHR false
                    isHr = false;
                    //UnSubscribe to the HR topic to ignore HR notifications
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("HR");
                }
                save.setVisibility(View.VISIBLE);
                department=value;

            }
        });

        //saving all the data in database
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(viewDialog ==null)
                { viewDialog=new ViewDialog(activity);}
                viewDialog.showDialog();
                String name=currentUser.getDisplayName();
                phone=number.getText().toString();
                post=designation.getText().toString();
                final int random = new Random().nextInt(1000) + 10;
                final int randomOne = new Random().nextInt(100) + 100;
                final int randomTwo = new Random().nextInt(1000) + 900;
                String id=""+random+randomOne+randomTwo;
                ems.setName(name);
                ems.setDepartment(department);
                ems.setDesignation(post);
                ems.setPhone(phone);
                ems.setLeaves(12);
                ems.setEmail(email);
                ems.setIsHr(isHr);
                ems.setPhotoUrl("");
                FirebaseInstanceId.getInstance().getInstanceId()
                        .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                            @Override
                            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                if (!task.isSuccessful()) {

                                    return;
                                }

                                // Get new Instance ID token for notification
                                //and saving it into database for device-to-device notifications
                                String token = task.getResult().getToken();
                                db.collection("employees")
                                        .document(ems.getEmail())
                                        .update("token", token);

                            }
                        });
                //Creating an Employee object using POJO class and pushing it into database
                Employee employee=new
                        Employee(id,"",name,email,phone,department,post,12,isHr);
                db.collection("employees").document(email).set(employee)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                viewDialog.hideDialog();
                                Intent loginIntent = new Intent(UserData.this, MainActivity.class);
                                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(loginIntent);
                                Animatoo.animateSwipeLeft(UserData.this);
                                finish();
                            }
                        });
            }
        });

    }



    //Method for newusers, taking data from users
    private void newUser()
    {
        nameString=new StringBuilder("Hey !  ");
        name=nameString.toString();
        typeWriterView.setDelay(100);
        typeWriterView.animateText(name);
        typeWriterView.setWithMusic(true);
        final int splash_length = 2000;
        new Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        designation.setVisibility(View.VISIBLE);
                        textInputLayout.setVisibility(View.VISIBLE);
                    }
                }                , splash_length);



}

    private void checkUser()
    {


        DocumentReference docIdRef = db.collection("employees").document(email);

        docIdRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        //User present in the database, initialize the global variables from database
                        ems.setName(document.getString("name"));
                        ems.setDepartment(document.getString("department"));
                        ems.setDesignation(document.getString("designation"));
                        ems.setPhone(document.getString("phone"));
                        ems.setLeaves(document.getLong("leaves").intValue());
                        ems.setEmail(document.getString("email"));
                        ems.setIsHr(document.getBoolean("isHr"));
                        ems.setPhotoUrl(document.getString("photoUrl"));
                        Intent loginIntent = new Intent(UserData.this,MainActivity.class);
                        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(loginIntent);
                        Animatoo.animateInAndOut(UserData.this);
                        finish();

                    } else {
                        //else hiding the viewDialog if present and calling the newUser method
                        if (viewDialog != null) { viewDialog.hideDialog(); viewDialog = null; }
                       newUser();

                    }
                } else {

                }
            }
        });

    }
    @Override public void onStop()
    {
        super.onStop();
        //Removing the Dialog when activity stops to avoid memory leaks
        if (viewDialog != null) { viewDialog.hideDialog(); viewDialog = null; }
    }





}
