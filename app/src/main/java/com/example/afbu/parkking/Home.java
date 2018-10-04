package com.example.afbu.parkking;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Home extends AppCompatActivity implements OnMapReadyCallback , DirectionFinderListener {


    SharedPreferences SharedPreference;
    SharedPreferences.Editor editor;
    private static final String PreferenceName = "UserPreference";
    private static final String PROFID_KEY = "ProfileIDKey";
    private static final String ON_FLOOR_KEY = "OnFloorKey";
    private String ProfileID;

    private static String TAG = Home.class.getSimpleName();

    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    private boolean mLocationPermissionGranted = false;
    private GoogleMap gMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final float DEFAULT_ZOOM = 15f;
    private LocationManager locationManager;
    private double latitude, longitude;
    private MarkerOptions markerOptions;

    private Object mLastKnownLocation;
    private DrawerLayout mDrawer;
    private ImageButton btnMenu, btnNotif, btnDirect, btnPosition,btnBuilding;
    private NavigationView NavMenu;
    private ImageView NavImgUser;
    private TextView Name, Email, AvailSlot;
    private String Firstname, Lastname, Middlename, Emailtxt, ProfilePicture;

    private String BuildingID, name;
    private double tolat, tolng, fromlat, fromlng;
    private AutoCompleteTextView searchPlace;
    private LatLng toPlace, fromPlace;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light, R.color.colorRed, R.color.colorBlack};

    private String onClickBuilding;
    private ArrayList<String> routersJSONArray;
    private ArrayList<String> floorIdList;
    private ArrayList<String> floorInfoTitle;

    private FusedLocationProviderClient mFusedLocationClient;
    private WifiScanner wifiScanner;
    private List<Integer> Distances;
    private int smallestDistance = 0, secondSmallestDistance = 0;

    private ArrayList<String> ParkKingPlaces;
    private ArrayList<Double> ParkKingLat, ParkKingLong;

    private String chosenBldg;
    private boolean haveArrived = false, onRouting = false;

    private TextView txtdistanceFromChosenBldg;
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

