package com.sunit.ems.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.sunit.ems.EMS;
import com.sunit.ems.R;
import com.sunit.ems.models.Leave;

import butterknife.BindView;
import butterknife.ButterKnife;
import sakout.mehdi.StateViews.StateView;

/**
 * LeaveHistory class to show user's day-off request history, with a real-time status view.
 */
public class LeaveHistory extends AppCompatActivity {

    //FireBase imports
    private FirebaseFirestore db;
    DocumentReference rRef,iRef;
    private FirestoreRecyclerAdapter adapter;
    Query query;
    FirebaseUser user;
    FirebaseAuth mAuth;

    EMS ems;



    @BindView(R.id.leaveHistory)
    RecyclerView recyclerView;
    //Handles empty state view, shows an error message
    @BindView(R.id.stateful)
    StateView mStatusPage;



    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leave_history);
        getSupportActionBar().hide();
        ButterKnife.bind(this);
        //Display Loading status
        mStatusPage.displayLoadingState();

        db=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();
        ems=(EMS) getApplicationContext();
        //set the query
        query = db.collection("employees").document(ems.getEmail()).collection("myLeaves")
                .orderBy("timeStamp", Query.Direction.ASCENDING);
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
                        .inflate(R.layout.leave_history_item, parent, false);

                return new LeaveHolder(view);
            }
            @Override
            public void onDataChanged() {
                super.onDataChanged();
                //get item count in the collection
                int i=getItemCount();
                //more than one item hides the loading state
                if(i>0) mStatusPage.hideStates();
                    //shows the error message with tag search defined in the manifest
                else  mStatusPage.displayState("search");
            }

            @Override
            protected void onBindViewHolder(@NonNull LeaveHolder viewHolder, int i, final Leave model) {

                //setting data field values
                viewHolder.approvedBy.setText(model.getApprovedBy());
                viewHolder.reason.setText(model.getReason());
                viewHolder.startDate.setText(model.getStartDate());
                viewHolder.endDate.setText(model.getEndDate());
                viewHolder.timeStamp.setText(model.getTimeStamp());
                viewHolder.dayCount.setText(model.getNoOfDays()+" Days");
                //Checking the status which is updated realtime
                //Changing the statusText and backGround color accordingly
                if(model.getStatus()==0)
                {
                    viewHolder.status.setText(getString(R.string.pending));
                    viewHolder.status.setBackgroundColor(
                            ContextCompat.getColor(ems,
                                    R.color.colorAccent));
                }
                if(model.getStatus()==1)
                {
                    viewHolder.status.setText(getString(R.string.approved));
                    viewHolder.status.setBackgroundColor(
                            ContextCompat.getColor(ems,
                                    R.color.green));
                }
                if(model.getStatus()==-1)
                {
                    viewHolder.status.setText(getString(R.string.rejected));
                    viewHolder.status.setBackgroundColor(
                            ContextCompat.getColor(ems,
                                    R.color.red));
                }
            }

        };
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);


    }

    //LeaveHolder class for RecyclerView adapter and binding of views
     class LeaveHolder extends RecyclerView.ViewHolder
    {
        @BindView(R.id.approvedBy)
        TextView approvedBy;
        @BindView(R.id.reason)
        TextView reason;
        @BindView(R.id.startDate)
        TextView startDate;
        @BindView(R.id.endDate)
        TextView endDate;
        @BindView(R.id.timeStamp)
        TextView timeStamp;
        @BindView(R.id.status)
        TextView status;
        @BindView(R.id.noOfDays)
        TextView dayCount;

        private LeaveHolder(View view)
        {
            super(view);
            ButterKnife.bind(this,view);
        }


    }

    //Start and stop listening to the database on activity start and stop
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
