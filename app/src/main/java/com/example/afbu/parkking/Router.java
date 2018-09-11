package com.example.afbu.parkking;
public class Router{
    private Double xPosition;
    private Double yPosition;
    private Double parallelRouterDistance;
    private Double distanceFromUser;
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