package com.sunit.ems.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.sunit.ems.EMS;
import com.sunit.ems.R;
import com.sunit.ems.ViewDialog;
import com.sunit.ems.models.Leave;
import com.sunit.ems.notification.QueueSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import sakout.mehdi.StateViews.StateView;

/**
 * ApproveLeave class handles the approval of day-off requests. And sends a peer-to-peer notification using
 * the notification token
 */
public class ApproveLeave extends AppCompatActivity {

    //FireBase imports
    private FirebaseFirestore db;
    DocumentReference rRef,iRef;
    private FirestoreRecyclerAdapter adapter;
    Query query;
    FirebaseUser user;
    FirebaseAuth mAuth;

    Activity activity;

    ViewDialog viewDialog;

    //Notification URL, serverKey and other notification data
    //This is usually a bad practice for peer to peer upstream message without server.
    //This exposes the serverKey to the clientSide which can cause exploitation
    //Usually this can be implemented using any serverSide technology or FireBase cloud function
    final private String FCM_API = "https://fcm.googleapis.com/fcm/send";
    final private String serverKey = "key=" + "AAAAIOZ11Ec:APA91bFZr6k5fdXATX6ahsTTAJKcEfILa5tnwqKU9YMLYlcckJr6JGsDo2XYMTVOuhrez_K-UJ-2KixnxPz47Vf9d2mTnqx4-yF7WXWTLOMOzI59cklIY9VRx612Ojk5tSooQNg7KUVG";
    final private String contentType = "application/json";
    final String TAG = "NOTIFICATION TAG";
    //Notification data variables
    String NOTIFICATION_TITLE;
    String NOTIFICATION_MESSAGE;
    String applicant_token;

