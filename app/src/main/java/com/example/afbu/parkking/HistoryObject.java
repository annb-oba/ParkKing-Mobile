package com.example.afbu.parkking;

import org.json.JSONException;
import org.json.JSONObject;

class HistoryObject {
    String buildingTitle;
    String slotDirectory;
    String plateNumber;
    String carMake;
    String timeIn;
    String timeOut;
    String parkingDuration;

    String amountTendered;
    String amountIncurred;
    String billingType;
    boolean hasTransaction;

    public HistoryObject(JSONObject historyObject) {
        this.amountTendered = "P 00.00";
        this.amountIncurred = "P 00.00";
        this.billingType = "N/A";
        this.hasTransaction = false;

        try {
            this.buildingTitle = historyObject.getString("building");
            this.slotDirectory = historyObject.getString("slot_directory");
            this.plateNumber = historyObject.getString("plate_number");
            this.carMake = historyObject.getString("car_make");
            this.timeIn = historyObject.getString("time_in");
            this.timeOut = historyObject.getString("time_out");
            this.parkingDuration = historyObject.getString("parking_duration");

            if (historyObject.has("parking_transaction")) {
                JSONObject parkingTransaction = historyObject.getJSONObject("parking_transaction");
                this.amountIncurred = parkingTransaction.getString("amount_incurred");
                this.amountTendered = parkingTransaction.getString("amount_tendered");
                this.billingType = parkingTransaction.getString("billing_type");
                this.hasTransaction = true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getBuildingTitle() {
        return buildingTitle;
    }

    public String getSlotDirectory() {
        return slotDirectory;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public String getCarMake() {
        return carMake;
    }

    public String getTimeIn() {
        return "In: " + timeIn;
    }

    public String getTimeOut() {
        return "Out: " + timeOut;
    }

    public String getAmountTendered() {
        return amountTendered;
    }

    public String getAmountIncurred() {
        return amountIncurred;
    }

    public String getBillingType() {
        return billingType;
    }

    public String getParkingDuration() {
        return parkingDuration;
    }

    public boolean hasTransaction() {
        return hasTransaction;
    }
}
