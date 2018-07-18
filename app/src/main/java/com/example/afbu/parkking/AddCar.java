package com.example.afbu.parkking;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class AddCar extends AppCompatActivity {

    SharedPreferences SharedPreference;
    SharedPreferences.Editor editor;
    private static final String PreferenceName = "UserPreference";
    private static final String PROFID_KEY = "ProfileIDKey";
    private String ProfileID;
    private EditText edtPlateNumber1,edtPlateNumber2, edtVehicleBrand, edtVehicleModel, edtVehicleColor;
    private Button addCar, addImage;
    private Bitmap bitmap;
    private final int IMG_REQUEST = 1;
    private static final int RESULT_LOAD_IMAGE = 0, REQUEST_CAMERA = 1;
    private static String TAG = AddCar.class.getSimpleName();
    private String message;
    private ImageView carImage;
    private AutoCompleteTextView CarBrandsSpinner, CarModelSpinner;
    private ArrayList<String> Brands, Models;
    private ArrayList <Integer> BrandID, ModelID;
    private Integer ChosenModelId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_car);
        SharedPreference = getSharedPreferences(PreferenceName, Context.MODE_PRIVATE);
        if(!SharedPreference.contains(PROFID_KEY)){
            Intent myIntent = new Intent(AddCar.this, StartUp.class);
            startActivity(myIntent);
        }else{
            ProfileID = SharedPreference.getString(PROFID_KEY, "");
        }
        CarBrandsSpinner = (AutoCompleteTextView) findViewById(R.id.AddCar_spinnerVBrand);
        CarModelSpinner = (AutoCompleteTextView) findViewById(R.id.AddCar_spinnerVModel);
        edtPlateNumber1 = (EditText) findViewById(R.id.AddCar_edtPlateNumber1);
        edtPlateNumber2 = (EditText) findViewById(R.id.AddCar_edtPlateNumber2);

        carImage = (ImageView) findViewById(R.id.AddCar_imgVehicle);
        carImage.setVisibility(View.GONE);
        addCar = (Button) findViewById(R.id.AddCar_btnAddCar);
        addImage = (Button) findViewById(R.id.AddCar_btnAddImage);

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // selectImage();
                uploadOptions();
            }
        });

        addCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addCar();
            }
        });
        CarBrandsSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for(int i=0; i<Brands.size(); i++){
                    if(CarBrandsSpinner.getText().toString().equals(Brands.get(i))){
                        getModels(BrandID.get(i));
                        Toast.makeText(getApplicationContext(),
                                Integer.toString(BrandID.get(i)), Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
        });

        CarModelSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                for(int i=0; i<Models.size(); i++){
                    if(CarModelSpinner.getText().toString().equals(Models.get(i))){
                        ChosenModelId = ModelID.get(i);
                        Toast.makeText(getApplicationContext(),
                                Integer.toString(ChosenModelId), Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
        });
        getBrands();

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
    private void getModels(final int brand_id){
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
    private void uploadOptions(){
        final CharSequence[] items  = {"Take photo", "Choose existing photo"};

        AlertDialog.Builder builder = new AlertDialog.Builder(AddCar.this);
        builder.setTitle("Change Profile Picture");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(items[which].equals("Take photo")){

                    Intent gotoCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (gotoCamera.resolveActivity(getPackageManager()) != null){
                        startActivityForResult(gotoCamera, REQUEST_CAMERA);
                    }
                }else if(items[which].equals("Choose existing photo")){
                    Intent gotoGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(gotoGallery, RESULT_LOAD_IMAGE);
                }
            }
        });

        builder.show();
    }
    private void addCar(){
        final String plate;
        final String model;


        final String picture;

        if(!edtPlateNumber1.getText().toString().equals("") && !edtPlateNumber2.getText().toString().equals("")){
            plate = edtPlateNumber1.getText().toString() + "-" + edtPlateNumber2.getText().toString();
        }
        else{
            plate="";
        }


        model = Integer.toString(ChosenModelId);



        if(bitmap!=null){
            picture = imageToString(bitmap);

        }
        else{
            picture=null;
        }

        if(picture!=null && !plate.isEmpty() && !model.isEmpty()){
            StringRequest strRequest = new StringRequest(Request.Method.POST, getString(R.string.addCarURL), new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(TAG, response.toString());
                    JSONObject jsonObject = null;

                    try {
                        jsonObject = new JSONObject(response);
                        message = jsonObject.getString("message");
                        Log.d(TAG, message);
                        Toast.makeText(getApplicationContext(),message, Toast.LENGTH_LONG).show();
                        if(jsonObject.getString("status").equals("success")){
                            Intent gotoCarList = new Intent(getApplicationContext(), CarList.class);
                            startActivity(gotoCarList);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(TAG, "Error: " + error.getMessage());
                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> parameters = new HashMap<String, String>();

                    parameters.put("plate_number", plate);
                    parameters.put("vehicle_owner_id", ProfileID);
                    parameters.put("model_id", model);
                    parameters.put("car_picture", picture );


                    return parameters;
                }
            };
            AppController.getInstance().addToRequestQueue(strRequest);
        }
        else{
            Toast.makeText(getApplicationContext(), "Please Complete all Fields", Toast.LENGTH_SHORT).show();
        }
    }

    private String imageToString(Bitmap bitmap){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte[] imgBytes = byteArrayOutputStream.toByteArray();

        return Base64.encodeToString(imgBytes, Base64.DEFAULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && data != null){
            if(requestCode == RESULT_LOAD_IMAGE){
                Uri path = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),path);
                    carImage.setVisibility(View.VISIBLE);
                    carImage.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else if(requestCode == REQUEST_CAMERA){
                Bundle bundle = data.getExtras();
                bitmap = (Bitmap) bundle.get("data");
                carImage.setVisibility(View.VISIBLE);
                carImage.setImageBitmap(bitmap);
            }
        }
    }
}

