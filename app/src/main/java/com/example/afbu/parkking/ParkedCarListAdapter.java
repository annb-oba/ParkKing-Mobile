package com.example.afbu.parkking;

import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParkedCarListAdapter extends RecyclerView.Adapter<ParkedCarListAdapter.ParkedCarViewHolder> {
    private static final String TAG = ParkedCarListAdapter.class.getSimpleName();
    private List<ParkedCar> parkedCarList;
    private Context mCtx;

    public ParkedCarListAdapter(List<ParkedCar> parkedCarList, Context mCtx) {
        this.parkedCarList = parkedCarList;
        this.mCtx = mCtx;
    }

    @NonNull
    @Override
    public ParkedCarListAdapter.ParkedCarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.from(parent.getContext()).inflate(R.layout.parked_car_item_layout, parent, false);

        return new ParkedCarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParkedCarListAdapter.ParkedCarViewHolder holder, int position) {
        final ParkedCar parkedCar = parkedCarList.get(position);

        holder.plateNumberTextView.setText(parkedCar.getPlate_number());
        holder.carMakeTextView.setText(parkedCar.getCar_make());

        holder.buildingParkedTextView.setText(parkedCar.getBuilding());
        holder.floorParkedTextView.setText("Floor " + parkedCar.getFloor() + "; Section " + parkedCar.getSection() + "; Slot " + parkedCar.getSlot());

        holder.dateParkedTextView.setText(parkedCar.getTime_in());
        holder.billTextView.setText(parkedCar.getBill());

        if (!parkedCar.isCurrently_used()) {
            holder.isUsedImageView.setVisibility(View.INVISIBLE);
        }

        holder.self.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParkedCarInfo(parkedCar.getVehicle_log_id());
            }
        });
    }

    private void getParkedCarInfo(int vehicle_log_id) {
        StringRequest strRequest = new StringRequest(Request.Method.GET, mCtx.getString(R.string.getVehicleLogURL) + String.valueOf(vehicle_log_id), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, response.toString());
                try {
                    JSONObject responseObj = new JSONObject(response);
                    if(responseObj.getBoolean("success")) {
                        getBuildingFloorRouters(responseObj.getJSONObject("vehicle_log_data"));
                    } else {
                        Toast.makeText(mCtx, responseObj.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(mCtx,
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

    public void getBuildingFloorRouters(JSONObject vehicle_log_data) {
        String building_id = "";
        String slot_id = "";
        try {
            building_id = vehicle_log_data.getString("building_id");
            slot_id = vehicle_log_data.getString("slot");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final String finalBuilding_id = building_id;
        final String finalSlot_id = slot_id;
        StringRequest strRequest = new StringRequest(Request.Method.GET, mCtx.getString(R.string.apiURL) + "get_building_floor_routers/" + building_id, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                final String tempResponse = response;
                    Intent myIntent = new Intent(mCtx, FloorMap.class);
                    myIntent.putExtra("floor_info", tempResponse);
                    myIntent.putExtra("building_id", finalBuilding_id);
                    myIntent.putExtra("slot_id", finalSlot_id);
                    myIntent.putExtra("intent","view_parked");
                    myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mCtx.startActivity(myIntent);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(mCtx,
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
    public int getItemCount() {
        return parkedCarList.size();
    }

    public class ParkedCarViewHolder extends RecyclerView.ViewHolder {
        TextView plateNumberTextView, carMakeTextView;
        TextView buildingParkedTextView, floorParkedTextView;
        TextView dateParkedTextView, billTextView;
        ImageView isUsedImageView;
        View self;

        public ParkedCarViewHolder(View itemView) {
            super(itemView);

            self = itemView;

            plateNumberTextView = (TextView) itemView.findViewById(R.id.plateNumberTextView);
            carMakeTextView = (TextView) itemView.findViewById(R.id.carMakeTextView);

            buildingParkedTextView = (TextView) itemView.findViewById(R.id.buildingParkedTextView);
            floorParkedTextView = (TextView) itemView.findViewById(R.id.floorParkedTextView);
            dateParkedTextView = (TextView) itemView.findViewById(R.id.dateParkedTextView);
            billTextView = (TextView) itemView.findViewById(R.id.billTextView);

            isUsedImageView = (ImageView) itemView.findViewById(R.id.isUsedImageView);
        }
    }
}
