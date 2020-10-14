package com.sunit.ems.activities;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sunit.ems.EMS;
import com.sunit.ems.R;
import com.sunit.ems.models.Leave;
import com.sunit.ems.notification.QueueSingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import github.ishaan.buttonprogressbar.ButtonProgressBar;
import ru.slybeaver.slycalendarview.SlyCalendarDialog;

/**
 * ApplyLeave class is responsible for the Day-off application process
 * Starts with date range picker, validates the date and calculates the noOfDays
 * excluding saturday and sunday, compares current balance if valid send notification
 * to the topic "HR" and save the data
 */
public class ApplyLeave extends AppCompatActivity {


    @BindView(R.id.selectDate)
    Button button;
    @BindView(R.id.startDate)
    TextView startDate;
    @BindView(R.id.endDate)
    TextView endDate;
    @BindView(R.id.dayOffLeft)
    TextView leaveBalance;
    @BindView(R.id.status)
    EditText reason;
    @BindView(R.id.applyLayout)
    LinearLayout applyLayout;
    @BindView(R.id.dayOffCount)
    TextView leaveCount;
    @BindView(R.id.dayCountString)
    TextView noOfDays;
    EMS ems;

    String firstDateString,endDateString,timeStamp,status,
            approvedBy="This request is yet to be approved"
            ,reasonString;
    int balance,noOfDaysInt;

