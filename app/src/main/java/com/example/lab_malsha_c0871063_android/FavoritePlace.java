package com.example.lab_malsha_c0871063_android;

import java.io.Serializable;

public class FavoritePlace implements Serializable {

    private String address;
    private Double longitude;
    private Double latitude;

    private String date;

    public FavoritePlace(String address, Double longitude, Double latitude, String date) {
        this.address = address;
        this.longitude = longitude;
        this.latitude = latitude;
        this.date = date;
    }

    public FavoritePlace(String address, Double longitude, Double latitude) {
        this.address = address;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getAddress() {
        return address;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
