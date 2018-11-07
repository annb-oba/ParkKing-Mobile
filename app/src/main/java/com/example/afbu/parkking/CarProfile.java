package com.example.afbu.parkking;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CarProfile extends AppCompatActivity {
    SharedPreferences SharedPreference;
    SharedPreferences.Editor editor;
    private static final String PreferenceName = "UserPreference";
    private static final String PROFID_KEY = "ProfileIDKey";
    private final int IMG_REQUEST = 1;
    private static final int PIC_CROP = 2;
    private static final int RESULT_LOAD_IMAGE = 0, REQUEST_CAMERA = 1;
    private String ProfileID;
    private String carID, message;
    private ImageButton btnBackHome, addImage;
    private EditText editTextPlateNumber1, editTextPlateNumber2;
    private AutoCompleteTextView CarBrandsSpinner, CarModelSpinner;
    private LinearLayout mainLayout, carSharingLayout, removeCarSharingLayout, deactivateCarLayout;
    private RelativeLayout loadingLayout;
    private View dividerView;
    private ArrayList<String> Brands, Models;
    private ArrayList<Integer> BrandID, ModelID;
    private Integer ChosenModelId;
    private TextView txtCarOwner;
    private Button btnSave;
    private ImageView carImage;
    private Switch switchActiveCar;
    private static String TAG = CarProfile.class.getSimpleName();
    private Boolean activeCar;
    private Bitmap bitmap = null;

    private Boolean pictureChanged = false;
    private Boolean originalActiveCar;
    private String originalPlateNumber;

    private SharedPreferences pendingParkingData;
    private SharedPreferences.Editor pendingParkingDataEditor;
    private static final String PENDING_PARK_DATA_PREF_KEY = "PendingParkingData";
    private static final String PENDING_PARKED_SLOT_ID_KEY = "pendingParkedSlotId";
    private static final String PENDING_PARKED_SLOT_TITLE_KEY = "pendingParkedSlotTitle";
    private static final String FAB_SELECTED_SLOT_ID_KEY = "fabSelectedSlotId";
    private static final String FAB_SELECTED_SLOT_TITLE_KEY = "fabSelectedSlotTitle";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_profile);
        initResources();
        initEvents();
        getCarData();
        getBrands();
    }

    public void initResources() {
        SharedPreference = getSharedPreferences(PreferenceName, Context.MODE_PRIVATE);
        if (!SharedPreference.contains(PROFID_KEY)) {
            Intent myIntent = new Intent(CarProfile.this, StartUp.class);
            startActivity(myIntent);
        } else {
            ProfileID = SharedPreference.getString(PROFID_KEY, "");
        }
        deactivateCarLayout = (LinearLayout) findViewById(R.id.CarProfile_DeactivateCarLayout);
        removeCarSharingLayout = (LinearLayout) findViewById(R.id.CarProfile_RemoveCarSharingLayout);
        carSharingLayout = (LinearLayout) findViewById(R.id.CarProfile_CarSharingLayout);
        dividerView = (View) findViewById(R.id.CarProfile_DividerView);
        mainLayout = (LinearLayout) findViewById(R.id.CarProfile_mainLayout);
        loadingLayout = (RelativeLayout) findViewById(R.id.CarProfile_ProgressBarLayout);
        btnSave = (Button) findViewById(R.id.CarProfile_btnSaveChanges);
        addImage = (ImageButton) findViewById(R.id.CarProfile_btnChangeImg);
        btnBackHome = (ImageButton) findViewById(R.id.CarProfile_btnBack);
        editTextPlateNumber1 = (EditText) findViewById(R.id.CarProfile_edtPlateNumber1);
        editTextPlateNumber2 = (EditText) findViewById(R.id.CarProfile_edtPlateNumber2);
        CarBrandsSpinner = (AutoCompleteTextView) findViewById(R.id.CarProfile_spinnerVBrand);
        CarModelSpinner = (AutoCompleteTextView) findViewById(R.id.CarProfile_spinnerVModel);
        txtCarOwner = (TextView) findViewById(R.id.CarProfile_carOwner);
        switchActiveCar = (Switch) findViewById(R.id.CarProfile_switchActiveCar);
        carImage = (ImageView) findViewById(R.id.CarProfile_imgCar);
        Intent intent = getIntent();
        carID = intent.getStringExtra("car_id");
    }

    public void initEvents() {
        removeCarSharingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeCarSharing();
            }
        });
        deactivateCarLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new android.app.AlertDialog.Builder(CarProfile.this)
                        .setTitle("Confirm Car Deactivation")
                        .setMessage("Are you sure want to Deactivate this car? You and everyone sharing this car will no longer be able to use it after you deactivate it. Do you want to proceed?")
                        .setPositiveButton("Deactivate", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                deactivateCar();
                            }
                        })
                        .setNegativeButton("Cancel", null).show();
            }
        });
        carSharingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CarCoOwners.class);
                intent.putExtra("car_id", carID);
                startActivity(intent);
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChanges();
            }
        });
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadOptions();
            }
        });
        switchActiveCar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    activeCar = true;
                } else {
                    activeCar = false;
                }
            }
        });
        CarBrandsSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (int i = 0; i < Brands.size(); i++) {
                    if (CarBrandsSpinner.getText().toString().equals(Brands.get(i))) {
                        getModels(BrandID.get(i));
                        ChosenModelId = null;
                        CarModelSpinner.setText("");
//                        Toast.makeText(getApplicationContext(),
//                                Integer.toString(BrandID.get(i)), Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
        });

        CarModelSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                for (int i = 0; i < Models.size(); i++) {
                    if (CarModelSpinner.getText().toString().equals(Models.get(i))) {
                        ChosenModelId = ModelID.get(i);
//                        Toast.makeText(getApplicationContext(),
//                                Integer.toString(ChosenModelId), Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
        });
        btnBackHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent gotoCarList = new Intent(getApplicationContext(), CarList.class);
                //startActivity(gotoCarList);
                finish();
            }
        });
    }
    public void removeCarSharing(){
        new android.app.AlertDialog.Builder(CarProfile.this)
                .setTitle("Revoke Co-Ownership")
                .setMessage("Are you sure want to revoke your co ownership of this car?")
                .setPositiveButton("Revoke", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        StringRequest strRequest = new StringRequest(Request.Method.POST,  getString(R.string.revokeCoOwnershipURL), new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d(TAG, response.toString());
                                JSONObject jsonObject = null;

                                try {
                                    jsonObject = new JSONObject(response);
                                    String message = jsonObject.getString("message");
                                    Log.d(TAG, message);
                                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                                    if (jsonObject.getString("status").equals("success")) {
                                        finish();
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
                                        "Unable to connect to Park King Servers", Toast.LENGTH_SHORT).show();
                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> parameters = new HashMap<String, String>();
                                parameters.put("car_id", carID);
                                parameters.put("co_owner_id", ProfileID);
                                return parameters;
                            }
                        };
                        AppController.getInstance().addToRequestQueue(strRequest);
                    }})
                .setNegativeButton("Cancel", null).show();
    }
    public void deactivateCar() {
        StringRequest strRequest = new StringRequest(Request.Method.POST, getString(R.string.deactivateCarURL), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, response.toString());
                JSONObject jsonObject = null;

                try {
                    jsonObject = new JSONObject(response);
                    String message = jsonObject.getString("message");
                    Log.d(TAG, message);
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                    if (jsonObject.getString("status").equals("success")) {
                        finish();
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
                parameters.put("car_id", carID);
                parameters.put("vehicle_owner_id", ProfileID);
                return parameters;
            }
        };
        AppController.getInstance().addToRequestQueue(strRequest);
    }

    public void saveChanges() {
        final String plate;
        final String model;
        final String picture;
        final String activeCar;
        if (originalPlateNumber.equals(editTextPlateNumber1.getText().toString() + "-" + editTextPlateNumber2.getText().toString())) {
            plate = "";
        } else {
            plate = editTextPlateNumber1.getText().toString() + "-" + editTextPlateNumber2.getText().toString();
        }
        if (ChosenModelId != null) {
            model = Integer.toString(ChosenModelId);
        } else {
            model = "";
        }
        if (bitmap != null) {
            picture = imageToString(bitmap);
        } else {
            picture = "";
        }
        if (originalActiveCar != this.activeCar) {
            if (this.activeCar) {
                activeCar = "true";
            } else {
                activeCar = "false";
            }
        } else {
            activeCar = "";
        }

        if (!picture.isEmpty() || !model.isEmpty() || !plate.isEmpty()) {
            StringRequest strRequest = new StringRequest(Request.Method.POST, getString(R.string.editCarInfoURL), new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(TAG, response.toString());
                    JSONObject jsonObject = null;

                    try {
                        jsonObject = new JSONObject(response);
                        String message = jsonObject.getString("message");
                        Log.d(TAG, message);
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        if (jsonObject.getString("status").equals("success")) {
                            //finish();
                            bitmap = null;
                            ChosenModelId = null;
                            if (!editTextPlateNumber1.getText().toString().equals("") && !editTextPlateNumber2.getText().toString().equals("")) {
                                originalPlateNumber = editTextPlateNumber1.getText().toString() + "-" + editTextPlateNumber2.getText().toString();
                            } else {
                                originalPlateNumber = null;
                            }
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
                            "Could not connect to ParkKing servers", Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> parameters = new HashMap<String, String>();
                    parameters.put("car_id", carID);
                    parameters.put("model_id", model);
                    parameters.put("plate_number", plate);
                    parameters.put("car_picture", picture);
//                parameters.put("active_car", activeCar);
                    parameters.put("profile_id", ProfileID);
                    return parameters;
                }
            };
            AppController.getInstance().addToRequestQueue(strRequest);
        }
        if (originalActiveCar != this.activeCar) {
            changeCar(activeCar);
        }
        if (originalActiveCar == this.activeCar && picture.isEmpty() && plate.isEmpty() && model.isEmpty()) {
            Toast.makeText(getApplicationContext(), "No Changes Made", Toast.LENGTH_SHORT).show();
        }
    }

    public void changeCar(final String activeCar) {

        if (!carID.equals("")) {
            StringRequest strRequest = new StringRequest(Request.Method.POST, getString(R.string.changeCarURL), new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(TAG, response.toString());
                    JSONObject jsonObject = null;

                    try {
                        jsonObject = new JSONObject(response);
                        String message = jsonObject.getString("message");
                        Log.d(TAG, message);
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        if (jsonObject.getString("status").equals("success")) {
                            if (jsonObject.has("has_set_existing") && jsonObject.getBoolean("has_set_existing")) {
                                pendingParkingData = getSharedPreferences(PENDING_PARK_DATA_PREF_KEY, Context.MODE_PRIVATE);

                                // if there is a pending slot either occupied or selected
                                if ((pendingParkingData.contains(PENDING_PARKED_SLOT_ID_KEY) && pendingParkingData.contains(PENDING_PARKED_SLOT_TITLE_KEY)) || (pendingParkingData.contains(FAB_SELECTED_SLOT_ID_KEY) && pendingParkingData.contains(FAB_SELECTED_SLOT_TITLE_KEY))) {
                                    // create dialog
                                    NotifyDriverConsequenceDialog notifyDriverConsequenceDialog = new NotifyDriverConsequenceDialog();
                                    notifyDriverConsequenceDialog.setDialogTitle("Set car to parked slot");
                                    notifyDriverConsequenceDialog.setDialogNote("NOTE: You may be penalized by the building administrator upon exit if you fail to update the car that you are using for the slot in which you parked.");

                                    // if slot is pending
                                    if (pendingParkingData.contains(PENDING_PARKED_SLOT_ID_KEY) && pendingParkingData.contains(PENDING_PARKED_SLOT_TITLE_KEY)) {
                                        notifyDriverConsequenceDialog.setDialogBody("Is this the car that you have used to park in Slot " + pendingParkingData.getString(PENDING_PARKED_SLOT_TITLE_KEY, "") + "?");
                                        notifyDriverConsequenceDialog.setPurpose("switch_car_with_pending_parking");
                                    }
                                    // if slot is selected
                                    else if (pendingParkingData.contains(FAB_SELECTED_SLOT_ID_KEY) && pendingParkingData.contains(FAB_SELECTED_SLOT_TITLE_KEY)) {
                                        notifyDriverConsequenceDialog.setDialogBody("Is this the car that you have used to park in the slot that you have selected? (Slot " + pendingParkingData.getString(FAB_SELECTED_SLOT_TITLE_KEY, "") + ")");
                                        notifyDriverConsequenceDialog.setPurpose("select_slot_for_occupancy");
                                    }

                                    notifyDriverConsequenceDialog.setmContext(getApplicationContext());
                                    notifyDriverConsequenceDialog.setmSupportFragmentManager(getSupportFragmentManager());
                                    notifyDriverConsequenceDialog.show(getSupportFragmentManager(), "NotifyDiscretionForChangeCarDialog");
                                }
                            }
                            if (switchActiveCar.isChecked()) {
                                switchActiveCar.setChecked(true);
                                originalActiveCar = true;
                            } else {
                                switchActiveCar.setChecked(false);
                                originalActiveCar = false;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(TAG, "Error: " + error.getMessage());
                    Toast.makeText(getApplicationContext(), "Server Error", Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> parameters = new HashMap<String, String>();


                    parameters.put("vehicle_owner_id", ProfileID);
                    parameters.put("id", carID);
                    parameters.put("active_car", activeCar);


                    return parameters;
                }
            };
            AppController.getInstance().addToRequestQueue(strRequest);

        }

    }

    public void getCarData() {
        StringRequest strRequest = new StringRequest(Request.Method.POST, getString(R.string.getCarInfoURL), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    message = object.getString("status");

                    if (message.equals("success")) {
                        loadingLayout.setVisibility(View.GONE);
                        mainLayout.setVisibility(View.VISIBLE);
                        JSONObject c = new JSONObject(object.getString("data"));
                        String tempPlate1, tempPlate2;
                        String[] tempPlatenumber = c.getString("plate_number").toString().split("-");
                        if (tempPlatenumber.length == 2) {
                            tempPlate1 = tempPlatenumber[0];
                            tempPlate2 = tempPlatenumber[1];
                            editTextPlateNumber1.setText(tempPlate1);
                            editTextPlateNumber2.setText(tempPlate2);
                            if (!editTextPlateNumber1.getText().toString().equals("") && !editTextPlateNumber2.getText().toString().equals("")) {
                                originalPlateNumber = editTextPlateNumber1.getText().toString() + "-" + editTextPlateNumber2.getText().toString();
                            } else {
                                originalPlateNumber = null;
                            }
                        }
                        if (c.get("active").equals("true")) {
                            switchActiveCar.setChecked(true);
                            originalActiveCar = true;
                            activeCar = true;
                        } else {
                            switchActiveCar.setChecked(false);
                            originalActiveCar = false;
                            activeCar = false;
                        }

                        CarBrandsSpinner.setText(c.getString("brand"));
                        CarModelSpinner.setText(c.getString("model"));
                        txtCarOwner.setText(c.getString("first_name") + " " + c.getString("last_name"));
                        Glide.with(getApplicationContext()).asBitmap().load(getString(R.string.carImagesURL) + c.getString("car_picture")).into(carImage);
                        if (c.get("owner").equals("false")) {
                            addImage.setVisibility(View.GONE);
                            disableEditText(editTextPlateNumber1);
                            disableEditText(editTextPlateNumber2);
                            disableEditText(CarModelSpinner);
                            disableEditText(CarBrandsSpinner);
                            carSharingLayout.setVisibility(View.GONE);
                            deactivateCarLayout.setVisibility(View.GONE);
                            removeCarSharingLayout.setVisibility(View.VISIBLE);
                        }
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
                        "Server Error", Toast.LENGTH_SHORT).show();
                finish();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("car_id", carID);
                parameters.put("profile_id", ProfileID);
                return parameters;
            }
        };
        AppController.getInstance().addToRequestQueue(strRequest);
    }

    private void getBrands() {
        StringRequest strRequest = new StringRequest(Request.Method.GET, getString(R.string.apiURL) + "brands", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    JSONArray result = object.getJSONArray("data");
                    Brands = new ArrayList<>();
                    BrandID = new ArrayList<>();
                    for (int i = 0; i < result.length(); i++) {
                        JSONObject c = result.getJSONObject(i);
                        Brands.add(c.getString("brand"));
                        BrandID.add(c.getInt("id"));
                    }
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_layout, Brands);
                    dataAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                    CarBrandsSpinner.setThreshold(1);
                    CarBrandsSpinner.setAdapter(dataAdapter);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        "Server Error", Toast.LENGTH_SHORT).show();
                finish();
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

    private void getModels(final int brand_id) {
        StringRequest strRequest = new StringRequest(Request.Method.GET, getString(R.string.apiURL) + "brand/" + brand_id + "/models", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    JSONArray result = object.getJSONArray("infos");
                    Models = new ArrayList<>();
                    ModelID = new ArrayList<>();
                    for (int i = 0; i < result.length(); i++) {
                        JSONObject c = result.getJSONObject(i);
                        Models.add(c.getString("model"));
                        ModelID.add(c.getInt("id"));
                    }

                    ArrayAdapter<String> dataAdapter1 = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_layout, Models);
                    dataAdapter1.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                    CarModelSpinner.setThreshold(1);
                    CarModelSpinner.setAdapter(dataAdapter1);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        "Server Error", Toast.LENGTH_SHORT).show();
                finish();
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

    private String imageToString(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] imgBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imgBytes, Base64.DEFAULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == RESULT_LOAD_IMAGE) {
                Uri path = data.getData();
                performCrop(path);
//                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),path);
//                    carImage.setVisibility(View.VISIBLE);
//                    carImage.setImageBitmap(bitmap);
            } else if (requestCode == REQUEST_CAMERA) {
                Uri picUri = data.getData();
                performCrop(picUri);
//                Bundle bundle = data.getExtras();
//                bitmap = (Bitmap) bundle.get("data");
//                carImage.setVisibility(View.VISIBLE);
//                carImage.setImageBitmap(bitmap);
            } else if (requestCode == PIC_CROP) {
                if (data != null) {

                    Bundle bundle = data.getExtras();
//                    bitmap = (Bitmap) bundle.getParcelable("data");
                    bitmap = (Bitmap) bundle.get("data");
                    carImage.setVisibility(View.VISIBLE);
                    carImage.setImageBitmap(bitmap);
                    pictureChanged = true;
                }
            }
        }
    }

    private void uploadOptions() {
        final CharSequence[] items = {"Take photo", "Choose existing photo"};

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(CarProfile.this);
        builder.setTitle("Change Profile Picture");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (items[which].equals("Take photo")) {

                    Intent gotoCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (gotoCamera.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(gotoCamera, REQUEST_CAMERA);
                    }
                } else if (items[which].equals("Choose existing photo")) {
                    Intent gotoGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(gotoGallery, RESULT_LOAD_IMAGE);
                }
            }
        });

        builder.show();
    }

    private void performCrop(Uri picUri) {
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            // indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image/*");
            // set crop properties here
            cropIntent.putExtra("crop", true);
            // indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            // indicate output X and Y
            cropIntent.putExtra("outputX", 256);
            cropIntent.putExtra("outputY", 256);
            // retrieve data on return
            cropIntent.putExtra("return-data", true);
            // start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, PIC_CROP);
        }
        // respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException anfe) {
            // display an error message
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private void disableEditText(EditText editText) {
        editText.setFocusable(false);
        editText.setEnabled(false);
        editText.setCursorVisible(false);
        editText.setKeyListener(null);
        editText.setTextColor(getResources().getColor(R.color.colorBlack));
    }
}
