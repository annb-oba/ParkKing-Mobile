package com.example.afbu.parkking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import java.util.List;
import java.util.Map;

public class FloorMap extends AppCompatActivity {

    private FloorMapView floorMapView;
    private TextView parkingFeeTextView;
    private TextView floorTitleTextView;
    private TextView availableSlotsTextView;
    private TextView selectedSlotTextView;
    private WifiScanner wifiScanner;
    private String floorID = "";
    private ImageButton backButton;
    private static final String TAG = FloorMap.class.getSimpleName();
    private ArrayList<Router> router1Array,router2Array,router3Array;
    private ArrayList<String> floorIdArray;
    private ArrayList<String> floorTitleArray;
    private ArrayList<Integer> buildingFloorHierarchy;
    private BroadcastReceiver resultReceiver;
    private TextView FloorMap_txtProgressBarTxt;
    private Spinner floorSpinner;
    private String[] buildingFloorID;
    private String[] buildingFloorTitle;
    private String buildingID;
    private String intent;

    private int spinner_check=0;
    private Boolean floorsLoaded=false;
    private String currentFloorID;
    public static final String CURRENT_FLOOR_ID = "currentFloorId";
    public static final String BROADCAST_RECEIVED ="bradcastReceived";
    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPreferences;
    private static final String PreferenceName = "UserPreference";
    private static final String PROFID_KEY = "ProfileIDKey";
    private String parkedSlotID;

    private SharedPreferences pendingParkingData;
    private SharedPreferences.Editor pendingParkingDataEditor;
    private static final String PENDING_PARK_DATA_PREF_KEY = "PendingParkingData";
    private static final String FAB_SELECTED_SLOT_ID_KEY = "fabSelectedSlotId";
    private static final String FAB_SELECTED_SLOT_TITLE_KEY = "fabSelectedSlotTitle";

    private FloatingActionButton occupySlotFAB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_floor_map);

        Intent myIntent;
        myIntent = getIntent();
        buildingID = myIntent.getStringExtra("building_id");
        intent = myIntent.getStringExtra("intent");
        parkedSlotID = myIntent.getStringExtra("slot_id");

