package com.company;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

class Ship {
    String name;
    List<HashMap<String, Integer>> occupiedCells;
    List<HashMap<String, Integer>> hitCells;
    boolean sunk;

    public Ship(String name) {
        this.name = name;
        this.occupiedCells = new ArrayList<>();
        this.hitCells = new ArrayList<>();
        this.sunk = false;
    }

    public void setPosition(int rowIndex, int colIndex){
        HashMap<String, Integer> coords = new HashMap<>();
        coords.put("row", rowIndex);
        coords.put("col", colIndex);
        occupiedCells.add(coords);
    }

    public void setHit(int rowIndex, int colIndex){
        HashMap<String, Integer> coords = new HashMap<>();
        coords.put("row", rowIndex);
        coords.put("col", colIndex);
        // validate for dupe hits
        for (HashMap<String, Integer> c: this.hitCells){
            if (Objects.equals(c, coords)) {
                throw new RuntimeException("Error: that coordinate has already been hit.  Try again.");
            }
        }
        hitCells.add(coords);
        if (isSunk()) setSunk();
        System.out.println(String.format("You %s a ship!", isSunk() ? "sank" : "hit"));
    }

    public boolean isSunk(){
        return occupiedCells.size() == hitCells.size();
    }

    public void setSunk(){
        this.sunk = true;
    }
}