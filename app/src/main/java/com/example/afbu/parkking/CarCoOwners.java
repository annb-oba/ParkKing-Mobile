package com.example.afbu.parkking;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class CarCoOwners extends AppCompatActivity {
    ImageButton btnAdd;
    String carID;
    private ImageButton btnBackHome;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_co_owners);
        Intent intent = getIntent();
        carID = intent.getStringExtra("car_id");
        btnAdd = (ImageButton)findViewById(R.id.CarCoOwners_btnAdd);
        btnBackHome = (ImageButton) findViewById(R.id.CarCoOwners_btnBack);
        btnBackHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gotoAddCoOwner = new Intent(getApplicationContext(), AddCoOwner.class);
                gotoAddCoOwner.putExtra("car_id", carID);
                startActivity(gotoAddCoOwner);

            }
        });

    }
}
