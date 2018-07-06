package com.example.afbu.parkking;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.renderscript.Script;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class SignIn extends AppCompatActivity {

    SharedPreferences SharedPreference;
    SharedPreferences.Editor editor;
    private static final String PreferenceName = "UserPreference";
    private static final String PROFID_KEY = "ProfileIDKey";

    private static String TAG = SignUp.class.getSimpleName();

    private EditText Email, Password;
    private Button btnSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        SharedPreference = getSharedPreferences(PreferenceName, Context.MODE_PRIVATE);
        if(SharedPreference.contains(PROFID_KEY)){
            Intent myIntent = new Intent(SignIn.this, Home.class);
            startActivity(myIntent);
        }

        initResources();
        initEvents();
    }

    private void initEvents() {
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    private void signIn() {
        StringRequest strRequest = new StringRequest(Request.Method.POST, getString(R.string.apiURL) + "signinvehicleowner", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    String status = object.getString("status");
                    if(status.equals("success")){
                        String vehicleowner_id = object.getString("vehicle_owner_id");
                        editor = SharedPreference.edit();
                        editor.putString(PROFID_KEY, vehicleowner_id);
                        if(editor.commit()){
                            Intent gotoHome = new Intent(getApplicationContext(), Home.class);
                            startActivity(gotoHome);
                        }
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
                parameters.put("email", Email.getText().toString());
                parameters.put("password", Password.getText().toString());

                return parameters;
            }
        };
        AppController.getInstance().addToRequestQueue(strRequest);
    }

    private void initResources() {
        Email = (EditText) findViewById(R.id.SignIn_edtEmail);
        Password = (EditText) findViewById(R.id.SignIn_edtPassword);
        btnSignIn = (Button) findViewById(R.id.SignIn_btnSignIn);
    }
}
