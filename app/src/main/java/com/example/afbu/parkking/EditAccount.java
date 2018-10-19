package com.example.afbu.parkking;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EditAccount extends AppCompatActivity {

    SharedPreferences SharedPreference;
    SharedPreferences.Editor editor;
    private static final String PreferenceName = "UserPreference";
    private static final String PROFID_KEY = "ProfileIDKey";

    private static String TAG = EditAccount.class.getSimpleName();

    private static final int RESULT_LOAD_IMAGE = 0, REQUEST_CAMERA = 1;

    private ImageButton btnBackHome, btnEditImage;
    private Button btnUpdateAccount;
    private ImageView imgUser;
    private EditText FirstName, MiddleName, LastName,ContactNumber;
    private TextView DeactivateAccount;
    private String ProfilePicture, ProfileID;
    private String Firstname, Lastname, Middlename, Contactnumber, Emailtxt;
    private Bitmap usrimg;
    Uri selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_account);

        SharedPreference = getSharedPreferences(PreferenceName, Context.MODE_PRIVATE);
        if(!SharedPreference.contains(PROFID_KEY)){
            Intent myIntent = new Intent(EditAccount.this, StartUp.class);
            startActivity(myIntent);
        }else{
            ProfileID = SharedPreference.getString(PROFID_KEY, "");
        }

        initResources();
        initEvents();
    }


    private void initEvents() {
        getVehicleOwnerInformation();

        btnBackHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(),
                 //       "lol", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        btnEditImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadOptions();
            }
        });

        btnUpdateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAccount();
            }
        });

        DeactivateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gotoDeact = new Intent(EditAccount.this, DeactivateAccount.class);
                startActivity(gotoDeact);
            }
        });
    }

    private void uploadOptions(){
        final CharSequence[] items  = {"Take photo", "Choose existing photo"};

        AlertDialog.Builder builder = new AlertDialog.Builder(EditAccount.this);
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

    private void initResources() {
        btnBackHome = (ImageButton) findViewById(R.id.EditAccount_btnBack);
        btnEditImage = (ImageButton) findViewById(R.id.EditAccount_btnChangeImg);
        imgUser = (ImageView) findViewById(R.id.EditAccount_imgUser);
        FirstName = (EditText) findViewById(R.id.EditAccount_edtFName);
        MiddleName = (EditText) findViewById(R.id.EditAccount_edtMName);
        LastName = (EditText) findViewById(R.id.EditAccount_edtLName);
        ContactNumber = (EditText) findViewById(R.id.EditAccount_edtCNumber);
        btnUpdateAccount = (Button) findViewById(R.id.EditAccount_btnApply);
        DeactivateAccount = (TextView) findViewById(R.id.EditAccount_txtDeactivate);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && data != null){
            if(requestCode == RESULT_LOAD_IMAGE){
                selectedImage = data.getData();
                try{
                    usrimg = MediaStore.Images.Media.getBitmap(getContentResolver(),selectedImage);
                    imgUser.setImageBitmap(usrimg);
                }catch (IOException e){
                    e.printStackTrace();
                }
                imgUser.setImageURI(selectedImage);
                ProfilePicture = imageToString(usrimg);
            }else if(requestCode == REQUEST_CAMERA){
                Bundle bundle = data.getExtras();
                usrimg = (Bitmap) bundle.get("data");
                imgUser.setImageBitmap(usrimg);
                ProfilePicture = imageToString(usrimg);
            }
        }
    }

    private String imageToString(Bitmap bitmap){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imgByte = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imgByte,  Base64.DEFAULT);
    }

    private void getVehicleOwnerInformation(){
        StringRequest strRequest = new StringRequest(Request.Method.GET,
                getString(R.string.apiURL) + "get_profile_details/" + ProfileID,                     //change id //done
                new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    String status = object.getString("status");
                    if(status.equals("success")){
                        JSONObject userinfo = new JSONObject(object.getString("data"));
                             Firstname = userinfo.getString("first_name");
                            Lastname = userinfo.getString("last_name");
                            Middlename = userinfo.getString("middle_name");
                            Contactnumber = userinfo.getString("contact_number");
                            Emailtxt = object.getString("email");
                            ProfilePicture = userinfo.getString("profile_picture");
                            FirstName.setText(Firstname);
                            LastName.setText(Lastname);
                            MiddleName.setText(Middlename);
                            ContactNumber.setText(Contactnumber);

                        getProfilePicture();
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

    private void getProfilePicture(){
        ImageRequest imgRequest = new ImageRequest(
                getString(R.string.profilepictureURL) + ProfilePicture,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                            imgUser.setImageBitmap(response);
                    }
                }, 0,0, null,new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        AppController.getInstance().addToRequestQueue(imgRequest);
    }

    private void updateAccount(){
        StringRequest strRequest = new StringRequest(Request.Method.PUT,
                getString(R.string.apiURL) + "edit_account", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    String status = object.getString("status");
                    if(status.equals("success")){
                        String message = object.getString("message");
                        Toast.makeText(getApplicationContext(),
                                message, Toast.LENGTH_SHORT).show();
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
                parameters.put("vehicle_owner_id", ProfileID);                                           //change ID //done
                parameters.put("last_name", LastName.getText().toString().trim());
                parameters.put("first_name", FirstName.getText().toString().trim());
                parameters.put("middle_name", MiddleName.getText().toString().trim());
                parameters.put("contact_number", ContactNumber.getText().toString().trim());
                parameters.put("profile_picture", ProfilePicture);
                return parameters;
            }
        };
        AppController.getInstance().addToRequestQueue(strRequest);
    }
}
