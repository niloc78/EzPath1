package com.example.ezpath;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultGeometry {
    ResultCoord location;

    public ResultCoord getLocation() {
        return location;
    }

    public void setLocation(ResultCoord location) {
        this.location = location;
    }
}
