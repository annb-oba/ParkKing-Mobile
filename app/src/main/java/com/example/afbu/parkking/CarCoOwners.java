package com.example.afbu.parkking;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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

public class CarCoOwners extends AppCompatActivity {
    private ImageButton btnAdd;
    private String carID,ProfileID;
    private ImageButton btnBackHome;
    SharedPreferences SharedPreference;
    SharedPreferences.Editor editor;
    private static final String PreferenceName = "UserPreference";
    private static final String PROFID_KEY = "ProfileIDKey";
    private static String TAG = CarCoOwners.class.getSimpleName();
    private ArrayList<CarCoOwner> CoOwnerList;
    private CarOwnerRecyclerViewAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_co_owners);
        initResources();
        initEvents();
        getCarCoOwners();
    }
    public void initResources(){
        Intent intent = getIntent();
        carID = intent.getStringExtra("car_id");
        btnAdd = (ImageButton)findViewById(R.id.CarCoOwners_btnAdd);
        btnBackHome = (ImageButton) findViewById(R.id.CarCoOwners_btnBack);
        SharedPreference = getSharedPreferences(PreferenceName, Context.MODE_PRIVATE);
        if(!SharedPreference.contains(PROFID_KEY)){
            finish();
        }else{
            ProfileID = SharedPreference.getString(PROFID_KEY, "");
        }
    }
    public void initEvents(){
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
    public void getCarCoOwners(){
        StringRequest strRequest = new StringRequest(Request.Method.POST, getString(R.string.apiURL) + "relationships/car", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if(jsonObject.getString("status").equals("success")){
                        JSONObject object = new JSONObject(response);
                        JSONArray result = object.getJSONArray("data");
                        CoOwnerList = new ArrayList<CarCoOwner>();

                        for (int i = 0; i < result.length(); i++) {
                            JSONObject c = result.getJSONObject(i);
                            String name = c.getString("first_name")+" "+c.getString("middle_name")+" "+c.getString("last_name");
                            CarCoOwner carCoOwner = new CarCoOwner(name,c.getString("email"),c.getString("profile_picture"));
                            CoOwnerList.add(carCoOwner);
                        }

                        initRecyclerView();
                    }
                    else{
                        String message = jsonObject.getString("message");
                        Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
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
                        "Unable to connect to Park King Servers", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("car_id", carID);
                parameters.put("vehicle_owner_id", ProfileID);
                return parameters;
            }
        };
        AppController.getInstance().addToRequestQueue(strRequest);
    }
    public void initRecyclerView(){
        Log.d("REC",Integer.toString(CoOwnerList.size()));
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        RecyclerView recyclerView = findViewById(R.id.CarCoOwners_recyclerView);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new CarOwnerRecyclerViewAdapter(getApplication(),CoOwnerList);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
    }
}
