package com.example.afbu.parkking.AStar;

public class Grid {

    public int x, y;
    public Grid parent;

    public int heuristicCost;
    public int finalCost;

    public boolean solution;

    public Grid(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "[" + x + "," + y + "]";
    }
}
