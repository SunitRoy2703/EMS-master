package com.sunit.ems.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.marcoscg.dialogsheet.DialogSheet;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.sunit.ems.EMS;
import com.sunit.ems.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * MainActivity class for homeScreen
 * Showing the user data using application global variables.
 * Control the main application flow
 * Edit user information
 */
public class MainActivity extends AppCompatActivity {
    //ButterKnife Injection for Views
    @BindView(R.id.profilePhoto)
    ImageView profilePhoto;
    @BindView(R.id.name)
    TextView userName;
    @BindView(R.id.designation)
    TextView designation;
    @BindView(R.id.department)
    TextView department;
    @BindView(R.id.requestLeave)
    Button applyLeave;
    @BindView(R.id.employeeList)
    Button employeeList;
    @BindView(R.id.approveLeave)
    Button approveLeave;
    @BindView(R.id.profile)
    Button profile;
    @BindView(R.id.leaveHistory)
    Button leaveHistory;
    @BindView(R.id.logOut)
    Button logOut;
    CircularImageView bottomProfilePhoto;
    EMS ems;
    //BottomDialogSheet for edit profile
    DialogSheet dialogSheet;
    EditText bottomName, bottomDesignation, bottomPhone;
    //Final variable for requestId
    static final int REQUEST_TAKE_PHOTO = 1;
    //FireBaseStorage for uploading images
    FirebaseStorage firebaseStorage;
    private UploadTask uploadTask;
    private Uri uri = null;
    FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        ButterKnife.bind(this);
        ems=(EMS)getApplicationContext();
        db = FirebaseFirestore.getInstance();
        userName.setText(ems.getName());
        designation.setText(ems.getDesignation());
        department.setText(ems.getDepartment());

        //Load the profilePhoto using Glide from imageUrl
        Glide.with(MainActivity.this)
                .load(ems.getPhotoUrl()).placeholder(R.drawable.placeholder).into(profilePhoto);


        approveLeave.setVisibility(View.INVISIBLE);
        //If HR show the ApproveDay-Off tile
        if(ems.getIsHr()) {
            approveLeave.setVisibility(View.VISIBLE);

        }

        firebaseStorage = FirebaseStorage.getInstance();
        //Initialize the DialogSheet for editing user information and saving it in the database.
        dialogSheet = new DialogSheet(MainActivity.this)
                .setTitle("Edit Profile")
                .setColoredNavigationBar(true)
                .setCancelable(true)
                .setRoundedCorners(true)
                .setBackgroundColor(getResources().getColor(R.color.colorBackground))
                .setColoredNavigationBar(true)
                .setPositiveButton(R.string.save, new DialogSheet.OnPositiveClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Onclick save button inside dialogsheet
                        //Checking for null URI for image
                        //If not null save the image in server and get the download Url and save the URL
                        if (uri != null)
                            sendDataToServer(uri);
                        String bottomNameText = bottomName.getText().toString();
                        String bottomDesignationText = bottomDesignation.getText().toString();
                        String bottomPhoneText = bottomPhone.getText().toString();
                        //Check for changed user information by matching with application global variables
                        //if different change the global value and save it to database
                        if (!bottomNameText.equals(ems.getName())) {
                            ems.setName(bottomNameText);
                            db.collection("employees").document(ems.getEmail())
                                    .update("name", bottomNameText);


                        }
                        if (!bottomDesignationText.equals(ems.getDesignation())) {

                            ems.setDesignation(bottomDesignationText);
                            db.collection("employees").document(ems.getEmail())
                                    .update("designation", bottomDesignationText);

                        }
                        if (!bottomPhoneText.equals(ems.getPhone())) {
                            ems.setPhone(bottomPhoneText);
                            db.collection("employees").document(ems.getEmail())
                                    .update("phone", bottomPhoneText);
                        }
                        //Finally setting the updated value in respective views
                        userName.setText(ems.getName());
                        designation.setText(ems.getDesignation());
                        dialogSheet.dismiss();
                    }
                });

        //Getting the bottomSheet inflated view and setting the field values
        dialogSheet.setView(R.layout.bottomsheet_profile);
        View inflatedView = dialogSheet.getInflatedView();
        bottomName = inflatedView.findViewById(R.id.bottomName);
        bottomDesignation = inflatedView.findViewById(R.id.bottomDesignation);
        bottomPhone = inflatedView.findViewById(R.id.phone_number);
        bottomProfilePhoto = inflatedView.findViewById(R.id.bottomPhoto);
        Glide.with(MainActivity.this)
                .load(ems.getPhotoUrl()).placeholder(R.drawable.placeholder).into(bottomProfilePhoto);
        bottomName.setText(ems.getName());
        bottomDesignation.setText(ems.getDesignation());
        bottomPhone.setText(ems.getPhone());
        //listener on imageView to fire an Implicit intent for picking an image for profilePhoto
        bottomProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickPhoto();
            }
        });

        //Listeners on different homeTiles to do the function or start an activity using explicit intent

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogSheet.show();
            }
        });
        applyLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ApplyLeave.class));
            }
        });

        employeeList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(MainActivity.this, EmployeeList.class));


            }
        });

        leaveHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,LeaveHistory.class));
            }
        });

        approveLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ApproveLeave.class));
            }
        });

        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //FireBaseUI method to SignOut the currentUser

                AuthUI.getInstance()
                        .signOut(MainActivity.this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                //Prints GOODBYE and starts the LoginActivity by Finishing the current activity
                                Toast.makeText(MainActivity.this,"GoodBye!!", Toast.LENGTH_SHORT).show();
                                Intent intent=new Intent(MainActivity.this, LoginActivity.class);
                                startActivity(intent);
                                Animatoo.animateSplit(MainActivity.this);
                                finish();
                            }
                        });
            }
        });



    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);


        //Checking response for image picker and showing the image in respective views
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            //Initialize the global uri variable for uploading it to server
            uri = data.getData();
            Glide.with(MainActivity.this)
                    .load(uri).placeholder(R.drawable.placeholder).into(profilePhoto);
            Glide.with(MainActivity.this)
                    .load(uri).placeholder(R.drawable.placeholder).into(bottomProfilePhoto);


        }

    }

    //Method to fire the Image Implicit intent when the user clicks on the image in bottomSheet
    private void clickPhoto() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_TAKE_PHOTO);
    }


    //Save the data into the server provided in the argument and get the download URL and save it to database
    private void sendDataToServer(Uri uri) {
        StorageReference storageRef = firebaseStorage.getReference();


        StorageReference profilePhotoRef = storageRef.child(ems.getName() + "profilePhoto.jpg");


        uploadTask = profilePhotoRef.putFile(uri);
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();

                }

                // Continue with the task to get the download URL
                return profilePhotoRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    //Get the downloadUrl and update the user photoURL field in the database
                    Uri downloadUri = task.getResult();
                    db.collection("employees").document(ems.getEmail())
                            .update("photoUrl", downloadUri.toString());

                } else {
                    // Handle failures
                    // ...
                }
            }
        });
    }


}

