package com.example.afbu.parkking;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
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

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class Home extends AppCompatActivity {

    SharedPreferences SharedPreference;
    SharedPreferences.Editor editor;
    private static final String PreferenceName = "UserPreference";
    private static final String PROFID_KEY = "ProfileIDKey";
    private String ProfileID;

    private static String TAG = EditAccount.class.getSimpleName();

    private DrawerLayout mDrawer;
    private ImageButton btnMenu, btnNotif;
    private NavigationView NavMenu;
    private ImageView NavImgUser;
    private TextView Name, Email;
    private String Firstname, Lastname, Middlename, Emailtxt , ProfilePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        SharedPreference = getSharedPreferences(PreferenceName, Context.MODE_PRIVATE);
        if(!SharedPreference.contains(PROFID_KEY)){
            Intent myIntent = new Intent(Home.this, StartUp.class);
            startActivity(myIntent);
        }else{
            ProfileID = SharedPreference.getString(PROFID_KEY, "");
        }

        initResources();
        initEvents();

    }
/*
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }*/

    private void initEvents() {

        getVehicleOwnerInformation();

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawer.openDrawer(NavMenu);
            }
        });

        btnNotif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gotoNotif = new Intent(getApplicationContext(), Notifications.class);
                startActivity(gotoNotif);
            }
        });

        NavMenu.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id){
                    case R.id.nav_account:
                        Intent gotoEditAcc = new Intent(getApplicationContext(), EditAccount.class);
                        startActivity(gotoEditAcc);
                        mDrawer.closeDrawer(NavMenu);
                        //overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        break;

                    case R.id.nav_parkinglistings:
                        Intent  gotoParkList= new Intent(getApplicationContext(), ParkingListings.class);
                        startActivity(gotoParkList);
                        mDrawer.closeDrawer(NavMenu);
                        break;

                    case R.id.nav_parkinghistory:
                        break;

                    case R.id.nav_mycarlist:
                        Intent gotoCarList = new Intent(getApplicationContext(), CarList.class);
                        startActivity(gotoCarList);
                        mDrawer.closeDrawer(NavMenu);
                        break;

                    case R.id.nav_logout:
                        editor = SharedPreference.edit();
                        editor.clear();
                        editor.commit();
                        Intent gotoStartUp = new Intent(getApplicationContext(), StartUp.class);
                        startActivity(gotoStartUp);
                        break;
                    case R.id.nav_change_password:
                        Intent gotoChangPassword = new Intent(getApplicationContext(), ChangePassword.class);
                        startActivity(gotoChangPassword);
                        break;
                }
                return false;
            }
        });
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
                                Emailtxt = object.getString("email");
                                ProfilePicture = userinfo.getString("profile_picture");
                                Toast.makeText(getApplicationContext(), Lastname, Toast.LENGTH_SHORT).show();
                                //Name.setText(Lastname);
                                //Email.setText(Emailtxt);
                                //getProfilePicture();
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

    private void getProfilePicture() {
        ImageRequest imgRequest = new ImageRequest(
                getString(R.string.profilepictureURL) + ProfilePicture,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        NavImgUser.setImageBitmap(response);
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

    private void initResources() {
        mDrawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        btnMenu = (ImageButton) findViewById(R.id.Home_btnMenu);
        NavMenu = (NavigationView) findViewById(R.id.nav_menu);
        btnNotif = (ImageButton) findViewById(R.id.Home_btnNotif);
        NavImgUser = (ImageView) findViewById(R.id.NavHeader_imgUser);
        Name = (TextView) findViewById(R.id.NavHeader_Name);
        Email = (TextView) findViewById(R.id.NavHeader_Email);
    }
}