//        // for testing purposes
//        SharedPreferences floorIDSharedPreference = getSharedPreferences(CURRENT_FLOOR_ID, MODE_PRIVATE);
//        editor = floorIDSharedPreference.edit();
//        editor.putString("currentFloorID", "1");
//        editor.commit();
//
//        editor = null;
//        // for testing purposes

        if(intent.equals("park") || intent.equals("view_parked")){
            initResources(intent);
            initEvents();
            if(!intent.equals("view_parked")) {
                initFloorScanner();
            }

        }else if(intent.equals("view")) {
            initResources(intent);
            initEvents();
        }
        else{
            //redirect
        }



    }

    private void initResources(String intent) {
        floorMapView = (FloorMapView) findViewById(R.id.FloorMap_floorMapView);
        floorSpinner = (Spinner) findViewById(R.id.FloorMap_floorSpinner);
        floorSpinner.setVisibility(View.GONE);
        findViewById(R.id.FloorMap_floorMapView).setVisibility(View.GONE);

        backButton = (ImageButton)findViewById(R.id.FloorMap_btnBackButton);
        parkingFeeTextView = (TextView) findViewById(R.id.parkingFeeTextView);
        floorTitleTextView = (TextView) findViewById(R.id.floorTitleTextView);
        availableSlotsTextView = (TextView) findViewById(R.id.availableSlotsTextView);
        selectedSlotTextView = (TextView) findViewById(R.id.selectedSlotTextView);

        occupySlotFAB = (FloatingActionButton) findViewById(R.id.occupySlotFAB);

        buildingFloorID = new String[0];
        buildingFloorTitle = new String[0];

        buildingFloorHierarchy = new ArrayList<>();

        if(intent.equals("park") || intent.equals("view_parked")){
           // Toast.makeText(getApplicationContext(),"Park",Toast.LENGTH_SHORT).show();
            if(!intent.equals("view_parked")) {
                FloorMap_txtProgressBarTxt = (TextView) findViewById(R.id.FloorMap_txtProgressBarTxt);
                resultReceiver = createBroadcastReceiver();
                LocalBroadcastManager.getInstance(this).registerReceiver(resultReceiver, new IntentFilter("com.parkking.floor.id.broadcast"));
                getBuildingFloors(intent);
            } else {
                // Toast.makeText(getApplicationContext(),"View",Toast.LENGTH_SHORT).show();
                findViewById(R.id.FloorMap_loadingLayout).setVisibility(View.GONE);
                findViewById(R.id.FloorMap_floorMapView).setVisibility(View.VISIBLE);
                findViewById(R.id.FloorMap_floorSpinner).setVisibility(View.VISIBLE);
                //floorID = "2";
                getBuildingFloors(intent);
                initFloorMap();
            }

        }else if(intent.equals("view")){
           // Toast.makeText(getApplicationContext(),"View",Toast.LENGTH_SHORT).show();
            findViewById(R.id.FloorMap_loadingLayout).setVisibility(View.GONE);
            findViewById(R.id.FloorMap_floorMapView).setVisibility(View.VISIBLE);
            findViewById(R.id.FloorMap_floorSpinner).setVisibility(View.VISIBLE);
            //floorID = "2";
            getBuildingFloors(intent);
            initFloorMap();
        }

        sharedPreferences = getSharedPreferences(PreferenceName, Context.MODE_PRIVATE);
    }

    private void getBuildingFloors(final String intent) {
        Integer index;
        StringRequest strRequest = new StringRequest(Request.Method.GET, getString(R.string.getBuildingFloorsURL) + buildingID, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, response.toString());
                try {
                    JSONObject responseObj = new JSONObject(response);
                    if (responseObj.getBoolean("success")) {
                        floorsLoaded=true;
                        JSONArray floorHierarchyJSONArray = new JSONArray(responseObj.getString("floor_hierarchy"));
                        for (int i = 0; i < floorHierarchyJSONArray.length(); i++) {
                            buildingFloorHierarchy.add(floorHierarchyJSONArray.getInt(i));
                        }

                        JSONArray floorJSONArray = new JSONArray(responseObj.getString("floors"));

                        buildingFloorID = new String[floorJSONArray.length()];
                        buildingFloorTitle = new String[floorJSONArray.length()];
                        ArrayList<FloorSpinnerObject> floorSpinnerObjects = new ArrayList<FloorSpinnerObject>();
                        for (int i = 0; i < floorJSONArray.length(); i++) {
                            JSONObject floorJSONObj = floorJSONArray.getJSONObject(i);
                            FloorSpinnerObject object = new FloorSpinnerObject(floorJSONObj.getString("id"),floorJSONObj.getString("title"));
                            buildingFloorID[i] = floorJSONObj.getString("id");
                            buildingFloorTitle[i] = floorJSONObj.getString("title");
                            floorSpinnerObjects.add(object);
                        }
                        FloorSpinnerAdapter floorSpinnerAdapter = new FloorSpinnerAdapter(getApplicationContext(),R.layout.floor_spinner_layout,floorSpinnerObjects);
//                        ArrayAdapter<String> buildingFloorSpinnerAdapter = new ArrayAdapter<String>(
//                                FloorMap.this,
//                                android.R.layout.simple_spinner_item,
//                                buildingFloorTitle
//                        );
//                        buildingFloorSpinnerAdapter.setDropDownViewResource(R.layout.floor_spinner_layout);
                        floorSpinner.setAdapter(floorSpinnerAdapter);
                        floorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    getFloorInformation(buildingFloorID[position]);
                                    //Toast.makeText(getApplicationContext(),"On Item Selected: "+buildingFloorID[position],Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });
                        if(intent.equals("park") || intent.equals("view_parked")){
                            SharedPreferences floorIDSharedPreference = getSharedPreferences(CURRENT_FLOOR_ID, MODE_PRIVATE);
                            String currentFloorID = floorIDSharedPreference.getString("currentFloorID", "");
                            for(int i=0; i<buildingFloorID.length;i++){
                                if(buildingFloorID[i].equals(currentFloorID)){
                                    floorSpinner.setSelection(i);
                                    break;
                                }
                            }
                        }
                        else if(intent.equals("view")){
                            floorSpinner.setSelection(0);
                        }

                    } else {
                        floorsLoaded=false;
                        Toast.makeText(FloorMap.this, responseObj.getString("message"), Toast.LENGTH_SHORT).show();
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

    private void getFloorInformation(String floorID) {
        this.floorID = floorID;
        floorMapView.detatchValueEventListener();
        initFloorMap();
    }

    public void initEvents() {
        parkingFeeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> parkingFeeInformationArray = floorMapView.getParkingFeeInformation();

                if(parkingFeeInformationArray.size() > 0) {
                    ParkingInformationDialog parkingInformationDialog = new ParkingInformationDialog();
                    parkingInformationDialog.setParkingRate(parkingFeeInformationArray.get(0));
                    parkingInformationDialog.setOvernightFee(parkingFeeInformationArray.get(1));
                    parkingInformationDialog.setSlotTitle(parkingFeeInformationArray.get(2));

                    parkingInformationDialog.show(getSupportFragmentManager(), "Parking Information Dialog");
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent myIntent = new Intent(FloorMap.this, Home.class);
//                startActivity(myIntent);
                finish();
            }
        });

        occupySlotFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pendingParkingData = getSharedPreferences(PENDING_PARK_DATA_PREF_KEY, Context.MODE_PRIVATE);

                NotifyDriverConsequenceDialog notifyDriverConsequenceDialog = new NotifyDriverConsequenceDialog();
                notifyDriverConsequenceDialog.setPurpose("select_slot_for_occupancy");
                notifyDriverConsequenceDialog.setmContext(getApplicationContext());
                notifyDriverConsequenceDialog.setmSupportFragmentManager(getSupportFragmentManager());
                notifyDriverConsequenceDialog.setDialogTitle("Manual slot saving confirmation");
                notifyDriverConsequenceDialog.setDialogBody("Do you want to save Slot " + pendingParkingData.getString(FAB_SELECTED_SLOT_TITLE_KEY, "") + " as your parked slot?");
                notifyDriverConsequenceDialog.setDialogNote("NOTE: You may incur excessive fees if you select a slot occupied later than your time of entry. Only proceed if you are sure that this is the slot in which you parked");
                notifyDriverConsequenceDialog.show(getSupportFragmentManager(), "ConfirmSlotOccupancy");
            }
        });
    }

    private BroadcastReceiver createBroadcastReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(floorsLoaded==true){
                    editor = getSharedPreferences(BROADCAST_RECEIVED,MODE_PRIVATE).edit();
                    editor.putString("broadcastReceived","received");
                    editor.commit();
                    //Toast.makeText(getApplicationContext(),"Broadcast Received",Toast.LENGTH_SHORT).show();
                    Log.w("LOG","BROADCAST RECEIVED FROM BACKGROUND THREAD");
                    currentFloorID = intent.getStringExtra("floor_id");
                    FloorMap_txtProgressBarTxt.setText("Loading Floor Map. . .");
                    findViewById(R.id.FloorMap_loadingLayout).setVisibility(View.GONE);
                    findViewById(R.id.FloorMap_floorMapView).setVisibility(View.VISIBLE);
                    findViewById(R.id.FloorMap_floorSpinner).setVisibility(View.VISIBLE);
                    getBuildingFloors("park");




                    //getFloorInformation(currentFloorID);
//                    initFloorMap();
                }
            }
        };
    }
    @Override
    protected void onDestroy() {
        if (resultReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(resultReceiver);
        }
        if(wifiScanner!=null){
          wifiScanner.getWifiRangeScannerRunnable().stop();
          wifiScanner.onDestroy();
        }
        floorMapView.detatchValueEventListener();
        floorMapView.destroyDrawingCache();
        floorMapView.destroySlotSharedPreference();

        editor = getSharedPreferences(CURRENT_FLOOR_ID, Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.commit();

        if(intent.equals("park")) {

        } else if(intent.equals("view")) {
//            initResources(intent);
//            initEvents();
        }

        super.onDestroy();
    }
    @Override
    protected void onPause() {
        super.onPause();

        if(wifiScanner != null) {
            Log.w("LOG","FloorMap onPause");
            wifiScanner.getWifiRangeScannerRunnable().pause();
        }
    }

    public void initFloorMap(){
        if(floorID.equals("")){
            return;
        }//BUG FIX FOR FLOOR MAP LOADING WHEN SPINNER IS INITIALIZED
        StringRequest strRequest = new StringRequest(Request.Method.GET, getString(R.string.get_floor_url_with_tenant) + floorID + "/" + (!sharedPreferences.getString(PROFID_KEY, "").trim().isEmpty() ? sharedPreferences.getString(PROFID_KEY, "") :  "0"), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, response.toString());
                try {

                    JSONObject responseObj = new JSONObject(response);
                    if (responseObj.getBoolean("success")) {
                        JSONObject floorObj = new JSONObject(responseObj.getString("floor_data"));

                        floorTitleTextView.setText(floorObj.getString("title"));
                        availableSlotsTextView.setText(floorObj.getString("open_slots"));

                        floorMapView.setIntent(intent);
                        floorMapView.setParkedSlot(parkedSlotID);
                        floorMapView.setFloorMapInformation(floorObj, floorID, parkingFeeTextView, availableSlotsTextView, selectedSlotTextView, buildingFloorHierarchy, occupySlotFAB);
                        floorMapView.setSupportFragmentManager(getSupportFragmentManager());
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

         wifiScanner = new WifiScanner(getApplicationContext(),router1Array, router2Array,router3Array,floorIdArray,floorTitleArray);
    }
}


