package com.example.vince.testastarmobile.AStar;

import android.os.Build;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class AStar {
    public static final int DIAGONAL_COST = 14;
    public static final int V_H_COST = 10;

    private Grid[][] grid;
    private PriorityQueue<Grid> openGrids;
    private boolean[][] closedGrids;

    private int startX, startY;
    private int endX, endY;

    private String output, scores, solution;

    public AStar(int width, int height, List<int[]> blocks) {
        grid = new Grid[width][height];
        closedGrids = new boolean[width][height];

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            openGrids = new PriorityQueue<Grid>((Grid c1, Grid c2) -> {
                return Integer.compare(c1.finalCost, c2.finalCost);
            });
        } else {
            openGrids = new PriorityQueue<Grid>(11, (Grid c1, Grid c2) -> {
                return Integer.compare(c1.finalCost, c2.finalCost);
            });
        }

        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid[x].length; y++) {
                grid[x][y] = new Grid(x, y);
                grid[x][y].heuristicCost = Math.abs(x - endX) + Math.abs(y - endY);
                grid[x][y].solution = false;
            }
        }

        for (int blocked_grid[] :
                blocks) {
            addBlockOnGrid(blocked_grid[0], blocked_grid[1]);
        }
    }

    public void addBlockOnGrid(int x, int y) {
        grid[x][y] = null;
    }

    public void startGrid(int x, int y) {
        startX = x;
        startY = y;

        grid[startX][startY].finalCost = 0;
    }

    public void endGrid(int ex, int ey) {
        endX = ex;
        endY = ey;
    }

    public void updateCost(Grid current, Grid t, int cost) {
        if (t == null || closedGrids[t.x][t.y]) {
            return;
        }

        int tFinalCost = t.heuristicCost + cost;
        boolean isOpen = openGrids.contains(t);

        if (!isOpen || tFinalCost < t.finalCost) {
            t.finalCost = tFinalCost;
            t.parent = current;

            if (!isOpen) {
                openGrids.add(t);
            }
        }
    }

    public void process() {
        openGrids.add(grid[startX][startY]);
        Grid current;

        while (true) {
            current = openGrids.poll();

            if (current == null)
                break;

            closedGrids[current.x][current.y] = true;

            if (current.equals(grid[endX][endY])) {
                return;
            }

            Grid t;

            if (current.x - 1 >= 0) {
                t = grid[current.x - 1][current.y];
                updateCost(current, t, current.finalCost + V_H_COST);

                if (current.y - 1 >= 0) {
                    t = grid[current.x - 1][current.y - 1];
                    updateCost(current, t, current.finalCost + DIAGONAL_COST);
                }

                if (current.y + 1 < grid[0].length) {
                    t = grid[current.x - 1][current.y + 1];
                    updateCost(current, t, current.finalCost + DIAGONAL_COST);
                }
            }

            if (current.y - 1 >= 0) {
                t = grid[current.x][current.y - 1];
                updateCost(current, t, current.finalCost + V_H_COST);
            }

            if (current.y + 1 < grid[0].length) {
                t = grid[current.x][current.y + 1];
                updateCost(current, t, current.finalCost + V_H_COST);
            }

            if (current.x + 1 < grid.length) {
                t = grid[current.x + 1][current.y];
                updateCost(current, t, current.finalCost + V_H_COST);

                if (current.y - 1 >= 0) {
                    t = grid[current.x + 1][current.y - 1];
                    updateCost(current, t, current.finalCost + DIAGONAL_COST);
                }

                if (current.y + 1 < grid[0].length) {
                    t = grid[current.x + 1][current.y + 1];
                    updateCost(current, t, current.finalCost + DIAGONAL_COST);
                }
            }
        }
    }

    public void display() {
        output = "Grid :\n";
        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid[x].length; y++) {
                if (x == startX && y == startY) {
                    output += "SO\t"; //source
                } else if (x == endX && y == endY) {
                    output += "DE\t"; //destination
                } else if (grid[x][y] != null) {
                    output += String.format("%-3d\t", 0);
                } else {
                    output += "BL\t"; // blocked
                }
            }

            output += "\n";
        }

        output += "\n";
    }

    public void displayScores() {
        scores = "Scores for grids:\n";

        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid[x].length; y++) {
                if(grid[x][y] != null) {
                    scores += String.format("%-3d\t", grid[x][y].finalCost);
                } else {
                    scores += "BL\t";
                }
            }

            scores += "\n";
        }
    }

    public List<Grid> getSolution() {
        List<Grid> solutionGrids = new ArrayList<>();
        if(closedGrids[endX][endY]) {
            Grid current = grid[endX][endY];
            solutionGrids.add(current);

            grid[current.x][current.y].solution = true;

            while(current.parent != null) {
                solutionGrids.add(current.parent);
                grid[current.parent.x][current.parent.y].solution = true;

                current = current.parent;
            }
        }

        return solutionGrids;
    }
}