    //FireBase import
    FirebaseFirestore db;

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
    String TOPIC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_leave);
        db=FirebaseFirestore.getInstance();
        ButterKnife.bind(this);
        getSupportActionBar().hide();
        ems=(EMS)getApplicationContext();
        balance=ems.getLeaves();
        leaveBalance.setText(""+ems.getLeaves());
        reason.setVisibility(View.INVISIBLE);
        applyLayout.setVisibility(View.INVISIBLE);
        noOfDays.setVisibility(View.INVISIBLE);


        final ButtonProgressBar save = findViewById(R.id.bpb_main);
        //On save button click
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reasonString=reason.getText().toString();
                //Checks the Day-Off reason for empty string
                if(!reasonString.equals(""))
                {
                    //Start button animation
                    save.startLoader();
                    //timeStamp string
                    String date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss Z", Locale.getDefault()).format(new Date());
                    final int random = new Random().nextInt(100) + 10;
                    final int randomOne = new Random().nextInt(10000) + 100;
                    String id=""+random+randomOne;
                    TOPIC = "/topics/HR"; //topic must match with what the receiver subscribed to
                    NOTIFICATION_TITLE = "New Day-Off request";
                    NOTIFICATION_MESSAGE = "Day-Off request from " + ems.getName() + ", " +
                            "Department-" + ems.getDepartment() + " for " + noOfDaysInt + " day.";
                    JSONObject notification = new JSONObject();
                    JSONObject notificationBody = new JSONObject();
                    try {
                        notificationBody.put("title", NOTIFICATION_TITLE);
                        notificationBody.put("message", NOTIFICATION_MESSAGE);

                        notification.put("to", TOPIC);
                        notification.put("data", notificationBody);
                    } catch (JSONException e) {
                        Log.e(TAG, "onCreate: " + e.getMessage());
                    }
                    sendNotification(notification);
                    int status=0;
                    Leave leave=new Leave(id,approvedBy,reasonString,endDateString,firstDateString
                    ,date,ems.getName(),ems.getDesignation(),ems.getDepartment(),ems.getEmail(),ems.getPhone(),noOfDaysInt,status);
                    db.collection("leaves").document(id).set(leave);

                    db.collection("employees").document(ems.getEmail()).collection("myLeaves")
                            .document(id).set(leave)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                      save.stopLoader();
                                    //Handler thread to show the animation and finish the activity with success toast

                                    final int splash_length = 500;
                                    new Handler().postDelayed(
                                            new Runnable() {

                                                @Override
                                                public void run() {

                                                    Toasty.success(ApplyLeave.this, "Day-Off Applied! Please wait for approval", Toast.LENGTH_LONG, true).show();
                                                    finish();
                                                }


                                            }, splash_length);

                                }
                            });
                }
                //DayOff string is empty
                else
                {
                   reason.setError("Oops! Empty");
                    Toasty.info(ApplyLeave.this, "Reason for Day-off is mandatory", Toast.LENGTH_SHORT, true).show();
                }



            }
        });
        //Callback for date range picker
        SlyCalendarDialog.Callback callback = new SlyCalendarDialog.Callback() {
            //On cancel button pressed
            @Override
            public void onCancelled() {
                Toasty.info(ApplyLeave.this, "Please select date range", Toast.LENGTH_SHORT, true).show();
            }

            //On dateSelected two Calendar objects firstDate and secondDate
            @Override
            public void onDataSelected(Calendar firstDate, Calendar secondDate, int hours, int minutes) {

                //SecondDate is null when only one date is selected
                if(secondDate==null) secondDate=firstDate;
                //Converting the dates to string and showing in the views
                Date first=firstDate.getTime();
                Date second=secondDate.getTime();
                Date currentDate=Calendar.getInstance().getTime();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                firstDateString = dateFormat.format(firstDate.getTime());
                endDateString=dateFormat.format(secondDate.getTime());
                startDate.setText(firstDateString);
                endDate.setText(endDateString);
                //Checking for previous date by comparing with current date object
                if(first.before(currentDate)){
                    Toasty.error(ApplyLeave.this, "Select an upcoming date", Toast.LENGTH_SHORT, true).show();
                    reason.setVisibility(View.INVISIBLE);
                    applyLayout.setVisibility(View.INVISIBLE);
                    noOfDays.setVisibility(View.INVISIBLE);
                    leaveCount.setText("");
                }
                //The date is valid, process the request
               else
                {
                    //Counter to hold noOfDays in the request
                    int numberOfDays = 0;
                    //Looping from the first date to last date to increment the counter while the day is not sunday or saturday
                    while (firstDate.before(secondDate)) {
                        if ((Calendar.SATURDAY != firstDate.get(Calendar.DAY_OF_WEEK))
                                &&(Calendar.SUNDAY != firstDate.get(Calendar.DAY_OF_WEEK))) {
                            numberOfDays++;

                        }
                        firstDate.add(Calendar.DATE,1);
                    }
                    //The while loop leaves the last day unchecked, when the two date is equal and
                    //the day is a working day then add one to the counter
                    if(firstDate.equals(secondDate) &&( (Calendar.SATURDAY != firstDate.get(Calendar.DAY_OF_WEEK))
                            &&(Calendar.SUNDAY != firstDate.get(Calendar.DAY_OF_WEEK))))
                    {
                        numberOfDays+=1;
                    }
                    //Checks the leaveBalance, shows the save button and reasonEditText
                    if(balance>=numberOfDays)
                    {
                        leaveCount.setText(""+numberOfDays);
                       reason.setVisibility(View.VISIBLE);
                       noOfDaysInt=numberOfDays;
                       applyLayout.setVisibility(View.VISIBLE);
                       noOfDays.setVisibility(View.VISIBLE);
                    }
                    //Not sufficient Leave Balance
                    else
                    {
                        reason.setVisibility(View.INVISIBLE);
                        leaveCount.setText("");
                        applyLayout.setVisibility(View.INVISIBLE);
                        noOfDays.setVisibility(View.INVISIBLE);
                        Toasty.warning(ApplyLeave.this, "Exceeds day-off balance.", Toast.LENGTH_SHORT, true).show();
                    }
                }





            }
        };
        //Firing the dateRangePicker on activity create
        new SlyCalendarDialog()
                .setSingle(false)
                .setCallback(callback)
                .show(getSupportFragmentManager(), "TAG_SLYCALENDAR");
        //Firing the dateRangePicker on button click
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SlyCalendarDialog()
                        .setSingle(false)
                        .setCallback(callback)
                        .show(getSupportFragmentManager(), "TAG_SLYCALENDAR");
            }
        });


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
                        Toast.makeText(ApplyLeave.this, "Request error", Toast.LENGTH_LONG).show();
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
}
