package com.example.afbu.parkking;
public class Router{
    private Double xPosition;
    private Double yPosition;
    private Double parallelRouterDistance;
    private Double distanceFromUser;
    private String SSID,id,macAddress;
    public Router(Double xPos, Double yPos, Double parallel){
        xPosition=xPos;
        yPosition=yPos;
        parallelRouterDistance=parallel;
    }
    public Router(Double xPos, Double yPos){
        xPosition=xPos;
        yPosition=yPos;
        parallelRouterDistance=null;
    }
    public Router(String SSID,String id){
        this.SSID=SSID;
        this.id=id;
    }

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public Double getParallelRouterDistance() {
        return parallelRouterDistance;
    }

    public void setParallelRouterDistance(Double parallelRouterDistance) {
        this.parallelRouterDistance = parallelRouterDistance;
    }

    public Double getDistanceFromUser() {
        return distanceFromUser;
    }

    public void setDistanceFromUser(Double distanceFromUser) {
        this.distanceFromUser = distanceFromUser;
    }



    public Double getyPosition() {
        return yPosition;
    }

    public void setyPosition(Double yPosition) {
        this.yPosition = yPosition;
    }

    public void Router(Double x, Double y){
        xPosition=x;

        yPosition=y;
    }
    public Double getxPosition() {
        return xPosition;
    }

    public void setxPosition(Double xPosition) {
        this.xPosition = xPosition;
    }
}