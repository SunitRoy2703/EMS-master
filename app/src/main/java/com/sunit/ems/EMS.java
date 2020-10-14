package com.sunit.ems;

import android.app.Application;
import android.graphics.Color;

import androidx.appcompat.content.res.AppCompatResources;

import com.google.firebase.FirebaseApp;

import sakout.mehdi.StateViews.StateViewsBuilder;

/**
 * Base Application class available for whole application lifecycle.
 * Initialize the StateViewsBuilder for showing different types of states in an activity.
 * Initialize the FireBaseApp and some global fields required throughout the application.
 * Getter and setter methods for the Global variables.
 */
public class EMS extends Application {
    String name,department,designation,phone,email,photoUrl;
    int leaves;
    boolean isHr;
    @Override
    public void onCreate()
    {
        super.onCreate();

        FirebaseApp.initializeApp(this);
        StateViewsBuilder
                .init(this)
                .setIconColor(Color.parseColor("#D2D5DA"))
                //Error state in case of no connection
                //Not used, logic for checking the connection is not implemented
                .addState("error",
                        "No Connection",
                        "Error retrieving information from server.",
                        AppCompatResources.getDrawable(this, R.drawable.ic_server_error),
                        "Retry"
                )


                //Error state in case of no data found.
                //Used in LeaveHistory and ApproveLeave activities.
                .addState("search",
                        "No Day-Off Data Found",
                        "Unfortunately I could not find any day-off request.",
                        AppCompatResources.getDrawable(this, R.drawable.search), null)


                .setButtonBackgroundColor(Color.parseColor("#317DED"))
                .setButtonTextColor(Color.parseColor("#FFFFFF"))
                .setIconSize(getResources().getDimensionPixelSize(R.dimen.state_views_icon_size));
    }



    public void setIsHr(boolean hr) {
        isHr = hr;
    }

    public boolean getIsHr() {
        return isHr;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setLeaves(int leaves) {
        this.leaves = leaves;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesignation() {
        return designation;
    }

    public String getDepartment() {
        return department;
    }

    public int getLeaves() {
        return leaves;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

}
