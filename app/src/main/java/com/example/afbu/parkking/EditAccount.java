package com.example.afbu.parkking;

import android.content.DialogInterface;
import android.content.Intent;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EditAccount extends AppCompatActivity {

    private static String TAG = EditAccount.class.getSimpleName();

    private static final int RESULT_LOAD_IMAGE = 0, REQUEST_CAMERA = 1;

    private ImageButton btnBackHome, btnEditImage;
    private Button btnUpdateAccount;
    private ImageView imgUser;
    private EditText FirstName, MiddleName, LastName, Email, ContactNumber;
    private String ProfilePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_account);

        initResources();
        initEvents();
    }


    private void initEvents() {
        getVehicleOwnerInformation();
        getProfilePicture();

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
        Email = (EditText) findViewById(R.id.EditAccount_edtEmail);
        btnUpdateAccount = (Button) findViewById(R.id.EditAccount_btnApply);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && data != null){
            if(requestCode == RESULT_LOAD_IMAGE){
                Uri selectedImage = data.getData();
                imgUser.setImageURI(selectedImage);
            }else if(requestCode == REQUEST_CAMERA){
                Bundle bundle = data.getExtras();
                final Bitmap bmp = (Bitmap) bundle.get("data");
                imgUser.setImageBitmap(bmp);
            }
        }
    }

    private void getVehicleOwnerInformation(){
        StringRequest strRequest = new StringRequest(Request.Method.GET,
                getString(R.string.apiURL) + "get_profile_details/" + "id",                     //change id
                new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    String status = object.getString("status");
                    if(status.equals("success")){
                        JSONArray result = object.getJSONArray("data");
                        for (int i = 0; i < result.length(); i++) {
                            JSONObject c = result.getJSONObject(i);
                            FirstName.setText(c.getString("first_name"));
                            LastName.setText(c.getString("last_name"));
                            MiddleName.setText(c.getString("middle_name"));
                            ContactNumber.setText(c.getString("contact_number"));
                            Email.setText(c.getString("email"));
                            ProfilePicture = c.getString("profile_picture");
                        }
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
        StringRequest strRequest = new StringRequest(Request.Method.POST,
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
                parameters.put("vehicle_owner_id", "id");                                           //change ID
                parameters.put("last_name", FirstName.getText().toString().trim());
                parameters.put("first_name", LastName.getText().toString().trim());
                parameters.put("middle_name", MiddleName.getText().toString().trim());
                parameters.put("contact_number", ContactNumber.getText().toString().trim());
                parameters.put("email", Email.getText().toString().trim());
                return parameters;
            }
        };
        AppController.getInstance().addToRequestQueue(strRequest);
    }
}
