package com.example.ezpath;

import android.content.Context;
import android.util.Log;

import com.android.volley.VolleyError;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.google.maps.android.SphericalUtil;

import org.json.JSONObject;

import java.util.Arrays;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrandResults {
    @JsonProperty("html_attributions") String[] html_attributions;
    String next_page_token;
    Result[] results;
    Result bestPlace;
    String errand;
    int[][] distMatrix;

    public String[] getHtml_attribution() {
        return html_attributions;
    }

    public String getNext_page_token() {
        return next_page_token;
    }



    public int[][] getDistMatrix() {
        return distMatrix;
    }

    public void setDistMatrix(int[][] distMatrix) {
        this.distMatrix = distMatrix;
    }
    public String distMatrixToString() {
        String matrix = "";
        for (int i = 0; i <= distMatrix.length - 1; i++) {
            matrix += Arrays.toString(distMatrix[i]) + ",\n";
        }
        return matrix;
    }
    public void calculateDistanceMatrix(Place source) {
        distMatrix = new int[1][results.length];
        LatLng latLng = source.getLatLng();
        for (int i = 0; i <= results.length - 1; i++) {
            double distance = SphericalUtil.computeDistanceBetween(latLng, results[i].getGeometry().getLocation().getLatLng());
            distMatrix[0][i] = (int) distance;
        }
        Log.d("Distance matrix", "dist matrix: " + distMatrixToString());

    }

    public String[] getHtml_attributions() {
        return html_attributions;
    }

    public void setHtml_attributions(String[] html_attributions) {
        this.html_attributions = html_attributions;
    }


    public Result[] getResults() {
        return results;
    }

    public String getErrand() {
        return errand;
    }

    public void setErrand(String errand) {
        this.errand = errand;
    }

    public Result getBestPlace() {
        return bestPlace;
    }

    public void setBestPlace(Result bestPlace) {
        this.bestPlace = bestPlace;
    }

    public void setHtml_attribution(String[] html_attributions) {
        this.html_attributions = html_attributions;
    }

    public void setNext_page_token(String next_page_token) {
        this.next_page_token = next_page_token;
    }

    public void setResults(Result[] results) {
        this.results = results;
    }

    public Result chooseBestPlace(int r) {
        bestPlace = results[0];
        for (int i = 0; i < results.length - 1; i++) {
            if (distMatrix[0][i] > r) {
                continue;
            }
            if (results[i + 1].getRating() > bestPlace.getRating()) {
                bestPlace = results[i + 1];
            }
        }
        return bestPlace;
    }
}
