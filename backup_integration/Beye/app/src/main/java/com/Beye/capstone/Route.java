package com.Beye.capstone;

import java.util.ArrayList;

public class Route {
    private int type; //1 = subway, 2 = bus, 3 = walk
    private ArrayList<String> path; //경로
    private ArrayList<Double> latitude; //위도
    private ArrayList<Double> longitude;    //경도
    private String dest;
    private String startStationID;

    public Route() {
        path = new ArrayList<String>();
        latitude = new ArrayList<Double>();
        longitude = new ArrayList<Double>();
    }

    public int getSize() {
        return  path.size();
    }

    public int getType() {
        return type;
    }

    public String getPath(int index) {
        return path.get(index);
    }

    public Double getLatitude(int index) {
        return latitude.get(index);
    }

    public Double getLongitude(int index) {
        return longitude.get(index);
    }

    public String getStartStationID() { return startStationID;}

    public void addRoute(String path, Double latitude, Double longitude) {
        addpath(path);
        addLatitude(latitude);
        addLonitude(longitude);
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public void setLastPath() {
        if(type == 3) {
            this.path.set(getSize() - 1, dest + "에 도착");
        }
    }

    public void setFirstPoint(Double latitude, Double longitude) {
        this.latitude.set(0,latitude);
        this.longitude.set(0,longitude);

    }

    public void setStartStationID(String startStationID) { this.startStationID = startStationID; }

    public void setType(int type) {
        this.type = type;
    }

    private void addpath(String path) {
        this.path.add(path);
    }

    private void addLatitude(Double latitude) {
        this.latitude.add(latitude);
    }

    private void addLonitude(Double longitude) {
        this.longitude.add(longitude);
    }


}
