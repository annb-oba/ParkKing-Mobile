package com.example.afbu.parkking;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SectionSlot {
    LatLng pointA, pointB, pointC, pointD;
    LatLng indicatorCoordinate;
    int sectionID, slotID;
    int billingID;
    int curr_stat;
    int[] grid_coordinates;
    boolean first_read;
    private String slotTitle;

    public void setFirst_read(boolean first_read) {
        this.first_read = first_read;
    }

    public int[] getGrid_coordinates() {
        return grid_coordinates;
    }

    public SectionSlot(JSONObject slotObject, Double floor_width, Double floor_height) {
        Log.d("SectionSlot", "created");
        try {
            this.indicatorCoordinate = new LatLng(slotObject.getDouble("x"), slotObject.getDouble("y"));
            this.sectionID = slotObject.getInt("section_id");
            this.slotID = slotObject.getInt("slot_id");
            this.billingID = slotObject.getInt("billing");
            this.curr_stat = slotObject.getInt("curr_stat");
            this.slotTitle = slotObject.getString("slot_tag");
            this.first_read = true;
            this.grid_coordinates = new int[]{slotObject.getInt("grid_x"), slotObject.getInt("grid_y")};

            JSONArray slotPoints = slotObject.getJSONArray("points");
            if (slotPoints.length() == 4) {
                JSONArray point1_coord = slotPoints.getJSONArray(0);
                pointA = new LatLng(
                        Double.parseDouble(String.format("%.5f", point1_coord.getDouble(0) * floor_width)),
                        Double.parseDouble(String.format("%.5f", point1_coord.getDouble(1) * floor_height))
                );

                JSONArray point2_coord = slotPoints.getJSONArray(1);
                pointB = new LatLng(
                        Double.parseDouble(String.format("%.5f", point2_coord.getDouble(0) * floor_width)),
                        Double.parseDouble(String.format("%.5f", point2_coord.getDouble(1) * floor_height))
                );

                JSONArray point3_coord = slotPoints.getJSONArray(2);
                pointD = new LatLng(
                        Double.parseDouble(String.format("%.5f", point3_coord.getDouble(0) * floor_width)),
                        Double.parseDouble(String.format("%.5f", point3_coord.getDouble(1) * floor_height))
                );

                JSONArray point4_coord = slotPoints.getJSONArray(3);
                pointC = new LatLng(
                        Double.parseDouble(String.format("%.5f", point4_coord.getDouble(0) * floor_width)),
                        Double.parseDouble(String.format("%.5f", point4_coord.getDouble(1) * floor_height))
                );
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public LatLng getPointA() {
        return pointA;
    }

    public LatLng getPointB() {
        return pointB;
    }

    public LatLng getPointC() {
        return pointC;
    }

    public LatLng getPointD() {
        return pointD;
    }

    public LatLng getIndicatorCoordinate() {
        return indicatorCoordinate;
    }

    public int getSectionID() {
        return sectionID;
    }

    public int getSlotID() {
        return slotID;
    }

    public int getBillingID() {
        return billingID;
    }

    public int getCurr_stat() {
        return curr_stat;
    }

    public boolean isFirst_read() {
        return first_read;
    }

    public void setCurr_stat(int curr_stat) {
        this.curr_stat = curr_stat;
    }

    public String getSlotTitle() {
        return slotTitle;
    }
}
