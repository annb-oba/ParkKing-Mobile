package com.example.afbu.parkking;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CarProfile extends AppCompatActivity {
    SharedPreferences SharedPreference;
    SharedPreferences.Editor editor;
    private static final String PreferenceName = "UserPreference";
    private static final String PROFID_KEY = "ProfileIDKey";
    private String ProfileID;
    private String carID,message;
    private ImageButton btnBackHome;
    private TextView txtPlateNumber,txtCarBrand,txtCarModel,txtCarOwner,txtSharedTo;
    private Button btnEditInfo,btnSwitchCar,btnCoOwners;
    private ImageView carImage;
    private static String TAG = CarProfile.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_profile);
        SharedPreference = getSharedPreferences(PreferenceName, Context.MODE_PRIVATE);
        if(!SharedPreference.contains(PROFID_KEY)){
            Intent myIntent = new Intent(CarProfile.this, StartUp.class);
            startActivity(myIntent);
        }else{
            ProfileID = SharedPreference.getString(PROFID_KEY, "");
        }
        btnBackHome = (ImageButton) findViewById(R.id.CarProfile_btnBack);
        txtPlateNumber =(TextView) findViewById(R.id.CarProfile_plateNumber);
        txtCarBrand =(TextView) findViewById(R.id.CarProfile_carBrand);
        txtCarModel =(TextView) findViewById(R.id.CarProfile_carModel);
        txtCarOwner =(TextView) findViewById(R.id.CarProfile_carOwner);
        txtSharedTo =(TextView) findViewById(R.id.CarProfile_sharedTo);
        btnEditInfo = (Button) findViewById(R.id.CarProfile_btnEditInfo);
        btnSwitchCar = (Button) findViewById(R.id.CarProfile_btnSwitchCar);
        btnCoOwners = (Button) findViewById(R.id.CarProfile_btnCoOwners);
        carImage = (ImageView) findViewById(R.id.CarProfile_imgVehicle);
        Intent intent = getIntent();
        carID = intent.getStringExtra("car_id");
        getCarData();
        btnSwitchCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(CarProfile.this)
                        .setTitle("Change Current Car")
                        .setMessage("Change car to this one?")
                        .setPositiveButton("Go", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                changeCar();
                            }})
                        .setNegativeButton("Cancel", null).show();
            }
        });
        btnCoOwners.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goToCarCoOwners = new Intent(getApplicationContext(), CarCoOwners.class);
                goToCarCoOwners.putExtra("car_id", carID);
                startActivity(goToCarCoOwners);

            }
        });
        btnBackHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent gotoCarList = new Intent(getApplicationContext(), CarList.class);
                //startActivity(gotoCarList);
                finish();
            }
        });
    }

   /*Override
    public void finish() {
        Intent gotoCarList = new Intent(getApplicationContext(), CarList.class);
        startActivity(gotoCarList);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }*/

    public void changeCar(){

        if(!carID.equals("")){
            StringRequest strRequest = new StringRequest(Request.Method.POST, getString(R.string.changeCarURL), new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(TAG, response.toString());
                    JSONObject jsonObject = null;

                    try {
                        jsonObject = new JSONObject(response);
                        String message = jsonObject.getString("message");
                        Log.d(TAG, message);
                        Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
                        if(jsonObject.getString("status").equals("success")){
                            Intent gotoCarList = new Intent(getApplicationContext(), CarList.class);
                            startActivity(gotoCarList);
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


                    parameters.put("vehicle_owner_id", ProfileID);
                    parameters.put("id", carID);



                    return parameters;
                }
            };
            AppController.getInstance().addToRequestQueue(strRequest);

        }

    }

    public void getCarData(){
        StringRequest strRequest = new StringRequest(Request.Method.GET, getString(R.string.getCarInfoURL) + carID, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                        JSONObject object = new JSONObject(response);
                        message = object.getString("status");

                    if(message.equals("success")) {
                        JSONObject c = new JSONObject(object.getString("data"));
                        txtPlateNumber.setText(c.getString("plate_number").toString());
                        txtCarBrand.setText(c.getString("brand"));
                        txtCarModel.setText(c.getString("model"));
                        txtCarOwner.setText(c.getString("first_name") + " " + c.getString("last_name"));
                        txtSharedTo.setText(c.getString("shared_to"));
                        Glide.with(getApplicationContext()).asBitmap().load(getString(R.string.carImagesURL)+c.getString("car_picture")).into(carImage);
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
                return parameters;
            }
        };
        AppController.getInstance().addToRequestQueue(strRequest);
    }
}
