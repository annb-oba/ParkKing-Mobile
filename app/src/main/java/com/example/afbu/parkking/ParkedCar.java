package com.example.afbu.parkking;

public class ParkedCar {
    private String plate_number, car_make;
    private String time_in, bill, building, floor, section, slot;
    private boolean currently_used;
    private int car_id, vehicle_log_id;

    public ParkedCar(String plate_number, String car_make, String time_in, String bill, String building, String floor, String section, String slot, boolean currently_used, int car_id, int vehicle_log_id) {
        this.plate_number = plate_number;
        this.car_make = car_make;
        this.time_in = time_in;
        this.bill = bill;
        this.building = building;
        this.floor = floor;
        this.section = section;
        this.slot = slot;
        this.currently_used = currently_used;
        this.car_id = car_id;
        this.vehicle_log_id = vehicle_log_id;
    }

    public String getPlate_number() {
        return plate_number;
    }

    public String getCar_make() {
        return car_make;
    }

    public String getTime_in() {
        return time_in;
    }

    public String getBill() {
        return "P " + bill;
    }

    public String getBuilding() {
        return building;
    }

    public String getFloor() {
        return floor;
    }

    public String getSection() {
        return section;
    }

    public String getSlot() {
        return slot;
    }

    public boolean isCurrently_used() {
        return currently_used;
    }

    public int getCar_id() {
        return car_id;
    }

    public int getVehicle_log_id() {
        return vehicle_log_id;
    }
}
