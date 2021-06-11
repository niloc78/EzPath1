package com.example.ezpath;

import android.content.Context;

import android.util.Log;

import com.android.volley.VolleyError;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.libraries.places.api.model.Place;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;

import org.json.JSONObject;

import java.util.ArrayList;

import java.util.List;


public class Path {
    private String overview_polyline;
    private Context context;
    private ArrayList<Result> bestResults;
    private List<LatLng> decoded_poly;


    public Path(ArrayList<Result> res, Context context) {
        this.bestResults = res;
        this.context = context;
    }
    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public List<LatLng> getDecoded_poly() {
        return decoded_poly;
    }

    public String getOverview_polyline() {
        return overview_polyline;
    }

    public ArrayList<Result> getBestResults() {
        return bestResults;
    }

    public void setBestResults(ArrayList<Result> bestResults) {
        this.bestResults = bestResults;
    }

    public void setDecoded_poly(List<LatLng> decoded_poly) {
        this.decoded_poly = decoded_poly;
    }

    public void setOverview_polyline(String overview_polyline) {
        this.overview_polyline = overview_polyline;
    }

    public void getPolyline(Place source, final PolyCallback callBack) {
        IResult mResultCallback = new IResult() {
            @Override
            public void notifySuccess(String requestType, JSONObject response) {
                try {
                    Log.d("Direction results ", "Direction results: " + response.toString());
                    JSONArray routes = response.getJSONArray("routes");
                    JSONObject route = routes.getJSONObject(0);
                    JSONObject polyline = route.getJSONObject("overview_polyline");
                    overview_polyline = polyline.getString("points");
                    Log.d("overview_polyline", "overview polyline: " + overview_polyline);

                    decoded_poly = PolyUtil.decode(overview_polyline);
                    Log.d("decoded_poly", "decoded poly: " + decoded_poly);
                    callBack.onSuccess();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void notifyError(String requestType, VolleyError error) {
                Log.d("calculate matrix error", "error");
            }

            public void notifySuccess(String requestType, JSONObject response, String errand) {
                //not used, stay empty
            }
        };

        GetUrlContent mGetUrlContent = new GetUrlContent(mResultCallback, context);

        String origin = "origin=place_id:" + source.getId();
        String destination = "&destination=place_id:" + source.getId();
        String waypoints = "&waypoints=optimize:true|" + "place_id:" + bestResults.get(0).getPlace_id();
        String api_key = "&key=" + BuildConfig.MAPS_API_KEY;

        for (int i = 1; i <= bestResults.size() - 1; i++) {
            waypoints += "|place_id:" + bestResults.get(i).getPlace_id();
        }

        String url = "https://maps.googleapis.com/maps/api/directions/json?";
        url += origin + destination + waypoints + api_key;
        mGetUrlContent.getDataVolley("GETCALL", url);

    }

}
