package com.example.afbu.parkking;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AddCoOwner extends AppCompatActivity {
    private static String TAG = AddCoOwner.class.getSimpleName();
    private Button addCoOwner;
    private ImageButton btnBack;
    private EditText edtEmail;
    private String carID,email,message;
    private SharedPreferences SharedPreference;
    private SharedPreferences.Editor editor;
    private String ProfileID;
    private static final String PreferenceName = "UserPreference";
    private static final String PROFID_KEY = "ProfileIDKey";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_co_owner);
        Intent intent = getIntent();
        carID = intent.getStringExtra("car_id");
        btnBack = (ImageButton) findViewById(R.id.AddCoOwner_btnBack);
        addCoOwner = (Button)findViewById(R.id.AddCoOwner_btnAdd);
        edtEmail = (EditText)findViewById(R.id.AddCoOwner_edtEmail);
        SharedPreference = getSharedPreferences(PreferenceName, Context.MODE_PRIVATE);
        ProfileID = SharedPreference.getString(PROFID_KEY, "");

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        addCoOwner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email=edtEmail.getText().toString().trim();
                addCoOwner();
            }
        });
    }

    public void addCoOwner(){
        if(!email.isEmpty() && !carID.isEmpty()){
            StringRequest strRequest = new StringRequest(Request.Method.POST, getString(R.string.addCoOwnerURL), new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(TAG, response.toString());
                    JSONObject jsonObject = null;

                    try {
                        jsonObject = new JSONObject(response);
                        message = jsonObject.getString("message");
                        Log.d(TAG, message);
                        Toast.makeText(getApplicationContext(),message, Toast.LENGTH_LONG).show();
                        if(jsonObject.getString("status").equals("success")){
                            finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(TAG, "Error: " + error.getMessage());
                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> parameters = new HashMap<String, String>();

                    parameters.put("email", email);
                    parameters.put("vehicle_owner_id", ProfileID);
                    parameters.put("car_id", carID);



                    return parameters;
                }
            };

            strRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            AppController.getInstance().addToRequestQueue(strRequest);
        }
        else{
            Toast.makeText(getApplicationContext(), "Please Complete all Fields", Toast.LENGTH_SHORT).show();
        }
    }
}
