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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_co_owners);
        Intent intent = getIntent();
        carID = intent.getStringExtra("car_id");
        btnAdd = (ImageButton)findViewById(R.id.CarCoOwners_btnAdd);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gotoAddCoOwner = new Intent(getApplicationContext(), AddCoOwner.class);
                gotoAddCoOwner.putExtra("car_id", carID);
                startActivity(gotoAddCoOwner);
                finish();
            }
        });

    }
}
