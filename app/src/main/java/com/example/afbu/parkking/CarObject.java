package com.example.afbu.parkking;

public class CarObject {
    private String id;
    private String vehicle_owner_profile_id;
    private String model_id;
    private String used_by;
    private String plate_number;
    private String car_picture;
    private String model;
    private String brand;

    public CarObject(String id, String plate_number, String model, String brand) {
        this.id = id;
        this.plate_number = plate_number;
        this.model = model;
        this.brand = brand;
        this.used_by=null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVehicle_owner_profile_id() {
        return vehicle_owner_profile_id;
    }

    public void setVehicle_owner_profile_id(String vehicle_owner_profile_id) {
        this.vehicle_owner_profile_id = vehicle_owner_profile_id;
    }

    public String getModel_id() {
        return model_id;
    }

    public void setModel_id(String model_id) {
        this.model_id = model_id;
    }

    public String getUsed_by() {
        return used_by;
    }

    public void setUsed_by(String used_by) {
        this.used_by = used_by;
    }

    public String getPlate_number() {
        return plate_number;
    }

    public void setPlate_number(String plate_number) {
        this.plate_number = plate_number;
    }

    public String getCar_picture() {
        return car_picture;
    }

    public void setCar_picture(String car_picture) {
        this.car_picture = car_picture;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }
}
