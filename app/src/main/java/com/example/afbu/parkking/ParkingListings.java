package com.example.afbu.parkking;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParkingListings extends AppCompatActivity {

    SharedPreferences SharedPreference;
    SharedPreferences.Editor editor;
    private static final String PreferenceName = "UserPreference";
    private static final String PROFID_KEY = "ProfileIDKey";
    private String ProfileID;

    private static String TAG = ParkingListings.class.getSimpleName();

    private ImageButton btnBackHome;
    private ListView ParkingList;
    private ArrayList<String> BuildingNames;
    private ArrayList<Integer> BuildingIDs;
    private EditText SearchBuildings;
    private ArrayAdapter<String> dataAdapter;
    private String ChosenBuilding;
    private TextView NumberOfFloors, NumberOfAvailableSlots, Distance;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_listings);

        SharedPreference = getSharedPreferences(PreferenceName, Context.MODE_PRIVATE);
        if(!SharedPreference.contains(PROFID_KEY)){
            Intent myIntent = new Intent(ParkingListings.this, StartUp.class);
            startActivity(myIntent);
        }else{
            ProfileID = SharedPreference.getString(PROFID_KEY, "");
        }

        initResources();
        initEvents();
    }

    private void initResources(){
        btnBackHome = (ImageButton) findViewById(R.id.ParkingListings_btnBack);
        ParkingList = (ListView) findViewById(R.id.ParkingListings_lstListings);
        SearchBuildings = (EditText) findViewById(R.id.ParkingListings_edtSearch);
        NumberOfAvailableSlots = (TextView) findViewById(R.id.ParkingListings_txtNoOfAvlSlots);
        NumberOfFloors = (TextView) findViewById(R.id.ParkingListings_txtNoOfFloors);
        Distance = (TextView) findViewById(R.id.ParkingListings_txtDistance);
    }

    private void initEvents(){
        getParkingList();

        btnBackHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        SearchBuildings.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                (ParkingListings.this).dataAdapter.getFilter().filter(s);
                dataAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        ParkingList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                ChosenBuilding = BuildingIDs.get(position).toString();

                StringRequest strRequest = new StringRequest(Request.Method.GET,
                        getString(R.string.apiURL) + "get_building_infos/" + ChosenBuilding,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject object = new JSONObject(response);
                                    String status = object.getString("status");
                                    if(status.equals("success")){
                                        String num_of_floors = object.getString("number_of_floors");
                                        String num_of_avail_slots = object.getString("number_of_available_slots");
                                        NumberOfAvailableSlots.setText(num_of_avail_slots);
                                        NumberOfFloors.setText(num_of_floors);
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
                        return parameters;
                    }
                };
                AppController.getInstance().addToRequestQueue(strRequest);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void getParkingList() {
        StringRequest strRequest = new StringRequest(Request.Method.POST,
                getString(R.string.apiURL) + "get_parking_list/",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            String status = object.getString("status");
                            if(status.equals("success")){
                                JSONArray result = object.getJSONArray("data");
                                BuildingNames = new ArrayList<>();
                                BuildingIDs = new ArrayList<>();
                                for (int i = 0; i < result.length(); i++) {
                                    JSONObject c = result.getJSONObject(i);
                                    BuildingNames.add(c.getString("name"));
                                    BuildingIDs.add(c.getInt("id"));
                                }
                                dataAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.row_layout, BuildingNames);
                                ParkingList.setAdapter(dataAdapter);
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
                return parameters;
            }
        };
        AppController.getInstance().addToRequestQueue(strRequest);
    }

}
