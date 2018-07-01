package com.example.afbu.parkking;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class Notifications extends AppCompatActivity {

    private ImageButton btnBackHome;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        initResources();
        initEvents();
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
        btnBackHome = (ImageButton) findViewById(R.id.Notifications_btnBack);
    }
}
