package com.example.afbu.parkking;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Home extends AppCompatActivity implements  OnMapReadyCallback, RoutingListener {

    SharedPreferences SharedPreference;
    SharedPreferences.Editor editor;
    private static final String PreferenceName = "UserPreference";
    private static final String PROFID_KEY = "ProfileIDKey";
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


    private DrawerLayout mDrawer;
    private ImageButton btnMenu, btnNotif, btnDirect;
    private NavigationView NavMenu;
    private ImageView NavImgUser;
    private TextView Name, Email;
    private String Firstname, Lastname, Middlename, Emailtxt, ProfilePicture;

    private String BuildingID, name;
    private double tolat, tolng, fromlat, fromlng;
    private AutoCompleteTextView searchPlace;
    private LatLng toPlace, fromPlace;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light, R.color.colorRed, R.color.colorBlack};


    private FusedLocationProviderClient mFusedLocationClient;

    private List<Integer> Distances;
    private int smallestDistance = 0, secondSmallestDistance = 0;
    private boolean haveArrived = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        SharedPreference = getSharedPreferences(PreferenceName, Context.MODE_PRIVATE);
        /*if(!SharedPreference.contains(PROFID_KEY)){
            Intent myIntent = new Intent(Home.this, StartUp.class);
            startActivity(myIntent);
        }else{
            ProfileID = SharedPreference.getString(PROFID_KEY, "");
        }*/

        initResources();
        initEvents();
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

    private void putParkKingMarker(){
        StringRequest strRequest = new StringRequest(Request.Method.GET,
                getString(R.string.apiURL) + "get_parking_markers/",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            JSONArray result = object.getJSONArray("data");
                            for (int i = 0; i < result.length(); i++) {
                                JSONObject c = result.getJSONObject(i);
                                BuildingID = c.getString("id");
                                name = c.getString("name");
                                latitude = Double.parseDouble(c.getString("latitude"));
                                longitude = Double.parseDouble(c.getString("longitude"));

                                StringRequest strRequest1 = new StringRequest(Request.Method.GET,
                                        getString(R.string.apiURL) + "get_building_infos/" + BuildingID,
                                        new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
                                                try {
                                                    JSONObject object = new JSONObject(response);
                                                    String status = object.getString("status");
                                                    if (status.equals("success")) {
                                                        String num_of_avail_slots = object.getString("number_of_available_slots");
                                                        markerOptions = new MarkerOptions();
                                                        markerOptions.title(name);
                                                        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("logo", 80, 110)));
                                                        markerOptions.position(new LatLng(latitude, longitude));
                                                        markerOptions.snippet(num_of_avail_slots);
                                                        gMap.addMarker(markerOptions);
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
                                AppController.getInstance().addToRequestQueue(strRequest1);

                                ArrayList<String> ParkKingPlaces = new ArrayList<String>();
                                ArrayList<Double> ParkKingLat = new ArrayList<Double>();
                                ArrayList<Double> ParkKingLong = new ArrayList<Double>();

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

    public Bitmap resizeMapIcons(String iconName,int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    private void getDeviceLocation() {

        /*mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationPermissionGranted) {
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Location currentLocation = (Location) task.getResult();
                            latitude = currentLocation.getLatitude();
                            longitude = currentLocation.getLongitude();
                            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), DEFAULT_ZOOM));
                            //Toast.makeText(this,currentLocation.getLatitude(), To)
                        } else {
                            Toast.makeText(Home.this, "Unable to get location.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Get Device Location: Security Exception:" + e.getMessage());
        }*/

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
        if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Toast.makeText(Home.this, "Heres10.", Toast.LENGTH_SHORT).show();
                    fromlat = location.getLatitude();
                    fromlng = location.getLongitude();
                    fromPlace = new LatLng(fromlat, fromlng);
                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    try {
                        List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(polylines.size() > 0) {
                        Routing routing = new Routing.Builder()
                                .travelMode(AbstractRouting.TravelMode.DRIVING)
                                .withListener(Home.this)
                                .waypoints(fromPlace, toPlace)
                                .alternativeRoutes(true)
                                .build();
                        routing.execute();
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

        }else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Toast.makeText(Home.this, "Heres8.", Toast.LENGTH_SHORT).show();
                    fromlat = location.getLatitude();
                    fromlng = location.getLongitude();
                    fromPlace = new LatLng(fromlat, fromlng);
                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    try {
                        List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(polylines.size() > 0) {
                        Routing routing = new Routing.Builder()
                                .travelMode(AbstractRouting.TravelMode.DRIVING)
                                .withListener(Home.this)
                                .waypoints(fromPlace, toPlace)
                                .alternativeRoutes(true)
                                .build();
                        routing.execute();
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
        }else{
            final CharSequence[] items  = {"Turn Location On", "Ignore"};

            final AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
            builder.setTitle("Turn Location Options");
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(items[which].equals("Turn Location On")){
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

    public boolean isServicesOk(){
         int avail = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(Home.this);

         if(avail == ConnectionResult.SUCCESS){
             return true;
         }else if (GoogleApiAvailability.getInstance().isUserResolvableError(avail)){
             Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(Home.this, avail, ERROR_DIALOG_REQUEST);
             dialog.show();
         }else{
             Toast.makeText(this, "You cant make a map request.", Toast.LENGTH_SHORT).show();
         }
         return false;
    }


    private void initEvents() {
        if(isServicesOk()){
            getLocationPermission();
        }

        //getVehicleOwnerInformation();

        btnDirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeDirections(toPlace);
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
                        mDrawer.closeDrawer(NavMenu);
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
        searchPlace = (AutoCompleteTextView) findViewById(R.id.Home_txtPlaces);
        btnDirect = (ImageButton) findViewById(R.id.Home_btnDirect);
        btnDirect.setVisibility(View.GONE);
        polylines = new ArrayList<>();
    }


    public void gotoCarList(View view) {
        Intent gotoCarList = new Intent(getApplicationContext(), CarList.class);
        startActivity(gotoCarList);
    }

    public void gotoParkList(View view) {
        Intent  gotoParkList= new Intent(getApplicationContext(), ParkingListings.class);
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
            gMap.setMyLocationEnabled(true);

        gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                tolat = marker.getPosition().latitude;
                tolng = marker.getPosition().longitude;
                toPlace = new LatLng(tolat, tolng);
                btnDirect.setVisibility(View.VISIBLE);
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
                TextView noOfSlots = (TextView) v.findViewById(R.id.info_window_txtslotsavail);

                bldgName.setText(marker.getTitle());
                noOfSlots.setText("Available Slots: " + marker.getSnippet());

                return v;
            }
        });
    }

    private void makeDirections(LatLng toPlace) {


        LatLng fromPlace = new LatLng(fromlat, fromlng);

        Toast.makeText(this, "Error: " +fromlng, Toast.LENGTH_LONG).show();

        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .waypoints(fromPlace, toPlace)
                .alternativeRoutes(true)
                .build();
        routing.execute();
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        if(polylines.size() > 0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();

        Distances = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //Distances.add(route.get(i).getDistanceValue());
            if(smallestDistance == 0){
                smallestDistance = i;
            }
            if(route.get(i).getDistanceValue() < secondSmallestDistance){
                if(route.get(i).getDistanceValue() < smallestDistance){
                    secondSmallestDistance = smallestDistance;
                    smallestDistance = i;
                }else{
                    secondSmallestDistance = i;
                }
            }
            Toast.makeText(getApplicationContext(), "Distance: "+ route.get(i).getDistanceValue(), Toast.LENGTH_LONG).show();
            if(haveArrived == false && route.get(i).getDistanceValue() <= 10){
                Toast.makeText(this, "You have reached your destination.", Toast.LENGTH_LONG).show();
                haveArrived = true;
            }
            //In case of more than 5 alternative routes
            /*int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = gMap.addPolyline(polyOptions);
            polylines.add(polyline);*/

            //Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }
        for(int x = 0; x != 2; x++){
            int colorIndex = x % COLORS.length;

            if(x == 1){
                PolylineOptions polyOptions = new PolylineOptions();
                polyOptions.color(getResources().getColor(COLORS[colorIndex]));
                polyOptions.width(10 + smallestDistance * 3);
                polyOptions.addAll(route.get(smallestDistance).getPoints());
                Polyline polyline = gMap.addPolyline(polyOptions);
                polylines.add(polyline);
            }else{
                PolylineOptions polyOptions = new PolylineOptions();
                polyOptions.color(getResources().getColor(COLORS[colorIndex]));
                polyOptions.width(10 + secondSmallestDistance * 3);
                polyOptions.addAll(route.get(secondSmallestDistance).getPoints());
                Polyline polyline = gMap.addPolyline(polyOptions);
                polylines.add(polyline);
            }

        }

        /*for(int x = 0; x < Distances.size(); x++){
            if(smallestDistance == 0){
                smallestDistance = Distances.get(x);
            }
            if(Distances.get(x) < secondSmallestDistance){
                if(Distances.get(x) < smallestDistance){
                    smallestDistance = Distances.get(x);
                }else{
                    secondSmallestDistance = Distances.get(x);
                }
            }
        }*/

    }

    @Override
    public void onRoutingCancelled() {

    }
}
