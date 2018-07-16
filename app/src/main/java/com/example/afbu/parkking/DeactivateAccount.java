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

public class DeactivateAccount extends AppCompatActivity {

    SharedPreferences SharedPreference;
    SharedPreferences.Editor editor;
    private static final String PreferenceName = "UserPreference";
    private static final String PROFID_KEY = "ProfileIDKey";
    private String ProfileID;

    private static String TAG = ParkingListings.class.getSimpleName();

    private ImageButton btnBackHome;
    private Button btnDeactAccount;
    private EditText edtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deactivate_account);

        SharedPreference = getSharedPreferences(PreferenceName, Context.MODE_PRIVATE);
        if(!SharedPreference.contains(PROFID_KEY)){
            Intent myIntent = new Intent(DeactivateAccount.this, StartUp.class);
            startActivity(myIntent);
        }else{
            ProfileID = SharedPreference.getString(PROFID_KEY, "");
        }

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

        btnDeactAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deactivateAccount();
            }
        });

    }

    private void initResources() {
        btnBackHome = (ImageButton) findViewById(R.id.DeactivateAccount_btnBack);
        btnDeactAccount = (Button) findViewById(R.id.DeactivateAccount_btnDeactAcc);
        edtPassword = (EditText) findViewById(R.id.DeactivateAccount_Password);
    }

    private void deactivateAccount(){
        StringRequest strRequest = new StringRequest(Request.Method.POST,
                getString(R.string.apiURL) + "deactivate_account",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            String status = object.getString("status");
                            if(status.equals("success")){
                                editor = SharedPreference.edit();
                                editor.clear();
                                editor.commit();
                                Intent gotoStartUp = new Intent(getApplicationContext(), StartUp.class);
                                startActivity(gotoStartUp);
                                Toast.makeText(getApplicationContext(),
                                        "Deactivation successful.", Toast.LENGTH_SHORT).show();
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
                parameters.put("password", edtPassword.getText().toString());
                parameters.put("vehicle_owner_id", ProfileID);
                return parameters;
            }
        };
        AppController.getInstance().addToRequestQueue(strRequest);
    }
}
