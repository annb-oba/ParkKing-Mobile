package com.example.afbu.parkking;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NotifyDriverConsequenceDialog extends AppCompatDialogFragment {
    private static final String TAG = NotifyDriverConsequenceDialog.class.getSimpleName();
    private String dialogTitle, dialogBody, dialogNote;
    private String purpose;

    private SharedPreferences pendingParkingData;
    private SharedPreferences.Editor pendingParkingDataEditor;
    private static final String PENDING_PARK_DATA_PREF_KEY = "PendingParkingData";
    private static final String PENDING_PARKED_SLOT_ID_KEY = "pendingParkedSlotId";
    private static final String PENDING_PARKED_SLOT_TITLE_KEY = "pendingParkedSlotTitle";
    private static final String FAB_SELECTED_SLOT_ID_KEY = "fabSelectedSlotId";
    private static final String FAB_SELECTED_SLOT_TITLE_KEY = "fabSelectedSlotTitle";

    private Context mContext;

    private SharedPreferences sharedPreferences;

    private static final String PreferenceName = "UserPreference";
    private static final String PROFID_KEY = "ProfileIDKey";
    private View view;

    private AlertDialog.Builder builder;
    private FragmentManager mSupportFragmentManager;

    public void setmSupportFragmentManager(FragmentManager mSupportFragmentManager) {
        this.mSupportFragmentManager = mSupportFragmentManager;
    }

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    public void setDialogTitle(String dialogTitle) {
        this.dialogTitle = dialogTitle;
    }

    public void setDialogBody(String dialogBody) {
        this.dialogBody = dialogBody;
    }

    public void setDialogNote(String dialogNote) {
        this.dialogNote = dialogNote;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        builder = new AlertDialog.Builder(getActivity());
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        view = layoutInflater.inflate(R.layout.dialog_notify_driver_for_discretion, null);
        TextView dialogTitleTextView = (TextView) view.findViewById(R.id.discretionDialogTitle);
        TextView dialogBodyTextView = (TextView) view.findViewById(R.id.discretionDialogBody);
        TextView dialogNoteTextView = (TextView) view.findViewById(R.id.discretionDialogNote);

        dialogTitleTextView.setText(dialogTitle);
        dialogBodyTextView.setText(dialogBody);
        dialogNoteTextView.setText(dialogNote);

        builder.setView(view)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (purpose) {
                            case "switch_car_with_pending_parking":
                            case "select_slot_for_occupancy":
                                saveVehicleLog();
                                break;
                            case "car_already_parked":
                            case "no_car":
                                Intent carListIntent = new Intent(mContext, CarList.class);
                                mContext.startActivity(carListIntent);
                            default:
                                break;
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        return builder.create();
    }

    private void saveVehicleLog() {
        sharedPreferences = getContext().getSharedPreferences(PreferenceName, Context.MODE_PRIVATE);
        pendingParkingData = getContext().getSharedPreferences(PENDING_PARK_DATA_PREF_KEY, Context.MODE_PRIVATE);

        StringRequest strRequest = new StringRequest(Request.Method.POST, getContext().getString(R.string.createVehicleLogURL), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);
                try {
                    JSONObject requestObj = new JSONObject(response);
                    if (!requestObj.getBoolean("success")) {
//                        Toast.makeText(mContext, requestObj.getString("message"), Toast.LENGTH_SHORT).show();

                        NotifyDriverConsequenceDialog notifyDriverConsequenceDialog = new NotifyDriverConsequenceDialog();
                        notifyDriverConsequenceDialog.setmContext(mContext);
                        notifyDriverConsequenceDialog.setPurpose(requestObj.getString("error_type"));
                        notifyDriverConsequenceDialog.setDialogTitle(requestObj.getString("message"));
                        notifyDriverConsequenceDialog.setDialogBody("Click \"Yes\" to proceed to Car List view and switch on the car that you are using");
                        notifyDriverConsequenceDialog.setDialogNote("NOTE: You may be penalized by the building administrator upon exit if you fail to update the car that you are using for the slot in which you parked. You may avoid this problem by going to the Car List page from the navigation tab to select the car that you are using.");
                        notifyDriverConsequenceDialog.show(mSupportFragmentManager, "SlotSavingErrorPreventionDialog");
                    } else {
                        String parkedSlot = "";
                        pendingParkingDataEditor = pendingParkingData.edit();

                        switch (purpose) {
                            case "switch_car_with_pending_parking":
                                parkedSlot = pendingParkingData.getString(PENDING_PARKED_SLOT_TITLE_KEY, "");
                                break;
                            case "select_slot_for_occupancy":
                                parkedSlot = pendingParkingData.getString(FAB_SELECTED_SLOT_TITLE_KEY, "");
                                break;
                        }
                        Toast.makeText(mContext, "This vehicle is now parked in Slot " + parkedSlot, Toast.LENGTH_SHORT).show();

                        pendingParkingDataEditor.clear();
                        pendingParkingDataEditor.apply();

                        Intent parkedCarsIntent = new Intent(mContext, ParkedCars.class);
                        mContext.startActivity(parkedCarsIntent);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(mContext,
                        error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                String slotId = "";
                switch (purpose) {
                    case "switch_car_with_pending_parking":
                        slotId = String.valueOf(pendingParkingData.getInt(PENDING_PARKED_SLOT_ID_KEY, 0));
                    break;
                    case "select_slot_for_occupancy":
                        slotId = String.valueOf(pendingParkingData.getInt(FAB_SELECTED_SLOT_ID_KEY, 0));
                        break;
                    default:
                        break;
                }
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("vehicle_owner_profile_id", sharedPreferences.getString(PROFID_KEY, ""));
                parameters.put("section_slot_id", slotId);
                parameters.put("log_type", "slot");
                return parameters;
            }
        };
        strRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        AppController.getInstance().addToRequestQueue(strRequest);
    }


    @Override
    public void show(FragmentManager manager, String tag) {
        try {
            FragmentTransaction ft = manager.beginTransaction();
            ft.add(this, tag);
            ft.commit();
        } catch (IllegalStateException e) {
            Log.d(tag, "Failed to show prompt");
        }
    }
}
