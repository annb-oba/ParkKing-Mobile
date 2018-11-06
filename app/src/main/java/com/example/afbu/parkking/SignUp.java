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
import android.util.Log;
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

public class SignUp extends AppCompatActivity {

    SharedPreferences SharedPreference;
    SharedPreferences.Editor editor;
    private static final String PreferenceName = "UserPreference";
    private static final String PROFID_KEY = "ProfileIDKey";
    private static final int PIC_CROP = 2;
    private static final int RESULT_LOAD_IMAGE = 0, REQUEST_CAMERA = 1;
    private static String TAG = SignUp.class.getSimpleName();
    private int  BTN_USERIMG;
    private String FIlENAME;

    private EditText FirstName, LastName, MiddleName, Email, CNumber, Password;
    private Button btnSignUp;
    private ImageButton btnUserImg;
    private ImageView imgUser;
    private Bitmap usrimg;
    private TextView EULA;
    Uri selectedImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        SharedPreference = getSharedPreferences(PreferenceName, Context.MODE_PRIVATE);

        initResources();
        initEvents();
    }

    private void initResources() {
        btnSignUp = (Button) findViewById(R.id.SignUp_btnSignUp);
        FirstName = (EditText) findViewById(R.id.SignUp_edtFName);
        LastName = (EditText) findViewById(R.id.SignUp_edtLName);
        Email = (EditText) findViewById(R.id.SignUp_edtEmail);
        CNumber = (EditText) findViewById(R.id.SignUp_edtCNumber);
        btnUserImg = (ImageButton) findViewById(R.id.SignUp_btnProfilePicture);
        imgUser = (ImageView) findViewById(R.id.SignUp_imgUser);
        MiddleName = (EditText) findViewById(R.id.SignUp_edtMName);
        EULA = (TextView) findViewById(R.id.SignUp_txtEula2);
    }


    private void initEvents() {

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!FirstName.getText().toString().trim().equals("") && !LastName.getText().toString().trim().equals("")
                        && !MiddleName.getText().toString().trim().equals("") && !CNumber.getText().toString().trim().equals("")
                         &&  !Email.getText().toString().trim().equals("")) {
                    signUp();
                } else {
                    Toast.makeText(getApplicationContext(), "Please complete all fields.", Toast.LENGTH_SHORT).show();
                }
            }
        });



        btnUserImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BTN_USERIMG = 1;
                uploadOptions();
            }
        });



    }


    private void uploadOptions() {
        final CharSequence[] items = {"Take photo", "Choose existing photo"};

        AlertDialog.Builder builder = new AlertDialog.Builder(SignUp.this);
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

    private void signUp() {
        StringRequest strRequest = new StringRequest(Request.Method.POST, getString(R.string.apiURL) + "signupvehicleowner", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    Log.d("Response", response);
                    String status = object.getString("status");
                    if (status.equals("success")) {
//                        String message = object.getString("message");
//                        Toast.makeText(getApplicationContext(),
//                                message, Toast.LENGTH_SHORT).show();
                        new android.app.AlertDialog.Builder(SignUp.this)
                                .setTitle("Password")
                                .setMessage("Your password has been sent to your e-mail address.")
                                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        Intent gotoSignIn = new Intent(getApplicationContext(), SignIn.class);
                                        startActivity(gotoSignIn);
                                        finish();
                                    }
                                }).show();
                    } else if (status.equals("failed")) {
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
                        "Failed to connect to Park King Servers", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("last_name", LastName.getText().toString().trim());
                parameters.put("first_name", FirstName.getText().toString().trim());
                parameters.put("middle_name", MiddleName.getText().toString().trim());
                parameters.put("contact_number", CNumber.getText().toString().trim());
                parameters.put("email", Email.getText().toString().trim());
                if (usrimg == null) {
                    parameters.put("profile_picture", "noimage.jpg");
                } else {
                    parameters.put("profile_picture", imageToString(usrimg));
                }
                return parameters;
            }
        };
        AppController.getInstance().addToRequestQueue(strRequest);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == RESULT_LOAD_IMAGE) {
                Uri path = data.getData();
                performCrop(path);
            } else if (requestCode == REQUEST_CAMERA) {
                Uri picUri = data.getData();
                performCrop(picUri);
            } else if (requestCode == PIC_CROP) {
                if (data != null) {
                    Bundle bundle = data.getExtras();
                    Bitmap bitmap = (Bitmap) bundle.get("data");
                    if(BTN_USERIMG == 1){
                        usrimg = bitmap;
                        imgUser.setImageBitmap(usrimg);
                    }
                }
            }
        }
    }

    private String imageToString(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imgByte = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imgByte, Base64.DEFAULT);
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

    public void openEULA(View v) {
        Intent gotoEULA = new Intent(getApplicationContext(), TermsAndCondition.class);
        startActivity(gotoEULA);
    }
}