//        getBuildingFloorRouters("2");

        initResources();
        initEvents();
    }
    @Override
    protected void onResume(){
        super.onResume();
//        initResources();
//        initEvents();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //delete wifiScanner
    }

    public void getBuildingFloorRouters(final String building_id) {
        StringRequest strRequest = new StringRequest(Request.Method.GET, getString(R.string.apiURL) + "get_building_floor_routers/" + building_id, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                final String tempResponse = response;
                new android.app.AlertDialog.Builder(Home.this)
                        .setTitle("You have arrived at your destination")
                        .setMessage("Enter building view?")
                        .setPositiveButton("Go", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Intent myIntent = new Intent(Home.this, FloorMap.class);
                                myIntent.putExtra("floor_info", tempResponse);
                                myIntent.putExtra("building_id", building_id);
                                myIntent.putExtra("intent","park");
                                startActivity(myIntent);
                            }})
                        .setNegativeButton("Cancel", null).show();
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

    private void getLocationPermission() {
        String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this, permission, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permission, LOCATION_PERMISSION_REQUEST_CODE);
        }

    }

    private void putParkKingMarker() {
        StringRequest strRequest = new StringRequest(Request.Method.GET,
                getString(R.string.apiURL) + "get_parking_markers/",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            ParkKingPlaces = new ArrayList<String>();
                            ParkKingLat = new ArrayList<Double>();
                            ParkKingLong = new ArrayList<Double>();
                            JSONObject object = new JSONObject(response);
                            JSONArray result = object.getJSONArray("data");
                            for (int i = 0; i < result.length(); i++) {
                                JSONObject c = result.getJSONObject(i);
                                BuildingID = c.getString("id");
                                name = c.getString("name");
                                latitude = Double.parseDouble(c.getString("latitude"));
                                longitude = Double.parseDouble(c.getString("longitude"));

                                markerOptions = new MarkerOptions();
                                markerOptions.title(name);
                                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("logo_non_transparent", 80, 110)));
                                markerOptions.position(new LatLng(latitude, longitude));
                                markerOptions.snippet(BuildingID);
                                gMap.addMarker(markerOptions);

                                ParkKingPlaces.add(name);
                                ParkKingLat.add(latitude);
                                ParkKingLong.add(longitude);

                                ArrayAdapter<String> dataAdapter = new CostumArrayAdapter(getApplicationContext(), ParkKingPlaces);
                                searchPlace.setThreshold(1);
                                searchPlace.setAdapter(dataAdapter);
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

    public Bitmap resizeMapIcons(String iconName, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    private void getDeviceLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    //Toast.makeText(Home.this, "Heres10.", Toast.LENGTH_SHORT).show();
                    fromlat = location.getLatitude();
                    fromlng = location.getLongitude();
                    fromPlace = new LatLng(fromlat, fromlng);
                    if(onRouting == true){
                        if (polylinePaths != null && toPlace != null) {
                            String string_FromPlace = fromlat+","+fromlng;
                            String string_ToPlace = toPlace.latitude+","+toPlace.longitude;
                            try {
                                new DirectionFinder(Home.this, string_FromPlace, string_ToPlace).execute();
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });

        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    // Toast.makeText(Home.this, "Heres8.", Toast.LENGTH_SHORT).show();
                    fromlat = location.getLatitude();
                    fromlng = location.getLongitude();
                    fromPlace = new LatLng(fromlat, fromlng);
                    if(onRouting == true){
                        if (polylinePaths != null && toPlace != null) {
                            String string_FromPlace = fromlat+","+fromlng;
                            String string_ToPlace = toPlace.latitude+","+toPlace.longitude;
                            try {
                                new DirectionFinder(Home.this, string_FromPlace, string_ToPlace).execute();
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });
        } else {
            final CharSequence[] items = {"Turn Location On", "Ignore"};

            final AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
            builder.setTitle("Turn Location Options");
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (items[which].equals("Turn Location On")) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                        return;
                    }
                }
            });
            builder.show();

        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;
                    //initMap();
                }
            }
        }
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        putParkKingMarker();
    }

    public boolean isServicesOk() {
        int avail = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(Home.this);

        if (avail == ConnectionResult.SUCCESS) {
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(avail)) {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(Home.this, avail, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You cant make a map request.", Toast.LENGTH_SHORT).show();
        }
        return false;
    }


    private void initEvents() {
        if (isServicesOk()) {
            getLocationPermission();
        }

        //getVehicleOwnerInformation();
        btnPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fromPlace != null){
                    gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(fromPlace, 18));
                }
                Log.w("LOG", "BTN POSITION CLICKED");
            }
        });

        btnBuilding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onClickBuilding!=null){
                    Intent myIntent = new Intent(Home.this, FloorMap.class);
                    myIntent.putExtra("building_id", onClickBuilding);
                    myIntent.putExtra("intent","view");
                    startActivity(myIntent);
                }
            }
        });

        btnDirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (polylinePaths != null) {
                    for (Polyline polyline:polylinePaths ) {
                        polyline.remove();
                    }
                }
                haveArrived = false;
                onRouting = true;
               try{
                   Log.w("LOG",Double.toString(fromPlace.latitude)+", "+Double.toString(fromPlace.longitude));
                   makeDirections(toPlace);
               }catch(Exception e){
                   new android.app.AlertDialog.Builder(Home.this)
                           .setTitle("GPS Error")
                           .setMessage("Error getting phone location. Please turn on High Accuracy GPS on your Phone's Location Settings")
                           .setPositiveButton("Go", new DialogInterface.OnClickListener() {
                               public void onClick(DialogInterface dialog, int whichButton) {
                                   //Redirect to GPS settings
                               }})
                           .setNegativeButton("Cancel", null).show();
               }
            }
        });

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
                switch (id) {
                    case R.id.nav_account:
                        Intent gotoEditAcc = new Intent(getApplicationContext(), EditAccount.class);
                        startActivity(gotoEditAcc);
                        mDrawer.closeDrawer(NavMenu);
                        //overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        break;

                    case R.id.nav_parkinglistings:
                        Intent gotoParkList = new Intent(getApplicationContext(), ParkingListings.class);
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
                        mDrawer.closeDrawer(NavMenu);
                        break;
                }
                return false;
            }
        });
    }

    private void getVehicleOwnerInformation() {
        StringRequest strRequest = new StringRequest(Request.Method.GET,
                getString(R.string.apiURL) + "get_profile_details/" + ProfileID,                     //change id //done
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            String status = object.getString("status");
                            if (status.equals("success")) {
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
                }, 0, 0, null, new Response.ErrorListener() {

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
        txtdistanceFromChosenBldg = (TextView) findViewById(R.id.Home_txtDistance);
        mDrawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        btnMenu = (ImageButton) findViewById(R.id.Home_btnMenu);
        NavMenu = (NavigationView) findViewById(R.id.nav_menu);
        btnNotif = (ImageButton) findViewById(R.id.Home_btnNotif);
        NavImgUser = (ImageView) findViewById(R.id.NavHeader_imgUser);
        Name = (TextView) findViewById(R.id.NavHeader_Name);
        Email = (TextView) findViewById(R.id.NavHeader_Email);
        searchPlace = (AutoCompleteTextView) findViewById(R.id.Home_txtPlaces);
        btnBuilding = (ImageButton) findViewById(R.id.Home_btnBuilding);
        btnDirect = (ImageButton) findViewById(R.id.Home_btnDirect);
        btnDirect.setVisibility(View.GONE);
        btnBuilding.setVisibility(View.GONE);
        btnPosition = (ImageButton) findViewById(R.id.Home_btnPosition);
        btnPosition.setVisibility(View.GONE);
        polylines = new ArrayList<>();
        AvailSlot = (TextView) findViewById(R.id.Home_txtNoOfAvailSlot);
    }


    public void gotoCarList(View view) {
        Intent gotoCarList = new Intent(getApplicationContext(), CarList.class);
        startActivity(gotoCarList);
    }

    public void gotoParkList(View view) {
        Intent gotoParkList = new Intent(getApplicationContext(), ParkingListings.class);
        startActivity(gotoParkList);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        getDeviceLocation();
        if (ActivityCompat.checkSelfPermission(Home.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Home.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        gMap.setPadding(0, 150, 0, 170);
        if (ActivityCompat.checkSelfPermission(Home.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Home.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request   the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        //gMap.setMyLocationEnabled(true);
        gMap.setMyLocationEnabled(true);
        gMap.getUiSettings().setMyLocationButtonEnabled(false);
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(13.954371, 121.163004), 10));

        gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String title = marker.getSnippet();
                onClickBuilding = title;
                if(chosenBldg == null){
                    chosenBldg = title;
                }else{
                    if(!chosenBldg.equals(onClickBuilding)){
                        onRouting = false;
                        haveArrived = false;
                        onClickBuilding = chosenBldg;
                    }
                }
                StringRequest strRequest1 = new StringRequest(Request.Method.GET,
                        getString(R.string.apiURL) + "get_building_infos/" + title,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject object = new JSONObject(response);
                                    String status = object.getString("status");
                                    if (status.equals("success")) {
                                        String num_of_avail_slots = object.getString("number_of_available_slots");
                                        AvailSlot.setText(num_of_avail_slots);
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
                        VolleyLog.d(TAG, "GMAP ONCLICK Error: " + error.getMessage());
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
                AppController.getInstance().addToRequestQueue(strRequest1);

                if(!haveArrived) {
                    tolat = marker.getPosition().latitude;
                    tolng = marker.getPosition().longitude;
                    toPlace = new LatLng(tolat, tolng);
                }

                try{
                    btnDirect.setVisibility(View.VISIBLE);
                    btnBuilding.setVisibility(View.VISIBLE);
                    float[] results = new float[1];
                    Location.distanceBetween(fromPlace.latitude, fromPlace.longitude,
                            toPlace.latitude, toPlace.longitude,
                            results);
                    int temp = Math.round(results[0]);
                    if(temp>100){
                        temp = Math.round(temp/1000);
                        txtdistanceFromChosenBldg.setText(Integer.toString(temp)+"km");
                    }else{
                        txtdistanceFromChosenBldg.setText(Integer.toString(temp)+"m");
                    }


                }catch(Exception e){
                    btnDirect.setVisibility(View.INVISIBLE);
                    btnBuilding.setVisibility(View.INVISIBLE);
                    new android.app.AlertDialog.Builder(Home.this)
                            .setTitle("GPS Error")
                            .setMessage("Error getting phone location. Please turn on High Accuracy GPS on your Phone's Location Settings")
                            .setPositiveButton("Go", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    //Redirect to GPS settings
                                }})
                            .setNegativeButton("Cancel", null).show();
                }


                return false;
            }
        });

        gMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View v = getLayoutInflater().inflate(R.layout.info_window, null);

                TextView bldgName = (TextView) v.findViewById(R.id.info_window_buildingname);

                bldgName.setText(marker.getTitle());
                //noOfSlots.setText("Available Slots: " + marker.getSnippet());

                return v;
            }
        });
        btnPosition.setVisibility(View.VISIBLE);
    }

    private void makeDirections(LatLng toPlace) {
        String string_FromPlace = fromlat+","+fromlng;
        String string_ToPlace = toPlace.latitude+","+toPlace.longitude;
        try {
            new DirectionFinder(this, string_FromPlace, string_ToPlace).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDirectionFinderStart() {
        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<com.example.afbu.parkking.Route> routes) {
        polylinePaths = new ArrayList<>();

        for (com.example.afbu.parkking.Route route : routes) {

            float[] results = new float[1];
            Location.distanceBetween(fromPlace.latitude, fromPlace.longitude,
                    toPlace.latitude, toPlace.longitude,
                    results);

            if(results[0] <= 200 && haveArrived == false){
                //Toast.makeText(getApplicationContext(),"You have arrived at your destionation",Toast.LENGTH_SHORT).show();
                haveArrived = true;
                if (polylinePaths != null) {
                    for (Polyline polyline : polylinePaths) {
                        polyline.remove();
                    }
                }
                if(onClickBuilding!=null){
                    getBuildingFloorRouters(onClickBuilding);
                }
            }else if(haveArrived == false){
                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(fromPlace, 16));

                if (polylinePaths != null) {
                    for (Polyline polyline:polylinePaths ) {
                        polyline.remove();
                    }
                }

                PolylineOptions polylineOptions = new PolylineOptions().
                        geodesic(true).
                        color(Color.BLUE).
                        width(20);

                for (int i = 0; i < route.points.size(); i++)
                    polylineOptions.add(route.points.get(i));

                polylinePaths.add(gMap.addPolyline(polylineOptions));
            }
            int temp = Math.round(results[0]);
            if(temp>100){
                temp = Math.round(temp/1000);
                txtdistanceFromChosenBldg.setText(Integer.toString(temp)+"km");
            }else{
                txtdistanceFromChosenBldg.setText(Integer.toString(temp)+"m");
            }




        }


    }
}
