package com.example.afbu.parkking;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
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
import java.util.Map;

public class CarList extends AppCompatActivity {

    private ImageButton btnBackHome, btnAddCar;
    private ListView carListView;
    private ArrayList<CarObject> carObjects;
    private static String TAG = CarList.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_list);
        initResources();
        initEvents();
    }



    private void initEvents() {
        btnBackHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnAddCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gotoAddCar = new Intent(getApplicationContext(), AddCar.class);
                startActivity(gotoAddCar);
            }
        });
        carListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {

                Intent goToCarProfile = new Intent(getApplicationContext(), CarProfile.class);
                goToCarProfile.putExtra("car_id", carObjects.get(i).getId());
                startActivity(goToCarProfile);
                 finish();
            }
        });
    }

    private void initResources() {
        btnBackHome = (ImageButton) findViewById(R.id.CarList_btnBack);
        btnAddCar = (ImageButton) findViewById(R.id.CarList_btnAddCar);
        carListView = (ListView) findViewById(R.id.CarList_carListView);
        getCarList();
    }

    public void getCarList(){
        StringRequest strRequest = new StringRequest(Request.Method.GET, getString(R.string.apiURL) + "cars/2", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    JSONArray result = object.getJSONArray("data");
                    carObjects = new ArrayList<>();

                    for (int i = 0; i < result.length(); i++) {
                        JSONObject c = result.getJSONObject(i);
                        CarObject carObject = new CarObject(c.getString("id"),c.getString("plate_number"),c.getString("model"),c.getString("brand"));
                        carObject.setUsed_by(c.getString("used_by"));
                        carObject.setVehicle_owner_profile_id(c.getString("vehicle_owner_profile_id"));
                        carObjects.add(carObject);
                    }

                    CarObjectAdapter adapter = new CarObjectAdapter(getApplicationContext(),R.layout.adapter_view_layout,carObjects);
                    carListView.setAdapter(adapter);

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
