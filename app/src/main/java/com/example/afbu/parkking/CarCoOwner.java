package com.example.afbu.parkking;

public class CarCoOwner {
    private String name,email,picture,co_owner_id;

    public CarCoOwner(String co_owner_id,String name, String email, String picture) {
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.co_owner_id = co_owner_id;

    }

    public String getCo_owner_id() {
        return co_owner_id;
    }

    public void setCo_owner_id(String co_owner_id) {
        this.co_owner_id = co_owner_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }
}
