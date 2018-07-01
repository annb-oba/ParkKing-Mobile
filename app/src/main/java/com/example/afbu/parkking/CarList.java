package com.example.afbu.parkking;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class CarList extends AppCompatActivity {

    private ImageButton btnBackHome, btnAddCar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_list);
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
        btnAddCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gotoAddCar = new Intent(getApplicationContext(), AddCar.class);
                startActivity(gotoAddCar);
            }
        });
    }

    private void initResources() {
        btnBackHome = (ImageButton) findViewById(R.id.CarList_btnBack);
        btnAddCar = (ImageButton) findViewById(R.id.CarList_btnAddCar);
    }
}
