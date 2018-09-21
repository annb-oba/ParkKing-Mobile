package com.example.afbu.parkking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.example.afbu.parkking.FloorMapView.FloorMapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FloorMap extends AppCompatActivity {

    private FloorMapView floorMapView;
    private WifiScanner wifiScanner;
    private String floorID = "5";
    private ImageButton backButton;
    private static final String TAG = FloorMap.class.getSimpleName();
    private ArrayList<Router> router1Array,router2Array,router3Array;
    private ArrayList<String> floorIdArray;
    private ArrayList<String> floorTitleArray;
    private BroadcastReceiver resultReceiver;
    private TextView FloorMap_txtProgressBarTxt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_floor_map);



        floorMapView = (FloorMapView) findViewById(R.id.FloorMap_floorMapView);
        FloorMap_txtProgressBarTxt = (TextView) findViewById(R.id.FloorMap_txtProgressBarTxt);
        findViewById(R.id.FloorMap_floorMapView).setVisibility(View.GONE);
        backButton = (ImageButton)findViewById(R.id.FloorMap_btnBackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(FloorMap.this, Home.class);
                startActivity(myIntent);
                finish();
            }
        });

        resultReceiver = createBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(resultReceiver, new IntentFilter("com.parkking.floor.id.broadcast"));

        initFloorScanner();
        // Log.w("FLOOR MAP",intent.getStringExtra("floor_info"));
        // WifiScanner wifiScanner = new WifiScanner(getApplicationContext(),floorMapView);
        //initFloorMap();
    }
    private BroadcastReceiver createBroadcastReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.w("LOGGGG","BROADCAST RECEIVED FROM BACKGROUND THREAD");
                floorID = intent.getStringExtra("floor_id");
                FloorMap_txtProgressBarTxt.setText("Loading Floor Map. . .");
                findViewById(R.id.FloorMap_loadingLayout).setVisibility(View.GONE);
                findViewById(R.id.FloorMap_floorMapView).setVisibility(View.VISIBLE);
                initFloorMap();

            }
        };
    }
    @Override
    protected void onDestroy() {
        if (resultReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(resultReceiver);
        }
        super.onDestroy();
    }

    public void initFloorMap(){
        StringRequest strRequest = new StringRequest(Request.Method.GET, getString(R.string.get_floor_url) + floorID, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, response.toString());
                try {

                    JSONObject responseObj = new JSONObject(response);
                    if (responseObj.getBoolean("success")) {
                        JSONObject floorObj = new JSONObject(responseObj.getString("floor_data"));

                        JSONObject floorIndicatorsObj = new JSONObject(floorObj.getString("floor_indicators"));
                        JSONArray floorIndicatorsArray = new JSONArray();
                        if (floorIndicatorsObj.getBoolean("has_indicators")) {
                            floorIndicatorsArray = new JSONArray(floorIndicatorsObj.getString("indicators"));
                        }

                        JSONObject floorSlotsObj = new JSONObject(floorObj.getString("floor_slots"));
                        JSONArray floorSlotsArray = new JSONArray();
//                        if(floorSlotsObj.getBoolean("has_slots")) {
//                            floorSlotsArray = new JSONArray(floorSlotsObj.getString("slots"));
//                        } else {
//                            Toast.makeText(FloorMap.this, "No Slots", Toast.LENGTH_SHORT).show();
//                        }

                        floorMapView.setFloorMapInformation(
                                getString(R.string.floor_map_folder) + floorObj.getString("image"),
                                floorIndicatorsArray, floorSlotsArray, floorObj.getDouble("map_width"),floorObj.getDouble("map_height"),
                                floorID);



                    } else {
                        Toast.makeText(getApplicationContext(), responseObj.getString("message"), Toast.LENGTH_SHORT).show();
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

    public void initFloorScanner(){

        router1Array = new ArrayList<Router>();
        router2Array = new ArrayList<Router>();
        router3Array = new ArrayList<Router>();
        floorIdArray = new ArrayList<String>();
        floorTitleArray = new ArrayList<String>();

        Intent myIntent;
        myIntent = getIntent();
        String response = myIntent.getStringExtra("floor_info");
        JSONObject object = null;
        try {
            object = new JSONObject(response);
            JSONArray data = object.getJSONArray("data");
            for(int i=0; i<data.length();i++) {
                JSONObject floor = data.getJSONObject(i);
                JSONObject floor_info = floor.getJSONObject("floor_info");
                String floor_id = floor_info.getString("id");
                String floor_title = floor_info.getString("title");
                JSONArray floor_routers = floor.getJSONArray("floor_routers");
                Router router1, router2, router3;
                router1 = new Router(floor_routers.getJSONObject(0).getString("SSID"), floor_routers.getJSONObject(0).getString("id"));
                router2 = new Router(floor_routers.getJSONObject(1).getString("SSID"), floor_routers.getJSONObject(1).getString("id"));
                router3 = new Router(floor_routers.getJSONObject(2).getString("SSID"), floor_routers.getJSONObject(2).getString("id"));
                floorIdArray.add(floor_id);
                floorTitleArray.add(floor_title);
                router1Array.add(router1);
                router2Array.add(router2);
                router3Array.add(router3);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WifiScanner wifiScanner = new WifiScanner(getApplicationContext(),router1Array, router2Array,router3Array,floorIdArray,floorTitleArray);
    }
}


