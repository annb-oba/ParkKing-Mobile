package com.example.afbu.parkking;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

public class Notifications extends AppCompatActivity {
    SharedPreferences SharedPreference;
    SharedPreferences.Editor editor;
    private static final String PreferenceName = "UserPreference";
    private static final String PROFID_KEY = "ProfileIDKey";
    private String ProfileID;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private Query notif_ref;
    private ImageButton btnBackHome;
    private ArrayList<Notification> notificationArrayList;
    private RecyclerView notificationRecyclerView;
    private NotificationRecyclerViewAdapter adapter;


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //nothing
    }

    @Override
    protected void onPause() {
        super.onPause();
        //update is_read
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        initResources();
        initEvents();
        initFirebase();
    }
    private void updateFirebase(){

        HashMap<String,Object> update = new HashMap<>();
        update.put("is_read","true");
        database.getReference().child("notif_individual").child(ProfileID).updateChildren(update);


    }
    private void initFirebase(){
        notif_ref = database.getReference().child("notif_individual").child(ProfileID).orderByChild("timestamp");
        notif_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    notificationArrayList=new ArrayList<Notification>();
                    Notification notification;
                    for (DataSnapshot notifData:dataSnapshot.getChildren()){
                        String tempId = notifData.getKey();
                        String tempModule = notifData.child("module").getValue().toString();
                        String tempTitle = notifData.child("title").getValue().toString();
                        String tempMessage = notifData.child("message").getValue().toString();
                        String tempDate = notifData.child("notification_date").getValue().toString();
                        String tempIs_read = notifData.child("is_read").getValue().toString();
                        String tempTimestamp = notifData.child("timestamp").getValue().toString();
                        try{
                            switch(tempModule){
                                case "co_owner_request": //with request_id
                                    String tempRequest_id = notifData.child("request_id").getValue().toString();
                                    notification = new Notification(tempId,tempTitle,tempMessage,tempDate,tempModule,tempIs_read,tempTimestamp,tempRequest_id);
                                    notificationArrayList.add(notification);
                                    break;
                                default:
                                    //no request ID
                                    notification = new Notification(tempId,tempTitle,tempMessage,tempDate,tempModule,tempIs_read,tempTimestamp,"");
                                    notificationArrayList.add(notification);
                            }
                        }catch(Exception e){

                        }


                    }
                    Collections.reverse(notificationArrayList);
                    initRecyclerView();
                    adapter.notifyItemInserted(notificationArrayList.size()-1);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private void initRecyclerView(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        RecyclerView recyclerView = findViewById(R.id.Notifications_RecyclerView);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new NotificationRecyclerViewAdapter(this,notificationArrayList);
        recyclerView.setAdapter(adapter);
    }
    private void initEvents() {
        btnBackHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initResources() {
        SharedPreference = getSharedPreferences(PreferenceName, Context.MODE_PRIVATE);
        if(!SharedPreference.contains(PROFID_KEY)){
            Intent myIntent = new Intent(Notifications.this, StartUp.class);
            startActivity(myIntent);
        }else{
            ProfileID = SharedPreference.getString(PROFID_KEY, "");
        }
        btnBackHome = (ImageButton) findViewById(R.id.Notifications_btnBack);
        notificationRecyclerView = (RecyclerView) findViewById(R.id.Notifications_RecyclerView);

    }
}
