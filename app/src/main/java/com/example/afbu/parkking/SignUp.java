package com.example.afbu.parkking;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity{

    SharedPreferences SharedPreference;
    SharedPreferences.Editor editor;
    private static final String PreferenceName = "UserPreference";
    private static final String PROFID_KEY = "ProfileIDKey";

    private static final int RESULT_LOAD_IMAGE = 0, REQUEST_CAMERA = 1;
    private static String TAG = SignUp.class.getSimpleName();
    private int BTN_CARIMG, BTN_USERIMG;
    private String FIlENAME;

    private EditText FirstName, LastName, MiddleName, Email, CNumber, PlateNumber, Password;
    private AutoCompleteTextView CarBrandsSpinner, CarModelSpinner;
    private ArrayList <String> Brands, Models;
    private ArrayList <Integer> BrandID, ModelID;
    private Button btnSignUp, btnCarImg;
    private ImageButton btnUserImg;
    private ImageView imgUser, imgCar;
    private Integer ChosenModelId;
    private Bitmap carimg, usrimg;
    Uri selectedImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        SharedPreference = getSharedPreferences(PreferenceName, Context.MODE_PRIVATE);
        if(SharedPreference.contains(PROFID_KEY)){
            Intent myIntent = new Intent(SignUp.this, Home.class);
            startActivity(myIntent);
        }

        initResources();
        initEvents();
    }

    private void initResources(){
        CarBrandsSpinner = (AutoCompleteTextView) findViewById(R.id.SignUp_spinnerVBrand);
        CarModelSpinner = (AutoCompleteTextView) findViewById(R.id.SignUp_spinnerVModel);
        btnSignUp = (Button) findViewById(R.id.SignUp_btnSignUp);
        FirstName = (EditText) findViewById(R.id.SignUp_edtFName);
        LastName = (EditText) findViewById(R.id.SignUp_edtLName);
        Email = (EditText) findViewById(R.id.SignUp_edtEmail);
        CNumber = (EditText) findViewById(R.id.SignUp_edtCNumber);
        PlateNumber = (EditText) findViewById(R.id.SignUp_edtPlateNumber);
        btnCarImg = (Button) findViewById(R.id.SignUp_btnVehiclePicture);
        btnUserImg = (ImageButton) findViewById(R.id.SignUp_btnProfilePicture);
        imgUser = (ImageView) findViewById(R.id.SignUp_imgUser);
        imgCar = (ImageView) findViewById(R.id.SignUp_imgVehicle);
        Password = (EditText) findViewById(R.id.SignUp_edtPassword);
        MiddleName = (EditText) findViewById(R.id.SignUp_edtMName);

        imgCar.setVisibility(View.GONE);

        getBrands();
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

                    ArrayAdapter<String> dataAdapter1 = new ArrayAdapter<String>(getApplicationContext(), R.layout.support_simple_spinner_dropdown_item, Models);
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
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.support_simple_spinner_dropdown_item, Brands);
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

    private void initEvents(){
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(FirstName.getText().toString() != null && LastName.getText().toString() != null
                        &&MiddleName.getText().toString() != null &&CNumber.getText().toString() != null
                        &&PlateNumber.getText().toString() != null &&Email.getText().toString() != null
                        &&Password.getText().toString() != null && imageToString(usrimg) != null
                        && imageToString(carimg) != null){
                    signUp();
                }else{
                    Toast.makeText(getApplicationContext(), "Please complete all fields.",Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCarImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BTN_CARIMG = 1;
                BTN_USERIMG = 0;
                uploadOptions();
            }
        });

        btnUserImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BTN_USERIMG = 1;
                BTN_CARIMG = 0;
                uploadOptions();
            }
        });

        CarBrandsSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getModels(BrandID.get(position));
            }
        });

        CarModelSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChosenModelId = ModelID.get(position);
            }
        });

    }


    private void uploadOptions(){
        final CharSequence[] items  = {"Take photo", "Choose existing photo"};

        AlertDialog.Builder builder = new AlertDialog.Builder(SignUp.this);
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

    private void signUp(){
        StringRequest strRequest = new StringRequest(Request.Method.POST, getString(R.string.apiURL) + "signupvehicleowner", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    String status = object.getString("status");
                    if(status.equals("success")){
                        String vehicleowner_id = object.getString("vehicle_owner_id");
                        editor = SharedPreference.edit();
                        editor.putString(PROFID_KEY, vehicleowner_id);
                        if(editor.commit()){
                            Intent gotoHome = new Intent(getApplicationContext(), Home.class);
                            startActivity(gotoHome);
                        }
                    }else if(status.equals("failed")){
                        String message = object.getString("message");
                        Toast.makeText(getApplicationContext(),
                                message, Toast.LENGTH_SHORT).show();
                    }

                    //Toast.makeText(getApplicationContext(), status, Toast.LENGTH_SHORT).show();
                    /*String status = object.getString("status");
                    String VehiclOwnerID = object.getString("vehicle_owner_id");
                    String UserID = object.getString("user_id");
                    if(status.equals("success")){
                        String filename = "profile_picture_"+VehiclOwnerID;
                        /*Intent gotoHome = new Intent(getApplicationContext(), Home.class);
                        startActivity(gotoHome);
                    }else{
                        Toast.makeText(getApplicationContext(),
                                "Sign Up failed.", Toast.LENGTH_SHORT).show();
                    }*/
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
                parameters.put("last_name", FirstName.getText().toString().trim());
                parameters.put("first_name", LastName.getText().toString().trim());
                parameters.put("middle_name", MiddleName.getText().toString().trim());
                parameters.put("contact_number", CNumber.getText().toString().trim());
                parameters.put("email", Email.getText().toString().trim());
                parameters.put("password", Password.getText().toString().trim());
                parameters.put("model_id", Integer.toString(ChosenModelId).trim());
                parameters.put("plate_number", PlateNumber.getText().toString().trim());
                parameters.put("profile_picture", imageToString(usrimg));
                parameters.put("vehicle_picture", imageToString(carimg));

                return parameters;
            }
        };
        AppController.getInstance().addToRequestQueue(strRequest);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && data != null){
            if(requestCode == RESULT_LOAD_IMAGE){
                if(BTN_CARIMG == 1){
                    selectedImage = data.getData();
                    try{
                        carimg = MediaStore.Images.Media.getBitmap(getContentResolver(),selectedImage);
                        imgCar.setVisibility(View.VISIBLE);
                        imgCar.setImageBitmap(carimg);
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }else if(BTN_USERIMG == 1){
                    selectedImage = data.getData();
                    try{
                        usrimg = MediaStore.Images.Media.getBitmap(getContentResolver(),selectedImage);
                        imgUser.setImageBitmap(usrimg);
                    }catch (IOException e){
                        e.printStackTrace();
                    }                }
            }else if(requestCode == REQUEST_CAMERA){
                if(BTN_CARIMG == 1){
                    Bundle bundle = data.getExtras();
                    carimg = (Bitmap) bundle.get("data");
                    imgCar.setVisibility(View.VISIBLE);
                    imgCar.setImageBitmap(carimg);

                    /*Bundle bundle = data.getExtras();
                    carimg = (Bitmap) bundle.get("data");
                    String PATH = Environment.getExternalStorageDirectory().getPath()+ carimg;
                    File f = new File(PATH);
                    selectedImage= Uri.fromFile(f);
                    cropImage();*/

                }else if(BTN_USERIMG == 1){
                    Bundle bundle = data.getExtras();
                    usrimg = (Bitmap) bundle.get("data");
                    imgUser.setImageBitmap(usrimg);
                }
            }
        }
    }

    private String imageToString(Bitmap bitmap){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imgByte = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imgByte,  Base64.DEFAULT);
    }

   /* private void cropImage(){
        try{
            Intent CropImage = new Intent("com.android.camera.action.CROP");
            CropImage.setDataAndType(selectedImage, "image/*");
            CropImage.putExtra("crop", true);
            CropImage.putExtra("OutputX",180);
            CropImage.putExtra("OutputY",180);
            CropImage.putExtra("AspectX",3);
            CropImage.putExtra("AspectY",4);
            CropImage.putExtra("ScaleUpIfneeded",true);
            CropImage.putExtra("return-data",true );

            startActivityForResult(CropImage, 1);

        }catch(ActivityNotFoundException e){

        }

    }*/
}
