package com.example.ezpath;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.android.volley.VolleyError;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.libraries.places.api.model.Place;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

public class Path {
    LinkedList<Result> bestPath;
    private Context context;
    private int[] distMatrix;

    public Path(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public int[] getDistMatrix() {
        return distMatrix;
    }

    public LinkedList<Result> getBestPath() {
        return bestPath;
    }

    public void setDistMatrix(int[] distMatrix) {
        this.distMatrix = distMatrix;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setBestPath(LinkedList<Result> bestPath) {
        this.bestPath = bestPath;
    }
    public void append(Result r) {
        bestPath.add(r);
    }
    public Result remove(int i) {
        return bestPath.remove(i);
    }
    public Result pop() {
        return bestPath.pollLast();
    }
    public Result dequeue() {
        return bestPath.pollFirst();
    }
    public void insertAt(int i, Result r) {
        bestPath.add(i, r);
    }
    public void clear() {
        bestPath.clear();
    }
    public boolean isEmpty() {
        return bestPath.isEmpty();
    }
    public Result getFirst() {
        return bestPath.peekFirst();
    }
    public Result getLast() {
        return bestPath.peekLast();
    }
    public Result get(int i) {
        return bestPath.get(i);
    }
    public void calculateDistanceMatrix(Place source, Result[] bestResults) {
        IResult mResultCallback = new IResult() {
            @Override
            public void notifySuccess(String requestType, JSONObject response) {
                try {
                    Log.d("Volley matrix response", "Volley matrix response: " + response);
                    JSONArray elements = response.getJSONArray("rows").getJSONObject(0).getJSONArray("elements");
                    Log.d("elements: ", "elements: " + elements.toString());
                    distMatrix = new int[elements.length()];
                    for (int i = 0; i <= elements.length() - 1; i++) {
                        distMatrix[i] = elements.getJSONObject(i).getJSONObject("distance").getInt("value");
                        Log.d("added", "added: " + distMatrix[i]);
                    }
                    Log.d("calculate matrix sucess", "success");
                    Log.d("DISTANCE MATRIX: ", Arrays.toString(getDistMatrix()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void notifyError(String requestType, VolleyError error) {
                Log.d("calculate matrix error", "error");
            }
        };

        GetUrlContent mGetUrlContent = new GetUrlContent(mResultCallback, context);

        String origin = "origins=place_id:" + source.getId();
        String destinations = "&destinations=place_id:" + bestResults[0].getPlace_id();
        String api_key = "&key=" + BuildConfig.MAPS_API_KEY;

        for (int i = 1; i <= bestResults.length - 1; i++) {
            if (bestResults[i].getPlace_id() == null) {
                Log.d("place id null:", "place id is null");
            }
            destinations += "|" + "place_id:" + bestResults[i].getPlace_id();

        }

        String url = "https://maps.googleapis.com/maps/api/distancematrix/json?";
        url += origin + destinations + api_key;
        mGetUrlContent.getDataVolley("GETCALL", url);
    }

    public void buildBestPath(String source_id, Result[] bestResults) {

    }
}
