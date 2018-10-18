package com.example.afbu.parkking;

import android.content.Context;
import android.content.SharedPreferences;
import android.speech.RecognizerIntent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParkedCars extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = ParkedCars.class.getSimpleName();
    private static final String PreferenceName = "UserPreference";
    private static final String PROFID_KEY = "ProfileIDKey";
    private SharedPreferences sharedPreferences;

    private RecyclerView parkedCarsRecyclerView;
    private List<ParkedCar> parkedCarList;
    private SwipeRefreshLayout parkedCarsContainer;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parked_cars);

        initResources();
    }

    private void initResources() {
        sharedPreferences = getSharedPreferences(PreferenceName, Context.MODE_PRIVATE);
        parkedCarsRecyclerView = findViewById(R.id.parkedCarsRecyclerView);
        parkedCarsContainer = (SwipeRefreshLayout) findViewById(R.id.parkedCarsContainer);
        parkedCarsContainer.setOnRefreshListener(this);
        parkedCarsContainer.post(new Runnable() {
            @Override
            public void run() {
                parkedCarsContainer.setRefreshing(true);
                getParkedCars();
            }
        });

        backButton = (ImageButton)findViewById(R.id.ParkedCars_btnBack);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent myIntent = new Intent(FloorMap.this, Home.class);
//                startActivity(myIntent);
                finish();
            }
        });
    }

    private void getParkedCars() {
        parkedCarsContainer.setRefreshing(true);
        parkedCarList = new ArrayList<>();
        StringRequest strRequest = new StringRequest(Request.Method.GET, getString(R.string.getParkedCarsURL) + sharedPreferences.getString(PROFID_KEY, "0"), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, response.toString());
                try {
                    JSONObject responseObj = new JSONObject(response);
                    JSONArray parkedCarsJSONArray = new JSONArray(responseObj.getString("parked_cars"));

                    if(parkedCarsJSONArray.length() > 0) {
                        for (int i = 0; i < parkedCarsJSONArray.length(); i ++) {
                            JSONObject parkedCarJSONObject = new JSONObject(parkedCarsJSONArray.get(i).toString());
                            JSONObject carInfoJSONObject = new JSONObject(parkedCarJSONObject.getString("vehicle_info"));
                            parkedCarList.add(new ParkedCar(
                                    carInfoJSONObject.getString("plate"),
                                    carInfoJSONObject.getString("car_make"),
                                    parkedCarJSONObject.getString("time_in"),
                                    parkedCarJSONObject.getString("bill"),
                                    parkedCarJSONObject.getString("building"),
                                    parkedCarJSONObject.getString("floor"),
                                    parkedCarJSONObject.getString("section"),
                                    parkedCarJSONObject.getString("slot"),
                                    parkedCarJSONObject.getBoolean("currently_used"),
                                    carInfoJSONObject.getInt("id"),
                                    parkedCarJSONObject.getInt("id")
                            ));
//                            Toast.makeText(ParkedCars.this, parkedCarsJSONArray.get(i).toString(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ParkedCars.this, "No vehicles parked", Toast.LENGTH_SHORT).show();
                    }

                    parkedCarsRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    ParkedCarListAdapter parkedCarListAdapter = new ParkedCarListAdapter(parkedCarList, getApplicationContext());
                    parkedCarsRecyclerView.setAdapter(parkedCarListAdapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                parkedCarsContainer.setRefreshing(false);
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

    @Override
    public void onRefresh() {
        getParkedCars();
    }
}
