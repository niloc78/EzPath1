package com.example.ezpath;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Result {
    String business_status;
    int price_level;
    String formatted_address;
    ResultGeometry geometry;
    String name;
    Object opening_hours;
    String place_id;
    Double rating;
    String[] types;

    public String getBusiness_status() {
        return business_status;
    }

    public int getPrice_level() {
        return price_level;
    }

    public ResultGeometry getGeometry() {
        return geometry;
    }

    public Double getRating() {
        return rating;
    }

    public Object getOpening_hours() {
        return opening_hours;
    }

    public String getName() {
        return name;
    }

    public String getPlace_id() {
        return place_id;
    }

    public String[] getTypes() {
        return types;
    }

    public void setGeometry(ResultGeometry geometry) {
        this.geometry = geometry;
    }

    public void setPrice_level(int price_level) {
        this.price_level = price_level;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOpening_hours(Object opening_hours) {
        this.opening_hours = opening_hours;
    }

    public void setPlace_id(String place_id) {
        this.place_id = place_id;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public void setTypes(String[] types) {
        this.types = types;
    }

    public String getFormatted_address() {
        return formatted_address;
    }

    public void setBusiness_status(String business_status) {
        this.business_status = business_status;
    }

    public void setFormatted_address(String formatted_address) {
        this.formatted_address = formatted_address;
    }
}
