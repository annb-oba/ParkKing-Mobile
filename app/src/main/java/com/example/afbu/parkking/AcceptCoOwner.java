package com.example.afbu.parkking;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AcceptCoOwner extends AppCompatActivity {
    private Intent intent;
    private String request_id;
    private ImageView ownerPicture,carPicture;
    private Button acceptButton;
    private TextView txtMessage,txtPlateNumber,txtBrand,txtModel,txtCarOwner;
    private LinearLayout mainLayout;
    private static String TAG = AcceptCoOwner.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_co_owner);
        initResources();
        initEvents();
        getRequestData();
    }

    public void initResources(){
        intent = getIntent();
        request_id = intent.getStringExtra("request_id");
        ownerPicture = (ImageView)findViewById(R.id.AcceptCoOwnership_ownerPicture);
        carPicture = (ImageView)findViewById(R.id.AcceptCoOwnership_carPicture);
        acceptButton = (Button)findViewById(R.id.AcceptCoOwnership_btnAccept);
        txtMessage = (TextView) findViewById(R.id.AcceptCoOwnership_txtMessage);
        txtPlateNumber = (TextView) findViewById(R.id.AcceptCoOwnership_plateNumber);
        txtBrand = (TextView) findViewById(R.id.AcceptCoOwnership_carBrand);
        txtModel = (TextView) findViewById(R.id.AcceptCoOwnership_carModel);
        txtCarOwner = (TextView) findViewById(R.id.AcceptCoOwnership_ownerName);
        mainLayout = (LinearLayout) findViewById(R.id.AcceptCoOwnership_mainLayout);
        mainLayout.setVisibility(View.INVISIBLE);
    }
    public void initEvents(){
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //accept request
            }
        });
    }
    public void getRequestData(){

        StringRequest strRequest = new StringRequest(Request.Method.POST, getString(R.string.showRelationshipRequestURL), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, response.toString());
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if(jsonObject.getString("status").equals("success")){
                        mainLayout.setVisibility(View.VISIBLE);
                        JSONObject data = new JSONObject(jsonObject.getString("data"));
                        Glide.with(getApplicationContext()).asBitmap().load(getString(R.string.carImagesURL)+data.getString("car_picture")).into(carPicture);
                        Glide.with(getApplicationContext()).asBitmap().load(getString(R.string.profilepictureURL)+data.getString("profile_picture")).into(ownerPicture);
                        Spanned span;
                        String owner_name = data.getString("first_name")+" "+data.getString("middle_name")+" "+data.getString("last_name");
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            span = Html.fromHtml("<b>"+owner_name+"</b> wants to share their <b>"+data.getString("brand") +" "+data.getString("model")+"</b> with plate number <b>"+data.getString("plate_number")+"</b> with you!",Html.FROM_HTML_MODE_LEGACY);
                            txtMessage.setText(span);
                        }
                        else {
                            txtMessage.setText(owner_name+" wants to share their "+data.getString("brand") +" "+data.getString("model")+" with plate number "+data.getString("plate_number")+" with you!");
                        }
                        //txtMessage.setText(span);

                        txtPlateNumber.setText(data.getString("plate_number"));
                        txtBrand.setText(data.getString("brand"));
                        txtModel.setText(data.getString("model"));
                        txtCarOwner.setText(owner_name);
                    }
                    else{
                        String message = jsonObject.getString("message");
                        Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getApplicationContext(), "Server Error", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("request_id", request_id);
                return parameters;
            }
        };
        AppController.getInstance().addToRequestQueue(strRequest);
    }
}
