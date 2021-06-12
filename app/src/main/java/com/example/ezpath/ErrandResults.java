package com.example.ezpath;


import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.google.maps.android.SphericalUtil;


import org.bson.types.ObjectId;

import java.util.Arrays;


import io.realm.RealmObject;
import io.realm.annotations.Ignore;

public class ErrandResults extends RealmObject {
    @Ignore String[] html_attributions;
    String next_page_token;
    @Ignore Result[] results;
    Result bestPlace;
    String errand;
    @Ignore int[][] distMatrix;

    public ErrandResults(){
    }


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
    public Result chooseBestPlace(int r, int price_level) {
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
    public Result chooseBestPlace(int r, int price_level, double rating, boolean chip_rating) {
        bestPlace = results[0];
        for (int i = 0; i < results.length - 1; i++) {
            if (distMatrix[0][i] > r) {
                continue;
            }

            if ((results[i].getPrice_level() != price_level) && (price_level != 0)) {
                continue;
            }

            if (results[i].getRating() < rating) {
                continue;
            }

            if (chip_rating == true) {
                if (results[i + 1].getRating() > bestPlace.getRating()) {
                    bestPlace = results[i + 1];
                }
            } else {
                break;
            }

        }
        return bestPlace;
    }
}
