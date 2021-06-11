package com.example.ezpath;


import io.realm.RealmObject;

public class ResultGeometry extends RealmObject{
    ResultCoord location;

    public ResultCoord getLocation() {
        return location;
    }

    public void setLocation(ResultCoord location) {
        this.location = location;
    }
}
