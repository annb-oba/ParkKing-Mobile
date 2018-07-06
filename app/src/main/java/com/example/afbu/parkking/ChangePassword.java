package com.example.afbu.parkking;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ChangePassword extends AppCompatActivity {

    SharedPreferences SharedPreference;
    SharedPreferences.Editor editor;
    private static final String PreferenceName = "UserPreference";
    private static final String PROFID_KEY = "ProfileIDKey";
    private String ProfileID;

    private static String TAG = ChangePassword.class.getSimpleName();

    private Button btnChangePassword;
    private ImageButton btnBackHome;
    private EditText CurrentPassword, NewPassword, ConfirmNewPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        SharedPreference = getSharedPreferences(PreferenceName, Context.MODE_PRIVATE);
        if(!SharedPreference.contains(PROFID_KEY)){
            Intent myIntent = new Intent(ChangePassword.this, StartUp.class);
            startActivity(myIntent);
        }else{
            ProfileID = SharedPreference.getString(PROFID_KEY, "");
        }

        initResources();
        initEvents();
    }

    private void initResources(){

        btnChangePassword = (Button) findViewById(R.id.ChangePassword_btnChangePassword);
        CurrentPassword = (EditText) findViewById(R.id.ChangePassword_edtCurrentPassword);
        NewPassword = (EditText) findViewById(R.id.ChangePassword_NewPassword);
        ConfirmNewPassword = (EditText) findViewById(R.id.ChangePassword_ConfirmNewPassword);
        btnBackHome = (ImageButton) findViewById(R.id.ChangePassword_btnBack);
    }

    private void initEvents(){

        btnBackHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });

    }

    private void changePassword(){
        StringRequest strRequest = new StringRequest(Request.Method.PUT, getString(R.string.apiURL) + "change_password", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    String status = object.getString("status");
                    if(status.equals("success")){
                        String message = object.getString("message");
                        Toast.makeText(getApplicationContext(),
                                message, Toast.LENGTH_SHORT).show();
                        Intent gotoHome = new Intent(ChangePassword.this, Home.class);
                        startActivity(gotoHome);
                    }else if(status.equals("failed")){
                        String message = object.getString("message");
                        Toast.makeText(getApplicationContext(),
                                message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("vehicle_owner_id", ProfileID);                             //change this //done
                parameters.put("current_password", CurrentPassword.getText().toString());
                parameters.put("new_password", NewPassword.getText().toString());
                parameters.put("confirm_new_password", ConfirmNewPassword.getText().toString());

                return parameters;
            }
        };
        AppController.getInstance().addToRequestQueue(strRequest);
    }
}
