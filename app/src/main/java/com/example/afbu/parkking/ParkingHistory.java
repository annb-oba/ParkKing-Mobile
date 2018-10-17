package com.example.afbu.parkking;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import java.util.Map;

public class ParkingHistory extends AppCompatActivity {

    SharedPreferences SharedPreference;
    SharedPreferences.Editor editor;
    private static final String PreferenceName = "UserPreference";
    private static final String PROFID_KEY = "ProfileIDKey";
    private String ProfileID;

    private static String TAG = ParkingHistory.class.getSimpleName();


    private ListView list_History;
    private ImageButton backButton;
    private TextView try1;
    private ArrayList<String> BuildingNames;
    private ArrayList<String> SlotIDs;
    private ArrayList<String> TimeIn;
    private ArrayList<String> TimeOut;
    private ArrayList<HistoryObject> historyObjects;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_history);

        initResources();
        initEvents();
    }

    private void initEvents() {
        getParkingHistory();
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void getParkingHistory() {

        StringRequest strRequest = new StringRequest(Request.Method.GET,
                getString(R.string.apiURL) + "getHistory/" + 1,                                //ID
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            JSONObject object = new JSONObject(response);
                            historyObjects = new ArrayList<>();
                            JSONObject main_array = object.getJSONObject("data");
                            //for (int x = 0; x < main_array.length(); x++) {
                                //JSONObject main_Sect = main_array.getJSONObject(x);
                                if((main_array.getString("status")).equals("success") &&
                                        (main_array.getString("message")).equals("You have parking history.")){
                                    Log.w("LOG", "YEY");
                                    Toast.makeText(getApplicationContext(),
                                            "HEREEEEEEEE", Toast.LENGTH_SHORT).show();
                                    JSONArray result = main_array.getJSONArray("history");
                                    for (int i = 0; i < result.length(); i++) {
                                        HistoryObject historyAdapter = new HistoryObject();
                                        JSONObject c = result.getJSONObject(i);
                                        historyAdapter.setBuilding_name(c.getString("building_name"));
                                        historyAdapter.setSlot_id(c.getString("slot_id"));
                                        historyAdapter.setTime_in(c.getString("time_in"));
                                        historyAdapter.setTime_out(c.getString("time_out"));
                                        historyObjects.add(historyAdapter);
                                    }
                                }else if((object.getString("status")).equals("success") &&
                                        (object.getString("message")).equals("You have no parking history.")){
                                    HistoryObject historyAdapter = new HistoryObject();
                                    historyAdapter.setBuilding_name("You have no parking history.");
                                    historyAdapter.setSlot_id("");
                                    historyAdapter.setTime_in("");
                                    historyAdapter.setTime_out("");
                                    historyObjects.add(historyAdapter);
                                }else{
                                    HistoryObject historyAdapter = new HistoryObject();
                                    historyAdapter.setBuilding_name("Failed to get your parking history.");
                                    historyAdapter.setSlot_id("");
                                    historyAdapter.setTime_in("");
                                    historyAdapter.setTime_out("");
                                    historyObjects.add(historyAdapter);
                                }

                                HistoryObjectAdapter adapter = new HistoryObjectAdapter(getApplicationContext(),R.layout.row_layout_history,historyObjects);
                                list_History.setAdapter(adapter);

                            //}
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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

        //try1.setText(historyObjects.toString());
    }

    private void initResources() {
        list_History = (ListView) findViewById(R.id.ParkingHistory_lstHistory);
        backButton = (ImageButton) findViewById(R.id.ParkingHistory_btnBack);
    }

}
