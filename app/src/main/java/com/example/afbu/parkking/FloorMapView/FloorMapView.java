package com.example.afbu.parkking.FloorMapView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Toast;

import com.example.afbu.parkking.CarObject;
import com.example.afbu.parkking.R;
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
import java.util.List;

public class FloorMapView extends View {
    private Bitmap floorImage;
    private List<Bitmap> floorIndicators;
    private List<JSONObject> floorIndicatorCoords;
    private Context mContext;
    private float userPositionX, userPositionY;
    private float floor_map_width,floor_map_height;

    private List<Bitmap> floorSlots;
    private List<JSONObject> floorSlotsInformation;
    private static final String[] slotStatusFile = {"open.png", "occupied.png", "closed.png"};
    private Bitmap userBitmap;
    private float floorImagePosX, floorImagePosY;
    private float floorImageWidth, floorImageHeight;
    private float mLastTouchX, mLastTouchY;

    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;
    private final static float MIN_ZOOM = 1f, MAX_ZOOM = 3.0f;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private  DatabaseReference slotRef;

    public FloorMapView(Context context) {
        super(context);
        init(null,context);
    }

    public FloorMapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(null,context);
    }

    public FloorMapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(null,context);
    }

    public FloorMapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(null,context);
    }

    private void init(@Nullable AttributeSet set, Context context) {
        this.mContext = context;

        floorImage = null;
        floorIndicators = new ArrayList<>();
        floorIndicatorCoords = new ArrayList<>();
        floorSlots = new ArrayList<>();
        floorSlotsInformation = new ArrayList<>();

        userPositionX = 500f;
        userPositionY = 500f;

        floorImagePosX = 0f;
        floorImagePosY = 0f;
        mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());

        userBitmap = null;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (floorImage != null) {
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

            for (int i = 0; i < floorSlots.size(); i ++) {
                try {
                    float floorSlotX = ((float) floorSlotsInformation.get(i).getDouble("x") * floorImageWidth - 50) + floorImagePosX;
                    float floorSlotY = ((float) floorSlotsInformation.get(i).getDouble("y") * floorImageHeight - 150) + floorImagePosY;
                    Log.w("LOG", "Slot X: "+ Double.toString(floorSlotsInformation.get(i).getDouble("x")));
                    Log.w("LOG", "Image Width: "+ Double.toString(floorImageWidth));
                    Log.w("LOG", "Floor Image Pos X: "+ Double.toString(floorImagePosX));
                    Log.w("LOG", "Floor Slot X: "+ Float.toString(floorSlotX));
                    canvas.drawBitmap(floorSlots.get(i), floorSlotX, floorSlotY, null);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            canvas.drawBitmap(userBitmap,(userPositionX-(getWidth()/10f)/2)+floorImagePosX,(userPositionY-(getWidth()/10f)/2)+floorImagePosY,null);

        }
    }

    public void setFloorMapInformation(String floorMapURL, JSONArray floorIndicatorsJSONArray, JSONArray floorSlotsJSONArray,Double floor_width, Double floor_height) {
        floorImagePosX = 0f;
        floorImagePosY = 0f;
        floorIndicators = new ArrayList<>();
        floorIndicatorCoords = new ArrayList<>();

        floor_map_width = floor_width.floatValue();
        floor_map_height = floor_height.floatValue();

        floorSlots = new ArrayList<>();
        floorSlotsInformation = new ArrayList<>();

        if (floorIndicatorsJSONArray.length() == 0) {
            Toast.makeText(getContext(), "None", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Has!", Toast.LENGTH_SHORT).show();
            for (int i = 0; i < floorIndicatorsJSONArray.length(); i++) {
                try {
                    floorIndicatorCoords.add(floorIndicatorsJSONArray.getJSONObject(i));
                    getIndicatorBitmap(floorIndicatorsJSONArray.getJSONObject(i).getString("img"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        if (floorSlotsJSONArray.length() == 0) {
            Toast.makeText(getContext(), "No Slots", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Has Slots", Toast.LENGTH_SHORT).show();
            for (int i = 0; i < floorSlotsJSONArray.length(); i++) {
                try {
                    floorSlotsInformation.add(floorSlotsJSONArray.getJSONObject(i));
                    getSlotBitmap();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        new RetrieveFloorImageTask().execute(floorMapURL);
        setSlotStatusListeners();
        userBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.user_position);
        Matrix matrix = new Matrix();
        RectF src = new RectF(0, 0, userBitmap.getWidth(), userBitmap.getHeight());
        RectF dest = new RectF(0, 0, getWidth()/10f, getWidth()/10f);
        matrix.setRectToRect(src, dest, Matrix.ScaleToFit.CENTER);
        userBitmap=Bitmap.createBitmap(userBitmap, 0, 0, userBitmap.getWidth(), userBitmap.getHeight(), matrix, true);
        postInvalidate();
    }

    private void setSlotStatusListeners() {
        for(int i = 0; i < floorSlotsInformation.size(); i ++) {
            JSONObject floorSlotObject = floorSlotsInformation.get(i);
            try {
                final int slot_id = floorSlotObject.getInt("slot_id");
                int section_id = floorSlotObject.getInt("section_id");

                slotRef = database.getReference("section").child(String.valueOf(section_id)).child("slot").child(String.valueOf(slot_id));

                final int finalI = i;
                slotRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        changeSlotBitmapStatus(slotStatusFile[Integer.valueOf(String.valueOf(dataSnapshot.child("status").getValue()))], finalI);
                        Toast.makeText(getContext(), String.valueOf(slot_id), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void changeSlotBitmapStatus(String slotURL, final int slotIndex) {
        @SuppressLint("StaticFieldLeak")
        RetrieveFloorIndicatorImg retrieveFloorIndicatorImg = new RetrieveFloorIndicatorImg() {
            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);

                floorSlots.set(slotIndex, bitmap);
                postInvalidate();
            }
        };

        retrieveFloorIndicatorImg.execute(getResources().getString(R.string.system_files) + slotURL);
    }

    private void getSlotBitmap() {
        @SuppressLint("StaticFieldLeak")
        RetrieveFloorIndicatorImg retrieveFloorIndicatorImg = new RetrieveFloorIndicatorImg() {
            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);

                floorSlots.add(bitmap);
            }
        };

        retrieveFloorIndicatorImg.execute(getResources().getString(R.string.system_files) + "default-slot.png");
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
        float proportion = getHeight() / floorImage.getHeight();
        floorImageWidth = proportion * floorImage.getWidth();
        floorImageHeight = getHeight();

        Matrix matrix = new Matrix();

        RectF src = new RectF(0, 0, floorImage.getWidth(), floorImage.getHeight());
        RectF dest = new RectF(0, 0, floorImageWidth, floorImageHeight);

        matrix.setRectToRect(src, dest, Matrix.ScaleToFit.FILL);

        return Bitmap.createBitmap(floorImage, 0, 0, floorImage.getWidth(), floorImage.getHeight(), matrix, true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean val = super.onTouchEvent(event);

        mScaleDetector.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                // get coords of clicked point
                final float x = event.getX();
                final float y = event.getY();

                // remember the last touched
                mLastTouchX = x;
                mLastTouchY = y;

                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final float x = event.getX();
                final float y = event.getY();

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
        }

        return true;
    }
    public void repositionUserBitmap(Double userPercentageX,Double userPercentageY){
        userPositionX = (userPercentageX.floatValue()/floor_map_width)*floorImageWidth;
        userPositionY = (userPercentageY.floatValue()/floor_map_height)*floorImageHeight;
        Log.w("LOG", "CANVAS X: "+ Float.toString(userPositionX));
        Log.w("LOG", "CANVAS Y: "+ Float.toString(userPositionY));

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
}
