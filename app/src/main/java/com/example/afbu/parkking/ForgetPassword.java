package com.example.afbu.parkking;

import android.content.Intent;
import android.content.SharedPreferences;
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

public class ForgetPassword extends AppCompatActivity {

    SharedPreferences SharedPreference;
    SharedPreferences.Editor editor;
    private static final String PreferenceName = "UserPreference";
    private static final String PROFID_KEY = "ProfileIDKey";

    private static String TAG = ForgetPassword.class.getSimpleName();

    private Button btnResetPass;
    private EditText edtEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        intitResources();
        initEvents();
    }

    private void initEvents() {
        btnResetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(edtEmail.getText().toString() == null){
                    Toast.makeText(getApplicationContext(), "Please input an email address.", Toast.LENGTH_SHORT).show();
                }else{
                    StringRequest strRequest = new StringRequest(Request.Method.POST, getString(R.string.apiURL) + "signinvehicleowner", new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject object = new JSONObject(response);
                                String status = object.getString("status");
                                if(status.equals("success")){
                                    String message = object.getString("message");
                                    Toast.makeText(getApplicationContext(),
                                            message, Toast.LENGTH_SHORT).show();
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
                                    "Failed to connect to Park King Servers", Toast.LENGTH_SHORT).show();
                        }
                    }) {
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> parameters = new HashMap<String, String>();
                            parameters.put("email", edtEmail.getText().toString());

                            return parameters;
                        }
                    };
                    AppController.getInstance().addToRequestQueue(strRequest);
                }

            }
        });
    }

    private void intitResources() {
        btnResetPass = (Button) findViewById(R.id.ForgetPassword_btnResetPass);
        edtEmail = (EditText) findViewById(R.id.ForgetPassword_edtEmail);
    }
}
