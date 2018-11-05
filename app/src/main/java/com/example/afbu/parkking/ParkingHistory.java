package com.example.afbu.parkking;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
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
import org.w3c.dom.Text;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParkingHistory extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    SharedPreferences SharedPreference;
    SharedPreferences.Editor editor;
    private static final String PreferenceName = "UserPreference";
    private static final String PROFID_KEY = "ProfileIDKey";
    private String ProfileID;

    private static String TAG = ParkingHistory.class.getSimpleName();

    private ImageButton backButton;

    private SwipeRefreshLayout parkingHistoryContainer;
    private RecyclerView parkingHistoryRecyclerView;
    private List<HistoryObject> historyObjects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_history);

        initResources();
        initEvents();
    }

    private void initEvents() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void getParkingHistory() {
        parkingHistoryContainer.setRefreshing(true);
        StringRequest strRequest = new StringRequest(Request.Method.GET,
                getString(R.string.apiURL) + "getHistory/" + ProfileID,                                //ID
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "onResponse: " + response);

                        try {
                            JSONObject responseObj = new JSONObject(response);
                            if (responseObj.getBoolean("success")) {
                                historyObjects = new ArrayList<>();
                                JSONArray parkingHistoriesJSONArray = responseObj.getJSONArray("histories");
                                for (int i = 0; i < parkingHistoriesJSONArray.length(); i++) {
                                    HistoryObject historyObject = new HistoryObject(parkingHistoriesJSONArray.getJSONObject(i));
                                    historyObjects.add(historyObject);
                                }

                                Log.d(TAG, "historyObjects: " + String.valueOf(historyObjects.size()));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        parkingHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        HistoryObjectAdapter historyObjectAdapter = new HistoryObjectAdapter(historyObjects, getApplicationContext(), getSupportFragmentManager());
                        parkingHistoryRecyclerView.setAdapter(historyObjectAdapter);

                        parkingHistoryContainer.setRefreshing(false);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        "Failed to get your parking history.", Toast.LENGTH_SHORT).show();
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

    private void initResources() {
        parkingHistoryRecyclerView = (RecyclerView) findViewById(R.id.parkingHistoryRecyclerView);
        parkingHistoryContainer = (SwipeRefreshLayout) findViewById(R.id.parkingHistoryContainer);

        backButton = (ImageButton) findViewById(R.id.ParkingHistory_btnBack);
        SharedPreference = getSharedPreferences(PreferenceName, Context.MODE_PRIVATE);
        if (!SharedPreference.contains(PROFID_KEY)) {
            finish();
        } else {
            ProfileID = SharedPreference.getString(PROFID_KEY, "");

            parkingHistoryContainer.setOnRefreshListener(this);
            parkingHistoryContainer.post(new Runnable() {
                @Override
                public void run() {
                    parkingHistoryContainer.setRefreshing(true);
                    getParkingHistory();
                }
            });
        }
    }

    @Override
    public void onRefresh() {
        getParkingHistory();
    }
}
