package com.example.afbu.parkking;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class StartUp extends AppCompatActivity {

    Button signIn, signUp;
    TextView contAsGuest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_up);

        initResources();
        initEvents();
    }

    private void initResources(){

        signIn = (Button) findViewById(R.id.StartUp_btnSignIn);
        signUp = (Button) findViewById(R.id.StartUp_btnSignUp);
        contAsGuest = (TextView) findViewById(R.id.StartUp_txtContinueAsGuest);

    }

    private void initEvents (){

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gotoSignIn = new Intent(getApplicationContext(), SignIn.class);
                startActivity(gotoSignIn);
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gotoSignUp = new Intent(getApplicationContext(), SignUp.class);
                startActivity(gotoSignUp);
            }
        });

        contAsGuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gotoHome = new Intent(getApplicationContext(), Home.class);
                startActivity(gotoHome);
            }
        });

    }
}
