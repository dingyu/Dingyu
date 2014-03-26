package com.example.dingyu.bean;

import com.example.dingyu.support.utils.ObjectToStringUtility;

import java.io.Serializable;

/**
 * User: 
 * Date: 12-7-31
 * "geo":{"type":"Point","coordinates":[30.1953,120.199235]}
 */
public class GeoBean implements Serializable {
    private String type;
    private double[] coordinates = {0.0, 0.0};

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double[] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(double[] coordinates) {
        this.coordinates = coordinates;
    }

    public double getLat() {
        return coordinates[0];
    }

    public double getLon() {
        return coordinates[1];
    }

    public void setLatitude(double lat) {
        coordinates[0] = lat;
    }

    public void setLongitude(double lon) {
        coordinates[1] = lon;
    }

    @Override
    public String toString() {
        return ObjectToStringUtility.toString(this);
    }
}
