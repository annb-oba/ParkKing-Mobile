package com.example.afbu.parkking;

public class FloorSpinnerObject {
    private String floorID,floorTitle;


    public FloorSpinnerObject(String floorID, String floorTitle) {
        this.floorID = floorID;
        this.floorTitle = floorTitle;

    }
    public String getFloorID() {
        return floorID;
    }
    public void setFloorID(String floorID) {
        this.floorID = floorID;
    }
    public String getFloorTitle() {
        return floorTitle;
    }

    public void setFloorTitle(String floorTitle) {
        this.floorTitle = floorTitle;
    }

}
