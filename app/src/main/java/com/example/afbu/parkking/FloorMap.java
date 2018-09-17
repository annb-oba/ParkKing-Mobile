package com.example.afbu.parkking;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

import java.util.HashMap;
import java.util.Map;

public class FloorMap extends AppCompatActivity {

    private FloorMapView floorMapView;
    private WifiScanner wifiScanner;
    private String floorID = "3";
    private static final String TAG = FloorMap.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_floor_map);

        floorMapView = (FloorMapView) findViewById(R.id.FloorMap_floorMapView);
        initFloorMap();
        wifiScanner = new WifiScanner (getApplicationContext(),floorMapView);
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
                        if(floorSlotsObj.getBoolean("has_slots")) {
                            floorSlotsArray = new JSONArray(floorSlotsObj.getString("slots"));
                        } else {
                            Toast.makeText(FloorMap.this, "No Slots", Toast.LENGTH_SHORT).show();
                        }

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
}