    EMS ems;
    @BindView(R.id.leaveHistory)
    RecyclerView recyclerView;
    @BindView(R.id.stateful)
    StateView mStatusPage;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve_leave);
        getSupportActionBar().hide();
        ButterKnife.bind(this);
        //Display the loadingState
        mStatusPage.displayLoadingState();
        activity=this;
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        ems = (EMS) getApplicationContext();
        //Set query add layoutManager to recycler and finally set the adapter
        query = db.collection("leaves").orderBy("department", Query.Direction.ASCENDING);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        FirestoreRecyclerOptions<Leave> leaveFireStoreRecyclerOptions =
                new FirestoreRecyclerOptions.Builder<Leave>()
                        .setQuery(query, Leave.class).build();
        adapter = new FirestoreRecyclerAdapter<Leave, LeaveHolder>(leaveFireStoreRecyclerOptions) {

            @NonNull
            @Override
            public LeaveHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.leave_approve_item, parent, false);

                return new LeaveHolder(view);
            }
            @Override
            public void onDataChanged() {
                super.onDataChanged();
                int i=getItemCount();
                if(i>0) mStatusPage.hideStates();
                else  mStatusPage.displayState("search");

            }

            @Override
            protected void onBindViewHolder(@NonNull LeaveHolder viewHolder, int i,@NonNull final Leave model) {

                //Hides user's leaveRequest
                if (model.getEmail().equals(ems.getEmail()))
                {
                    viewHolder.itemView.setVisibility(View.INVISIBLE);
                    viewHolder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0,0));
                }
                //sets the card field values
                viewHolder.name.setText(model.getName());
                viewHolder.designation.setText(model.getDesignation());
                viewHolder.department.setText(model.getDepartment());
                viewHolder.reason.setText(model.getReason());
                viewHolder.startDate.setText(model.getStartDate());
                viewHolder.endDate.setText(model.getEndDate());
                viewHolder.timeStamp.setText(model.getTimeStamp());
                viewHolder.dayCount.setText(model.getNoOfDays()+" Days");
                viewHolder.call.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Fires the Action_DIAL implicit intent
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        String num = "+91" + model.getPhone();
                        intent.setData(Uri.parse("tel:" + num));
                        (activity).startActivity(intent);

                    }
                });

                final int noOfDays = model.getNoOfDays();//Day-Off count
                //Request accept listener
                viewHolder.accept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //show the Dialog
                        viewDialog=new ViewDialog(activity);
                        viewDialog.showDialog();
                        //Fetch the employee document and initialize the currentDayOff Balance and notification token
                        db.collection("employees").document(model.getEmail())
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        DocumentSnapshot document = task.getResult();
                                        int leaveBalance;
                                        String date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss Z", Locale.getDefault()).format(new Date());
                                        if (document.exists()) {

                                            leaveBalance=document.getDouble("leaves").intValue();
                                            applicant_token = document.getString("token");
                                            //Converting token string into an array
                                            String[] array = {applicant_token};
                                            //Checking for sufficient balance
                                            if (leaveBalance >= noOfDays)
                                             {

                                                 //Calculating the finalBalance
                                               int finalBalance=leaveBalance-noOfDays;
                                                 //notification title and message using timeStamp and HR's name
                                                 NOTIFICATION_TITLE = "Day-Off Request Approved";
                                                 NOTIFICATION_MESSAGE = "Your request has been approved by " + ems.getName() + ". On " + date
                                                         + ". Your closing leave balance " + finalBalance + " Day";
                                                 //JSON objects
                                                 JSONObject notification = new JSONObject();
                                                 JSONObject notificationBody = new JSONObject();

                                                 try {
                                                     notificationBody.put("title", NOTIFICATION_TITLE);
                                                     notificationBody.put("message", NOTIFICATION_MESSAGE);

                                                     notification.put("registration_ids", new JSONArray(Arrays.asList(array)));
                                                     notification.put("data", notificationBody);

                                                 } catch (JSONException e) {
                                                     Log.e(TAG, "onCreate: " + e.getMessage());
                                                 }
                                                 //call the sendNotification method by passing the JSON object
                                                 sendNotification(notification);

                                                 //Update the database with status, finalBalance and approvedBy text
                                                 db.collection("employees").document(model.getEmail()).collection("myLeaves")
                                                   .document(model.getID()).update("approvedBy","Approved By "+ems.getName()+" on "+date
                                                        );
                                                 db.collection("employees").document(model.getEmail())
                                                         .update("leaves",finalBalance);

                                                 db.collection("employees").document(model.getEmail())
                                                         .collection("myLeaves")
                                                         .document(model.getID())
                                                         .update("status",1)
                                                         .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                             @Override
                                                             public void onComplete(@NonNull Task<Void> task) {
                                                                 viewDialog.hideDialog();
                                                                 //Deleting from the collection
                                                                 db.collection("leaves").document(model.getID()).delete();
                                                             }
                                                         });


                                             }
                                            //Not sufficient leave balance, hide the dialog and show toast
                                             else
                                             {
                                                 viewDialog.hideDialog();
                                                 Toast.makeText(activity,"Not sufficient leave balance",Toast.LENGTH_LONG).show();

                                             }
                                        }
                                    }
                                });

                    }
                });
                //Request reject listener
                viewHolder.reject.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        viewDialog=new ViewDialog(activity);
                        viewDialog.showDialog();
                        db.collection("employees").document(model.getEmail())
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        DocumentSnapshot document = task.getResult();
                                        int leaveBalance;
                                        String date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss Z", Locale.getDefault()).format(new Date());
                                        if (document.exists()) {

                                            applicant_token = document.getString("token");
                                            String[] array = {applicant_token};

                                            NOTIFICATION_TITLE = "Day-Off Request Rejected";
                                            NOTIFICATION_MESSAGE = "Your request for Day-Off has been rejected by " + ems.getName() + ". On " + date
                                                    + " Please contact the concerned HR for further assistance.";
                                            JSONObject notification = new JSONObject();
                                            JSONObject notificationBody = new JSONObject();

                                            try {
                                                notificationBody.put("title", NOTIFICATION_TITLE);
                                                notificationBody.put("message", NOTIFICATION_MESSAGE);

                                                notification.put("registration_ids", new JSONArray(Arrays.asList(array)));
                                                notification.put("data", notificationBody);

                                            } catch (JSONException e) {
                                                Log.e(TAG, "onCreate: " + e.getMessage());
                                            }
                                            sendNotification(notification);

                                            db.collection("employees").document(model.getEmail()).collection("myLeaves")
                                                    .document(model.getID()).update("approvedBy", "Rejected By " + ems.getName() + " on " + date
                                            );
                                            db.collection("employees").document(model.getEmail())
                                                    .collection("myLeaves")
                                                    .document(model.getID())
                                                    .update("status", -1);
                                            db.collection("leaves").document(model.getID()).delete();

                                            viewDialog.hideDialog();
                                        }
                                    }
                                });



                    }
                });
            }

        };
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

    }

    //LeaveHolder class for RecyclerView adapter and binding of views
    class LeaveHolder extends RecyclerView.ViewHolder
    {


        @BindView(R.id.reason)
        TextView reason;
        @BindView(R.id.startDate)
        TextView startDate;
        @BindView(R.id.endDate)
        TextView endDate;
        @BindView(R.id.timeStamp)
        TextView timeStamp;
        @BindView(R.id.accept)
        FloatingActionButton accept;
        @BindView(R.id.reject)
        FloatingActionButton reject;
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.designation)
        TextView designation;
        @BindView(R.id.department)
        TextView department;
        @BindView(R.id.call)
        Button call;
        @BindView(R.id.dayCount)
        TextView dayCount;


        private LeaveHolder(View view)
        {
            super(view);
            ButterKnife.bind(this,view);
        }


    }

    //Send the notification to the server using QueueSingleton
    private void sendNotification(JSONObject notification) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_API, notification,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "onResponse: " + response.toString());

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ApproveLeave.this, "Request error", Toast.LENGTH_LONG).show();
                        Log.i(TAG, "onErrorResponse: Didn't work");
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", serverKey);
                params.put("Content-Type", contentType);
                return params;
            }
        };
        QueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }













    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }
    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();

    }

}
