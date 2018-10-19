package com.example.afbu.parkking;

class HistoryObject {
    private String building_name;
    private String slot_id;
    private String time_in, time_out, amount_incurred;

    public HistoryObject() {
        this.building_name = "";
        this.slot_id = "";
        this.time_in = "";
        this.time_out = "";
        this.amount_incurred = "";
    }

    public String getAmount_incurred() {
        return amount_incurred;
    }

    public void setAmount_incurred(String amount_incurred) {
        this.amount_incurred = amount_incurred;
    }

    public String getBuilding_name() {
        return building_name;
    }

    public void setBuilding_name(String building_name) {
        this.building_name = building_name;
    }

    public String getSlot_id() {
        return slot_id;
    }

    public void setSlot_id(String slot_id) {
        this.slot_id = slot_id;
    }

    public String getTime_in() {
        return time_in;
    }

    public void setTime_in(String time_in) {
        this.time_in = time_in;
    }

    public String getTime_out() {
        return time_out;
    }

    public void setTime_out(String time_out) {
        this.time_out = time_out;
    }
}
