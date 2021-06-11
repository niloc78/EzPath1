package com.example.ezpath;


import com.google.android.gms.maps.model.LatLng;

import io.realm.RealmObject;


public class ResultCoord extends RealmObject{
    Double lat;
    Double lng;

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public LatLng getLatLng() {
        return new LatLng(lat, lng);
    }
    public String getLatLngAsString() {
        return Double.toString(lat) + "," + Double.toString(lng);
    }
}
