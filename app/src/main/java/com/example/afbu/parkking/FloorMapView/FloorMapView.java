package com.example.afbu.parkking.FloorMapView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.example.afbu.parkking.AStar.AStar;
import com.example.afbu.parkking.AStar.Grid;
import com.example.afbu.parkking.AppController;
import com.example.afbu.parkking.R;
import com.example.afbu.parkking.SaveSlotPromptDialog;
import com.example.afbu.parkking.SectionSlot;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FloorMapView extends View {
    private static final String TAG = FloorMapView.class.getSimpleName();
    private Context mContext;

    private Bitmap floorImage;
    private List<Bitmap> floorIndicators;
    private List<JSONObject> floorIndicatorCoords;

    private float userPositionX, userPositionY;
    private float floor_map_width, floor_map_height;
    private int x_line_count, y_line_count;

    private int entrance_x, entrance_y, exit_x, exit_y, next_floor_x, next_floor_y, previous_floor_x, previous_floor_y;

    private Paint routePaint;
    private Paint routeStrokePaint;
    private Path routePath;

    private List<Bitmap> floorSlots;
    private static final String[] slotStatusFile = {"open", "occupied", "closed"};
    private int[][] path_grids;
    private List<String> blocked_grids;
    private int destination_x, destination_y;

    AStar pathfindingAStar;

    private Bitmap userBitmap;

    private float floorImagePosX, floorImagePosY;
    private float floorImageWidth, floorImageHeight;
    private float mLastTouchX, mLastTouchY;

    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;
    private final static float MIN_ZOOM = 1f, MAX_ZOOM = 3.0f;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference slotRef;
    private DatabaseReference userRef;
    private String floorID;
    private boolean isCurrentFloor;

    private List<String> parkingFeeInformationList;
    private TextView parkingFeeTextView;
    private TextView availableSlotsTextView;
    private TextView selectedSlotTextView;

    private List<SectionSlot> sectionSlotListArray;

    private SharedPreferences sharedPreferences;
    private SharedPreferences floorIDSharedPreference;
    private static final String PreferenceName = "UserPreference";
    private static final String PROFID_KEY = "ProfileIDKey";
    public static final String CURRENT_FLOOR_ID = "currentFloorId";

    private SharedPreferences sectionSlotFinder;
    private SharedPreferences.Editor sectionSlotFinderEditor;
    private static final String SECTION_SLOT_FINDER_PREF_KEY = "SectionSlotFinder";
    private static final String SLOT_FIND_X_KEY = "sectionSlotFindX";
    private static final String SLOT_FIND_Y_KEY = "sectionSlotFindY";
    private static final String SLOT_FIND_FLOOR_ID_KEY = "sectionSlotFindFloorID";

    private List<ValueEventListener> slotEventListener;
    private static final int INVALID_POINTER_ID = -1;
    private int mActivePointerID = INVALID_POINTER_ID;

    private float canvasRotation;
    private float floorMapGridSize;
    private android.support.v4.app.FragmentManager supportFragmentManager;
    private String intent;
    private String parkedSlot;
    private ArrayList<Integer> buildingFloorHierarchy;
    private String selectedSlotFloorID;
    private String currentFloorID;

    public FloorMapView(Context context) {
        super(context);
        init(null, context);
    }

    public FloorMapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(null, context);
    }

    public FloorMapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(null, context);
    }

    public FloorMapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(null, context);
    }

    private void init(@Nullable AttributeSet set, Context context) {
        this.mContext = context;

        floorImage = null;
        floorIndicators = new ArrayList<>();
        floorIndicatorCoords = new ArrayList<>();
        floorSlots = new ArrayList<>();

        userPositionX = 500f;
        userPositionY = 500f;

        floorImagePosX = 0f;
        floorImagePosY = 0f;
        mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());

        parkingFeeInformationList = new ArrayList<>();
        parkingFeeTextView = null;
        availableSlotsTextView = null;
        selectedSlotTextView = null;

        canvasRotation = 0f;
        userBitmap = null;
        path_grids = null;
        blocked_grids = new ArrayList<>();
        pathfindingAStar = null;

        entrance_x = -1;
        entrance_y = -1;
        exit_y = -1;
        exit_x = -1;
        next_floor_x = -1;
        next_floor_y = -1;
        previous_floor_x = -1;
        previous_floor_y = -1;

        selectedSlotFloorID = "-1";
        currentFloorID = "-1";

        isCurrentFloor = false;
        buildingFloorHierarchy = new ArrayList<>();

        sectionSlotFinder = mContext.getSharedPreferences(SECTION_SLOT_FINDER_PREF_KEY, Context.MODE_PRIVATE);
        sectionSlotFinderEditor = sectionSlotFinder.edit();

        destination_x = -1;
        destination_y = -1;

        sectionSlotListArray = new ArrayList<>();
        slotEventListener = new ArrayList<>();
        sharedPreferences = mContext.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE);
        floorIDSharedPreference = mContext.getSharedPreferences(CURRENT_FLOOR_ID, mContext.MODE_PRIVATE);
    }

    private void getPath(int startX, int startY, int endX, int endY) {
        if (endX >= 0 && endY >= 0) {
            pathfindingAStar = new AStar(
                    y_line_count,
                    x_line_count,
                    startX,
                    startY,
                    blocked_grids);
            pathfindingAStar.endGrid(endX, endY);
            pathfindingAStar.process();
            List<Grid> solutionGrids = pathfindingAStar.getSolution();

            if (solutionGrids.size() > 0) {
                path_grids = new int[solutionGrids.size()][2];
                for (int x = 0; x < solutionGrids.size(); x++) {
                    path_grids[x][1] = solutionGrids.get(x).x;
                    path_grids[x][0] = solutionGrids.get(x).y;
                }

                routePaint = new Paint();
                routePaint.setAntiAlias(true); // enable anti aliasing
                routePaint.setColor(Color.WHITE); // set default color to white
                routePaint.setDither(true); // enable dithering
                routePaint.setStyle(Paint.Style.STROKE); // set to STOKE
                routePaint.setStrokeJoin(Paint.Join.ROUND); // set the join to round you want
                routePaint.setStrokeCap(Paint.Cap.ROUND);  // set the paint cap to round too
                routePaint.setStrokeWidth(((getHeight() / y_line_count) * 0.75f) - 5);
                routePaint.setPathEffect(new CornerPathEffect(getHeight() / 20)); // set the path effect when they join.

                routeStrokePaint = new Paint();
                routeStrokePaint.setAntiAlias(true); // enable anti aliasing
                routeStrokePaint.setColor(Color.BLACK); // set default color to white
                routeStrokePaint.setDither(true); // enable dithering
                routeStrokePaint.setStyle(Paint.Style.STROKE); // set to STOKE
                routeStrokePaint.setStrokeJoin(Paint.Join.ROUND); // set the join to round you want
                routeStrokePaint.setStrokeCap(Paint.Cap.ROUND);  // set the paint cap to round too
                routeStrokePaint.setStrokeWidth(((getHeight() / y_line_count) * 0.75f));
                routeStrokePaint.setPathEffect(new CornerPathEffect(getHeight() / 20)); // set the path effect when they join.

                routePath = new Path();
            }
        } else {
            routePaint = null;
            routeStrokePaint = null;
            routePath = null;
            path_grids = null;
            sectionSlotFinderEditor.remove(SLOT_FIND_X_KEY);
            sectionSlotFinderEditor.remove(SLOT_FIND_Y_KEY);
            sectionSlotFinderEditor.remove(SLOT_FIND_FLOOR_ID_KEY);
            sectionSlotFinderEditor.commit();
            destination_x = -1;
            destination_y = -1;
        }

        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (floorImage != null) {
            canvas.save();
            canvas.scale(mScaleFactor, mScaleFactor);
            if ((floorImagePosX * -1) < 0) {
                floorImagePosX = 0;
            } else if ((floorImagePosX * -1) > floorImageWidth * mScaleFactor - getWidth()) {
                floorImagePosX = (floorImageWidth * mScaleFactor - getWidth()) * -1;
            }

            if ((floorImagePosY * -1) < 0) {
                floorImagePosY = 0;
            } else if ((floorImagePosY * -1) > floorImageHeight * mScaleFactor - getHeight()) {
                floorImagePosY = (floorImageHeight * mScaleFactor - getHeight()) * -1;
            }

            if ((floorImageHeight * mScaleFactor) < getHeight()) {
                floorImageHeight = 0;
            }

            canvas.drawBitmap(floorImage, floorImagePosX, floorImagePosY, null);

            for (int i = 0; i < floorIndicators.size(); i++) {
                try {
                    float indicatorX = ((float) floorIndicatorCoords.get(i).getDouble("x") * floorImageWidth - 50) + floorImagePosX;
                    float indicatorY = ((float) floorIndicatorCoords.get(i).getDouble("y") * floorImageHeight - 150) + floorImagePosY;

                    canvas.drawBitmap(floorIndicators.get(i), indicatorX, indicatorY, null);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            for (int i = 0; i < floorSlots.size(); i++) {
                float floorSlotX = ((float) sectionSlotListArray.get(i).getIndicatorCoordinate().latitude * floorImageWidth - (String.valueOf(sectionSlotListArray.get(i).getSlotID()).equals(parkedSlot) ? 82 : 50)) + floorImagePosX;
                float floorSlotY = ((float) sectionSlotListArray.get(i).getIndicatorCoordinate().longitude * floorImageHeight - (String.valueOf(sectionSlotListArray.get(i).getSlotID()).equals(parkedSlot) ? 200 : 150)) + floorImagePosY;
                canvas.drawBitmap(floorSlots.get(i), floorSlotX, floorSlotY, null);
            }

            if (path_grids != null) {
                routePath.moveTo((((path_grids[0][0] / (float) x_line_count) * floorImageWidth) + floorImagePosX) + ((routePaint.getStrokeWidth() / 2) + 7.5f), (((path_grids[0][1] / (float) y_line_count) * floorImageHeight) + floorImagePosY) + ((routePaint.getStrokeWidth() / 2)) + 7.5f);
                for (int i = 1; i < path_grids.length; i++) {
                    routePath.lineTo((((path_grids[i][0] / (float) x_line_count) * floorImageWidth) + floorImagePosX) + ((routePaint.getStrokeWidth() / 2) + 7.5f), (((path_grids[i][1] / (float) y_line_count) * floorImageHeight) + floorImagePosY) + ((routePaint.getStrokeWidth() / 2)) + 7.5f);
                }
                canvas.drawPath(routePath, routeStrokePaint);
                canvas.drawPath(routePath, routePaint);
                routePath.reset();
            }

            if (isCurrentFloor) {
                canvas.drawBitmap(userBitmap, (userPositionX - (getWidth() / 10f) / 2) + floorImagePosX, (userPositionY - (getWidth() / 10f) / 2) + floorImagePosY, null);
            }
            canvas.restore();
        }
    }

    public void setFloorMapInformation(JSONObject floorObj, String floorID, TextView parkingFeeTextView, TextView availableSlotsTextView, TextView selectedSlotTextView, ArrayList<Integer> buildingFloorHierarchy) {
        // reset values
        floorImagePosX = 0f;
        floorImagePosY = 0f;

        detatchValueEventListener();

        slotEventListener = new ArrayList<>();
        sectionSlotListArray = new ArrayList<>();

        path_grids = null;
        blocked_grids = new ArrayList<>();

        floorIndicators = new ArrayList<>();
        floorIndicatorCoords = new ArrayList<>();

        floorSlots = new ArrayList<>();

        userBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.user_position);
        Matrix matrix = new Matrix();
        RectF src = new RectF(0, 0, userBitmap.getWidth(), userBitmap.getHeight());
        RectF dest = new RectF(0, 0, getWidth() / 10f, getWidth() / 10f);
        matrix.setRectToRect(src, dest, Matrix.ScaleToFit.CENTER);
        userBitmap = Bitmap.createBitmap(userBitmap, 0, 0, userBitmap.getWidth(), userBitmap.getHeight(), matrix, true);

        if (!sharedPreferences.getString(PROFID_KEY, "").trim().isEmpty()) {
            userRef = database.getReference().child("users").child(sharedPreferences.getString(PROFID_KEY, ""));
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        if (dataSnapshot.child("x").exists() && dataSnapshot.child("y").exists()) {
                            repositionUser(
                                    Integer.parseInt(dataSnapshot.child("x").getValue().toString()) > 0 ? Integer.parseInt(dataSnapshot.child("x").getValue().toString()) : 0
                                    , Integer.parseInt(dataSnapshot.child("y").getValue().toString()) > 0 ? Integer.parseInt(dataSnapshot.child("y").getValue().toString()) : 0
                            );

                            if (blocked_grids.size() > 0 && !intent.equals("view_parked")) {
                                Runnable runnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        if (destination_x >= 0 && destination_y >= 0) {
                                            assessPathToTake();
                                        }
                                    }
                                };
                                Thread thread = new Thread(runnable);
                                thread.start();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        // reset values

        // floor iD
        this.floorID = floorID;

        // text views for selected slot
        this.parkingFeeTextView = parkingFeeTextView;
        this.availableSlotsTextView = availableSlotsTextView;
        this.selectedSlotTextView = selectedSlotTextView;

        try {
            // floor map
            new RetrieveFloorImageTask().execute(mContext.getString(R.string.floor_map_folder) + floorObj.getString("image"));

            // floor map dimensions in meters
            floor_map_width = (float) floorObj.getDouble("map_width");
            floor_map_height = (float) floorObj.getDouble("map_height");
            this.floorMapGridSize = (float) floorObj.getDouble("grid_size");

            // floor line counts (grid x and y counts)
            this.x_line_count = floorObj.getInt("x_line_count");
            this.y_line_count = floorObj.getInt("y_line_count");

            // slots
            JSONObject floorSlotsObj = new JSONObject(floorObj.getString("floor_slots"));
            if (floorSlotsObj.getBoolean("has_slots")) {
                JSONArray floorSlotsJSONArray = new JSONArray(floorSlotsObj.getString("slots"));
                if (floorSlotsJSONArray.length() > 0) {
                    for (int i = 0; i < floorSlotsJSONArray.length(); i++) {
                        try {
                            this.sectionSlotListArray.add(new SectionSlot(floorSlotsJSONArray.getJSONObject(i), floorObj.getDouble("map_width"), floorObj.getDouble("map_height")));
                            getSlotBitmap(floorSlotsJSONArray.getJSONObject(i).getString("slot_id"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                Toast.makeText(mContext, "No Slots", Toast.LENGTH_SHORT).show();
            }

            // indicators
            JSONObject floorIndicatorsObj = new JSONObject(floorObj.getString("floor_indicators"));
            if (floorIndicatorsObj.getBoolean("has_indicators")) {
                JSONArray floorIndicatorsJSONArray = new JSONArray(floorIndicatorsObj.getString("indicators"));

                if (floorIndicatorsJSONArray.length() > 0) {
                    for (int i = 0; i < floorIndicatorsJSONArray.length(); i++) {
                        try {
                            floorIndicatorCoords.add(floorIndicatorsJSONArray.getJSONObject(i));
                            getIndicatorBitmap(floorIndicatorsJSONArray.getJSONObject(i).getString("img"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            // map grids
            if (floorObj.getString("map_grid") != "null") {
                JSONArray gridY = new JSONArray(floorObj.getString("map_grid"));

                for (int i = 0; i < gridY.length(); i++) {
                    JSONArray gridX = new JSONArray(gridY.getString(i));

                    for (int j = 0; j < gridX.length(); j++) {
                        if (gridX.getInt(j) == 0) {
                            blocked_grids.add(String.valueOf(i) + "," + String.valueOf(j));
                        }
                    }
                }
            }

            // block grids
            entrance_x = floorObj.getInt("entrance_x");
            entrance_y = floorObj.getInt("entrance_y");
            exit_x = floorObj.getInt("exit_x");
            exit_y = floorObj.getInt("exit_y");

            if ((floorObj.getInt("next_floor_x") != -1 && floorObj.getInt("next_floor_y") != -1) &&
                    (floorObj.getInt("previous_floor_x") != -1 && floorObj.getInt("previous_floor_y") != -1)) {
                next_floor_x = floorObj.getInt("next_floor_x");
                next_floor_y = floorObj.getInt("next_floor_y");
                previous_floor_x = floorObj.getInt("previous_floor_y");
                previous_floor_y = floorObj.getInt("previous_floor_y");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        this.buildingFloorHierarchy = buildingFloorHierarchy;
        this.selectedSlotFloorID = sectionSlotFinder.getString(SLOT_FIND_FLOOR_ID_KEY, "-1");
        this.currentFloorID = floorIDSharedPreference.getString("currentFloorID", "-1");
        this.isCurrentFloor = this.currentFloorID.equals(this.floorID);

        destination_x = sectionSlotFinder.getInt(SLOT_FIND_X_KEY, -1);
        destination_y = sectionSlotFinder.getInt(SLOT_FIND_Y_KEY, -1);

        if (destination_x >= 0 && destination_y >= 0) {
            assessPathToTake();
        }

        setSlotStatusListeners();
        postInvalidate();
    }

    private void setSlotStatusListeners() {
        for (int i = 0; i < sectionSlotListArray.size(); i++) {
            final SectionSlot floorSlotObject = sectionSlotListArray.get(i);
            final int slot_id = floorSlotObject.getSlotID();
            int section_id = floorSlotObject.getSectionID();

            slotRef = database.getReference("section").child(String.valueOf(section_id)).child("slot").child(String.valueOf(slot_id)).child("status");

            final int finalI = i;
            slotEventListener.add(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Toast.makeText(mContext, "Change", Toast.LENGTH_SHORT).show();
                        int status = Integer.valueOf(String.valueOf(dataSnapshot.getValue()));
                        if (!String.valueOf(slot_id).equals(parkedSlot)) {
                            changeSlotBitmapStatus(slotStatusFile[status], finalI);
                        }
                        if (floorSlotObject.isFirst_read()) {
                            floorSlotObject.setFirst_read(false);
                        } else {
                            switch (status) {
                                case 0:
                                    availableSlotsTextView.setText(String.valueOf(Integer.valueOf(availableSlotsTextView.getText().toString()) + 1));
                                    break;
                                case 1:
                                case 2:

                                    if (status == 1) {
                                        if (inSlotArea(floorSlotObject.getPointA(),
                                                floorSlotObject.getPointB(),
                                                floorSlotObject.getPointC(),
                                                floorSlotObject.getPointD(),
                                                new LatLng(((userPositionX / floorImageWidth) * floor_map_width), ((userPositionY / floorImageHeight) * floor_map_height)))) {
                                            SaveSlotPromptDialog saveSlotPromptDialog = new SaveSlotPromptDialog();
                                            saveSlotPromptDialog.setSlotTitle(floorSlotObject.getSlotTitle());
                                            saveSlotPromptDialog.show(supportFragmentManager, "Save Slot Prompt");

                                            saveVehicleLog(floorSlotObject.getSlotID());
                                        }
                                    } else {
                                        Toast.makeText(mContext, "This slot is closed!", Toast.LENGTH_SHORT).show();
                                    }
                                    if (floorSlotObject.getCurr_stat() == 0)
                                        availableSlotsTextView.setText(String.valueOf(Integer.valueOf(availableSlotsTextView.getText().toString()) - 1));
                                    break;
                            }
                            floorSlotObject.setCurr_stat(status);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            slotRef.addValueEventListener(slotEventListener.get(i));
        }
    }

    private void saveVehicleLog(final int slotID) {
        StringRequest strRequest = new StringRequest(Request.Method.POST, mContext.getString(R.string.createVehicleLogURL), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);
                try {
                    JSONObject requestObj = new JSONObject(response);
                    if (!requestObj.getBoolean("success")) {
                        Toast.makeText(mContext, requestObj.getString("message"), Toast.LENGTH_SHORT).show();
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
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("vehicle_owner_profile_id", sharedPreferences.getString(PROFID_KEY, ""));
                parameters.put("section_slot_id", String.valueOf(slotID));
                return parameters;
            }
        };
        AppController.getInstance().addToRequestQueue(strRequest);
    }

    private void changeSlotBitmapStatus(String slotURL, final int slotIndex) {
        if (floorSlots.size() > slotIndex) {
            floorSlots.set(slotIndex,
                    resizeSlotIndicator(
                            BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(slotURL, "drawable", mContext.getPackageName())), false
                    ));
            postInvalidate();
        }
    }

    private void getSlotBitmap(String slot_id) {
        floorSlots.add(
                slot_id.equals(parkedSlot) ?
                        resizeSlotIndicator(
                                BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("logo", "drawable", mContext.getPackageName())), slot_id.equals(parkedSlot)
                        ) : resizeSlotIndicator(
                        BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("default_slot", "drawable", mContext.getPackageName())), slot_id.equals(parkedSlot)
                )
        );
    }

    private void getIndicatorBitmap(String imgURL) {
        @SuppressLint("StaticFieldLeak")
        RetrieveFloorIndicatorImg retrieveFloorIndicatorImg = new RetrieveFloorIndicatorImg() {
            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);

                floorIndicators.add(bitmap);
            }
        };

        retrieveFloorIndicatorImg.execute(getResources().getString(R.string.system_files) + imgURL);
    }

//    public void setSupportFragmentManager(FragmentManager supportFragmentManager) {
//        this.supportFragmentManager = supportFragmentManager;
//    }

    public void setSupportFragmentManager(android.support.v4.app.FragmentManager supportFragmentManager) {
        this.supportFragmentManager = supportFragmentManager;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public void setParkedSlot(String parkedSlot) {
        this.parkedSlot = parkedSlot;
    }

    public void destroySlotSharedPreference() {
        sectionSlotFinderEditor.clear();
        sectionSlotFinderEditor.commit();
    }

    class RetrieveFloorImageTask extends AsyncTask<String, Void, Void> {

        private Exception exception;

        protected Void doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);

                floorImage = myBitmap;
                floorImage = resizeBitmap(floorImage);
                postInvalidate();
            } catch (IOException e) {
                // Log exception
                return null;
            }
            return null;
        }
    }


    class RetrieveFloorIndicatorImg extends AsyncTask<String, Void, Bitmap> {

        private Exception exception;

        protected Bitmap doInBackground(String... urls) {
            Bitmap indicatorBitmap;
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                indicatorBitmap = BitmapFactory.decodeStream(input);

                Matrix matrix = new Matrix();
                RectF src = new RectF(0, 0, indicatorBitmap.getWidth(), indicatorBitmap.getHeight());
                RectF dest = new RectF(0, 0, 99.89f, 150f);
                matrix.setRectToRect(src, dest, Matrix.ScaleToFit.CENTER);

                return Bitmap.createBitmap(indicatorBitmap, 0, 0, indicatorBitmap.getWidth(), indicatorBitmap.getHeight(), matrix, true);
            } catch (IOException e) {
                // Log exception
                return null;
            }
        }
    }

    private Bitmap resizeBitmap(Bitmap floorImage) {
        float proportion = 0f;
        if (floorImage.getWidth() > floorImage.getHeight()) {
            proportion = (float) getMeasuredHeight() / floorImage.getHeight();
            floorImageWidth = proportion * floorImage.getWidth();
            floorImageHeight = getHeight();
        } else {
            proportion = (float) getWidth() / floorImage.getWidth();
            floorImageHeight = proportion * floorImage.getHeight();
            floorImageWidth = getWidth();

            mScaleFactor = getHeight() / floorImageHeight;
        }

        Matrix matrix = new Matrix();

        RectF src = new RectF(0, 0, floorImage.getWidth(), floorImage.getHeight());
        RectF dest = new RectF(0, 0, floorImageWidth, floorImageHeight);

        matrix.setRectToRect(src, dest, Matrix.ScaleToFit.FILL);

        return Bitmap.createBitmap(floorImage, 0, 0, floorImage.getWidth(), floorImage.getHeight(), matrix, true);
    }

    private Bitmap resizeSlotIndicator(Bitmap indicator, boolean isParked) {
        Matrix matrix = new Matrix();

        RectF src = new RectF(0, 0, indicator.getWidth(), indicator.getHeight());
        RectF dest;
        if (!isParked) {
            dest = new RectF(0, 0, 100, 150);
        } else {
            dest = new RectF(0, 0, (float) 164.8, 200);
        }

        matrix.setRectToRect(src, dest, Matrix.ScaleToFit.FILL);

        return Bitmap.createBitmap(indicator, 0, 0, indicator.getWidth(), indicator.getHeight(), matrix, true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean val = super.onTouchEvent(event);
        if (floorImage == null)
            return val;

        mScaleDetector.onTouchEvent(event);

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                // get coords of clicked point
                final float x = event.getX();
                final float y = event.getY();

                // remember the last touched
                mLastTouchX = x;
                mLastTouchY = y;

                for (int i = 0; i < sectionSlotListArray.size(); i++) {
                    float bitmapXPosition = ((float) sectionSlotListArray.get(i).getIndicatorCoordinate().latitude * floorImageWidth - 50) + floorImagePosX;
                    float bitmapYPosition = ((float) sectionSlotListArray.get(i).getIndicatorCoordinate().longitude * floorImageHeight - 150) + floorImagePosY;

                    if (x > (bitmapXPosition) * mScaleFactor && x < (bitmapXPosition + floorSlots.get(i).getWidth()) * mScaleFactor && y > (bitmapYPosition) * mScaleFactor && y < (bitmapYPosition + floorSlots.get(i).getHeight()) * mScaleFactor) {
                        getSlotInformation(i);
                    }
                }

                mActivePointerID = event.getPointerId(0);

                break;
            }
            case MotionEvent.ACTION_MOVE: {

                final int pointerIndex = event.findPointerIndex(mActivePointerID);
                final float x = event.getX(pointerIndex);
                final float y = event.getY(pointerIndex);

                if (!mScaleDetector.isInProgress()) {
                    final float dX = x - mLastTouchX;
                    final float dY = y - mLastTouchY;

                    floorImagePosX += (dX / mScaleFactor);
                    floorImagePosY += (dY / mScaleFactor);

                    mLastTouchX = x;
                    mLastTouchY = y;

                    invalidate();
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                mActivePointerID = INVALID_POINTER_ID;
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = event.getPointerId(pointerIndex);

                if (pointerId == mActivePointerID) {
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastTouchX = event.getX(newPointerIndex);
                    mLastTouchY = event.getY(newPointerIndex);
                    mActivePointerID = event.getPointerId(newPointerIndex);
                }

                break;
            }
        }

        return true;
    }

    private void getSlotInformation(int i) {
        if (destination_x == sectionSlotListArray.get(i).getGrid_coordinates()[1] && destination_y == sectionSlotListArray.get(i).getGrid_coordinates()[0]) {
            routePaint = null;
            routeStrokePaint = null;
            routePath = null;
            path_grids = null;

            sectionSlotFinderEditor.remove(SLOT_FIND_X_KEY);
            sectionSlotFinderEditor.remove(SLOT_FIND_Y_KEY);
            sectionSlotFinderEditor.remove(SLOT_FIND_FLOOR_ID_KEY);
            sectionSlotFinderEditor.commit();
        } else {
            sectionSlotFinderEditor.putInt(SLOT_FIND_X_KEY, sectionSlotListArray.get(i).getGrid_coordinates()[1]);
            sectionSlotFinderEditor.putInt(SLOT_FIND_Y_KEY, sectionSlotListArray.get(i).getGrid_coordinates()[0]);
            sectionSlotFinderEditor.putString(SLOT_FIND_FLOOR_ID_KEY, floorID);
            sectionSlotFinderEditor.commit();
        }

        destination_x = sectionSlotFinder.getInt(SLOT_FIND_X_KEY, -1);
        destination_y = sectionSlotFinder.getInt(SLOT_FIND_Y_KEY, -1);
        selectedSlotFloorID = sectionSlotFinder.getString(SLOT_FIND_FLOOR_ID_KEY, "-1");

        if (destination_x >= 0 && destination_y >= 0) {
            assessPathToTake();
        }

        StringRequest strRequest = new StringRequest(Request.Method.GET, mContext.getString(R.string.getBillingInfoURL) + sectionSlotListArray.get(i).getSlotID(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, response.toString());
                try {
                    JSONObject requestObj = new JSONObject(response);
                    if (requestObj.getBoolean("success")) {
                        parkingFeeInformationList = new ArrayList<>();
                        if (requestObj.getJSONObject("billing_info") != null && destination_x != -1 && destination_y != -1) {
                            parkingFeeInformationList.add(requestObj.getJSONObject("billing_info").getString("rate"));
                            parkingFeeInformationList.add(requestObj.getJSONObject("billing_info").getString("overnight_fee"));
                            parkingFeeInformationList.add(requestObj.getJSONObject("billing_info").getString("title"));

                            parkingFeeTextView.setText("View");
                            parkingFeeTextView.setTextColor(Color.parseColor("#3E55A4"));

                            selectedSlotTextView.setText(requestObj.getJSONObject("billing_info").getString("title"));
                            selectedSlotTextView.setTextColor(Color.parseColor("#3E55A4"));
                        } else {
                            parkingFeeTextView.setText("N/A");
                            parkingFeeTextView.setTextColor(Color.BLACK);

                            selectedSlotTextView.setText("None");
                            selectedSlotTextView.setTextColor(Color.BLACK);
                        }
                    }
                    postInvalidate();
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
                Map<String, String> parameters = new HashMap<String, String>();
                return parameters;
            }
        };
        AppController.getInstance().addToRequestQueue(strRequest);

        if (blocked_grids.size() > 0 && !intent.equals("view_parked")) {
            if (destination_x >= 0 && destination_y >= 0) {
                assessPathToTake();
            }
        }
    }

    public void repositionUserBitmap(Double userPercentageX, Double userPercentageY) {
        userPositionX = (userPercentageX.floatValue() / floor_map_width) * floorImageWidth;
        userPositionY = (userPercentageY.floatValue() / floor_map_height) * floorImageHeight;
        // // Log.w("LOG", "CANVAS X: "+ Float.toString(userPositionX));
        // // Log.w("LOG", "CANVAS Y: "+ Float.toString(userPositionY));

        postInvalidate();
    }


    private void repositionUser(int gridPosX, int gridPosY) {
        userPositionX = ((gridPosX / floor_map_width) * floorImageWidth) / (1 / floorMapGridSize);
        userPositionY = ((gridPosY / floor_map_height) * floorImageHeight) / (1 / floorMapGridSize);
        postInvalidate();
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            mScaleFactor *= scaleGestureDetector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(MIN_ZOOM, Math.min(mScaleFactor, MAX_ZOOM));

            postInvalidate();

            return true;
        }
    }

    public List<String> getParkingFeeInformation() {
        return parkingFeeInformationList;
    }

    public Double getTriangleArea(LatLng pointA, LatLng pointB, LatLng pointC) {
        Double area;
        area = Math.abs(
                (
                        (pointA.latitude * (pointB.longitude - pointC.longitude))
                                + (pointB.latitude * (pointC.longitude - pointA.longitude))
                                + (pointC.latitude * (pointA.longitude - pointB.longitude))
                ) / 2);
        return area;
    }

    public Double getRectangleArea(LatLng pointA, LatLng pointB, LatLng pointD, LatLng pointC) {
        Double area = 2d;
        area = (Math.sqrt(Math.pow((pointA.latitude - pointB.latitude), 2) + Math.pow((pointA.longitude - pointB.longitude), 2))) * (Math.sqrt(Math.pow((pointA.latitude - pointC.latitude), 2) + Math.pow((pointA.longitude - pointC.longitude), 2)));
        return area;
    }

    public final Boolean inSlotArea(LatLng pointA, LatLng pointB, LatLng pointC, LatLng pointD, LatLng userPosition) {
        Double rectangleArea, APD, DPC, CPB, PBA;
        rectangleArea = getRectangleArea(pointA, pointB, pointC, pointD);
        APD = getTriangleArea(pointA, userPosition, pointD);
        DPC = getTriangleArea(pointD, userPosition, pointC);
        CPB = getTriangleArea(pointC, userPosition, pointB);
        PBA = getTriangleArea(userPosition, pointB, pointA);
        Log.d("slot_computation", "Point A: " + Double.toString(pointA.latitude) + ", " + pointA.longitude);
        Log.d("slot_computation", "Point B: " + Double.toString(pointB.latitude) + ", " + pointB.longitude);
        Log.d("slot_computation", "Point C: " + Double.toString(pointC.latitude) + ", " + pointC.longitude);
        Log.d("slot_computation", "Point D: " + Double.toString(pointD.latitude) + ", " + pointD.longitude);

        Log.d("slot_computation", "Rectangle area: " + Double.toString(Math.round(rectangleArea * 10000.0) / 10000.0));
        Log.d("slot_computation", "APD: " + Double.toString(APD));
        Log.d("slot_computation", "DPC: " + Double.toString(DPC));
        Log.d("slot_computation", "CPB: " + Double.toString(CPB));
        Log.d("slot_computation", "PBA: " + Double.toString(PBA));
        Log.d("slot_computation", "Total area: " + Double.toString((Math.round((APD + DPC + CPB + PBA) * 10000.0) / 10000.0)));


        if ((Math.round((APD + DPC + CPB + PBA) * 10000.0) / 10000.0) == (Math.round(rectangleArea * 10000.0) / 10000.0)) {
            return true;
        } else {
            return false;
        }
    }

    public void detatchValueEventListener() {
        if (slotEventListener.size() > 0 && sectionSlotListArray.size() > 0) {
            for (int i = 0; i < slotEventListener.size(); i++) {
                final SectionSlot floorSlotObject = sectionSlotListArray.get(i);
                final int slot_id = floorSlotObject.getSlotID();
                int section_id = floorSlotObject.getSectionID();

                slotRef = database.getReference("section").child(String.valueOf(section_id)).child("slot").child(String.valueOf(slot_id)).child("status");
                slotRef.removeEventListener(slotEventListener.get(i));
            }
        }
    }

    private void assessPathToTake() {
        if (blocked_grids.size() == 0) {
            Toast.makeText(mContext, "No path defined on this map", Toast.LENGTH_SHORT).show();
            return;
        }

        if (floorID.equals(selectedSlotFloorID)) {
            if (isCurrentFloor) {
                // draw path from position to slot
                getPath((int) Math.floor(((userPositionY * floor_map_height) * (1 / floorMapGridSize)) / floorImageHeight),
                        (int) Math.floor(((userPositionX * floor_map_width) * (1 / floorMapGridSize)) / floorImageWidth),
                        destination_x, destination_y);
            } else {
                // draw path from entrance to slot
                if (buildingFloorHierarchy.indexOf(Integer.valueOf(floorID)) < buildingFloorHierarchy.indexOf(Integer.valueOf(currentFloorID))) {
                    getPath(previous_floor_x, previous_floor_y, destination_x, destination_y);
                } else if (buildingFloorHierarchy.indexOf(Integer.valueOf(floorID)) > buildingFloorHierarchy.indexOf(Integer.valueOf(currentFloorID))){
                    getPath(entrance_x, entrance_y, destination_x, destination_y);
                }
            }
        } else if (buildingFloorHierarchy.indexOf(Integer.valueOf(floorID)) < buildingFloorHierarchy.indexOf(Integer.valueOf(selectedSlotFloorID))) {
            if (isCurrentFloor) {
                // draw path from position to next floor
                getPath((int) Math.floor(((userPositionY * floor_map_height) * (1 / floorMapGridSize)) / floorImageHeight),
                        (int) Math.floor(((userPositionX * floor_map_width) * (1 / floorMapGridSize)) / floorImageWidth),
                        next_floor_x, next_floor_y);
            } else if (buildingFloorHierarchy.indexOf(Integer.valueOf(currentFloorID)) < buildingFloorHierarchy.indexOf(Integer.valueOf(floorID))) {
                // draw path from entrance to next floor block
                if(next_floor_x >= 0 && next_floor_y >= 0) {
                    getPath(entrance_x, entrance_y, next_floor_x, next_floor_y);
                } else {
                    Toast.makeText(mContext, "No path found from entrance to next floor", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (buildingFloorHierarchy.indexOf(Integer.valueOf(floorID)) > buildingFloorHierarchy.indexOf(Integer.valueOf(selectedSlotFloorID))) {
            if (isCurrentFloor) {
                // draw path from position to exit
                getPath((int) Math.floor(((userPositionY * floor_map_height) * (1 / floorMapGridSize)) / floorImageHeight),
                        (int) Math.floor(((userPositionX * floor_map_width) * (1 / floorMapGridSize)) / floorImageWidth),
                        exit_x, exit_y);
            } else if (buildingFloorHierarchy.indexOf(Integer.valueOf(currentFloorID)) > buildingFloorHierarchy.indexOf(Integer.valueOf(floorID))) {
                // draw path from previous floor to exit
                if (previous_floor_x >= 0 && previous_floor_y >= 0) {
                    getPath(previous_floor_x, previous_floor_y, exit_x, exit_y);
                } else {
                    Toast.makeText(mContext, "No path found from previous floor to exit", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
