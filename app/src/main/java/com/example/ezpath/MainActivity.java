package com.example.ezpath;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Toast;
import android.widget.ViewAnimator;
import android.widget.ViewFlipper;

import com.android.volley.VolleyError;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, AddErrandDialog.AddErrandDialogListener {

    ViewAnimator viewAnimator_dash_home;
    ViewAnimator viewAnimator_home_notes_map;
    BottomNavigationView bottom_nav_view;
    SupportMapFragment mapFragment;
    GoogleMap map;
    ListView errands_list;
    ArrayList<String> errandArray;
    ArrayAdapter<String> errandArrayAdapter;
    Button addErrandButton;
    Button sideMenuButton;
    private static final int PERMISSION_REQUEST_CODE = 1;
    DrawerLayout drawerLayout;
    IResult mResultCallBack;
    Marker currMarker;
    Place currPlace;
    GetUrlContent mGetUrlContent;
    private static final String API_KEY = BuildConfig.MAPS_API_KEY;
    ObjectMapper objectMapper;
    ErrandResults testErrandResults;
    Path testPath;
    Path path;
    ArrayList<Result> bestResults;
    Polyline polyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerSetUp();

        viewAnimator_dash_home = (ViewAnimator) findViewById(R.id.view_animator);

        viewAnimator_home_notes_map = (ViewAnimator) findViewById(R.id.view_animator_home_map_notes);

        botNavSetUp();

        checkRequestPermissions();

        initializePlaces();
        initPlaceSelectionListener();

        GoogleMapOptions options = new GoogleMapOptions();
        options.compassEnabled(true)
                .zoomControlsEnabled(true);
        mapFragment = SupportMapFragment.newInstance(options);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.map_container, mapFragment)
                .commit();
        mapFragment.getMapAsync(this);

        errandAddSetUp();
        bestResults = new ArrayList<Result>();
        objectMapper = new ObjectMapper();

        mResultCallBack = new IResult() {
            @Override
            public void notifySuccess(String requestType, JSONObject response) {
                Log.d("errand test response", "Volley requester" + requestType);
                Log.d("errand test response", "Volley JSON post" + response);
                try {
                    ErrandResults errandResults = objectMapper.readValue(response.toString(), ErrandResults.class); // map json results to object
                    testErrandResults = errandResults; // rename
                    Result[] results = errandResults.getResults();
                    errandResults.calculateDistanceMatrix(currPlace);
                    Result bestPlace = errandResults.chooseBestPlace(1500);
                    bestResults.add(bestPlace);
                    map.addMarker(new MarkerOptions()
                            .position(bestPlace.getGeometry().getLocation().getLatLng())
                            .title(bestPlace.getName()));
                    updatePolyMap();
//                    Log.d("parsed result", "best place based on rating: " + bestPlace.getName() + " location: " + bestPlace.getFormatted_address() + " coords: " + bestPlace.getGeometry().getLocation().getLatLng());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void notifyError(String requestType, VolleyError error) {
                Log.d("error response", "Volley requester" + requestType);
                Log.d("error response", "Volley JSON post" + "That didnt work!");
            }
        };
        mGetUrlContent = new GetUrlContent(mResultCallBack, this);
    }

    public void uwuTest(View v) {

        testPath = new Path(bestResults, this);
        testPath.getPolyline(currPlace, new PolyCallback() {
            @Override
            public void onSuccess() {
                polyline.remove();
                polyline = map.addPolyline(new PolylineOptions().addAll(testPath.getDecoded_poly()));
            }
        });

    }
    public void updatePolyMap() {
        testPath = new Path(bestResults, this);
        testPath.getPolyline(currPlace, new PolyCallback() {
            @Override
            public void onSuccess() {
                if (polyline != null) {
                    polyline.remove();
                }
                polyline = map.addPolyline(new PolylineOptions().addAll(testPath.getDecoded_poly()));
            }
        });
    }

    public void uwu2Test(View v) {
        polyline = map.addPolyline(new PolylineOptions().addAll(testPath.getDecoded_poly()));
    }

//    public void calculate() {
//        if ((currPlace != null) && (!bestResults.isEmpty())) {
//            path = new Path(this);
//            Result[] res = (Result[]) bestResults.toArray();
//            path.calculateDistanceMatrixAndBuildPath(currPlace, res);
//        }
//    }

    public void drawerSetUp() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        sideMenuButton = (Button) findViewById(R.id.side_menu);
        sideMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(Gravity.LEFT);
            }
        });
    }

    public void errandAddSetUp() {
        errands_list = (ListView) findViewById(R.id.errands_list);
        addErrandButton = (Button) findViewById(R.id.add_errand_button);
        addErrandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openErrandDialog();
            }
        });

        errandArray = new ArrayList<String>();
        errandArrayAdapter = new ArrayAdapter<String>(MainActivity.this, R.layout.errand_item, R.id.errand_name, errandArray);
        errands_list.setAdapter(errandArrayAdapter);
    }

    public void getErrandSearchPlaceResults(Place placeSelected, String keywords, int r) {
        //String radius = "&radius=" + Integer.toString(r);
        String latitude = Double.toString(placeSelected.getLatLng().latitude);
        String longitude = Double.toString(placeSelected.getLatLng().longitude);
        String location = "&location=" + latitude + "," + longitude;
        String query = "query=" + keywords.replaceAll(" ", "+");
        String key = "&key=" + API_KEY;
        String rankby = "&rankby=distance";

        String url = "https://maps.googleapis.com/maps/api/place/textsearch/json?";
        url += query + location + rankby + key;
        mGetUrlContent.getDataVolley("GETCALL", url);
    }

    @Override
    public void addErrand(String errand) {
        if (errandArray.size() < 9 && currPlace != null) { // max errands is 9
            errandArray.add(errand);
            getErrandSearchPlaceResults(currPlace, errand, 1);
            errandArrayAdapter.notifyDataSetChanged();
        }
    }

    public void openErrandDialog() {
        AddErrandDialog errandDialog = new AddErrandDialog();
        errandDialog.show(getSupportFragmentManager(), "errand dialog");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(0,0)));
        map = googleMap;
    }


    public void checkRequestPermissions() {
        if (checkPermission()) {
            Log.e("permission", "Permission already granted.");
        } else {
            requestPermission();
            Toast.makeText(MainActivity.this,
                    "requested", Toast.LENGTH_LONG).show();
        }
    }

    public void botNavSetUp() {
        bottom_nav_view = (BottomNavigationView) findViewById(R.id.nav_bar);
        bottom_nav_view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        navigateToHome();
                        break;

                    case R.id.notes:
                        navigateToNotes();
                        break;

                    case R.id.map:
                        navigateToMap();
                        break;
                }
                return false;
            }
        });
        bottom_nav_view.setOnNavigationItemReselectedListener(new BottomNavigationView.OnNavigationItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {
            }
        });
    }

    public void initPlaceSelectionListener() {
        //init
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        //specify type of place data returned
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        //handle response
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                LatLng latLng = place.getLatLng();
                currPlace = place;
                Toast.makeText(MainActivity.this, latLng.toString(), Toast.LENGTH_SHORT).show();

                //add function once place is selected
                if(map != null) {
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(10).build();
                    map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    if (currMarker == null) {
                        currMarker = map.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title("Current Location"));
                    } else {
                        currMarker.setPosition(latLng);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "map is null", Toast.LENGTH_SHORT).show();
                }
//
            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
                //on close or error
            }
        });
        ((View) findViewById(R.id.places_autocomplete_search_button)).setVisibility(View.GONE);
        ((EditText) findViewById(R.id.places_autocomplete_search_input)).setHint("Enter your location");
    }

    public void initializePlaces() {
        String apiKey = API_KEY;
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }
        PlacesClient placesClient = Places.createClient(this);
    }

    public boolean checkPermission() {

        int FineLocationPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);

        return FineLocationPermissionResult == PackageManager.PERMISSION_GRANTED;

    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(MainActivity.this, new String[]
                {
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case PERMISSION_REQUEST_CODE:


                if (grantResults.length > 0) {


                    boolean locationPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (locationPermission) {

                        Toast.makeText(MainActivity.this,
                                "Permission accepted", Toast.LENGTH_LONG).show();


                    } else {
                        Toast.makeText(MainActivity.this,
                                "Permission denied", Toast.LENGTH_LONG).show();

                    }
                    break;
                }
        }
    }


    public void navigateToHomeFromDash(View v) {
        viewAnimator_dash_home.setInAnimation(this, R.anim.slide_in_right);
        viewAnimator_dash_home.setOutAnimation(this, R.anim.slide_out_left);
        viewAnimator_home_notes_map.setDisplayedChild(1);
        viewAnimator_dash_home.setDisplayedChild(1);
        Menu menu = bottom_nav_view.getMenu();
        menu.findItem(R.id.home).setChecked(true);

    }
    public void navigateToHome() {
        if (viewAnimator_home_notes_map.getCurrentView().getId() == findViewById(R.id.notes_page_layout).getId()) {
            viewAnimator_home_notes_map.setInAnimation(this, R.anim.slide_in_right);
            viewAnimator_home_notes_map.setOutAnimation(this, R.anim.slide_out_left);
        } else {
            viewAnimator_home_notes_map.setInAnimation(this, android.R.anim.slide_in_left);
            viewAnimator_home_notes_map.setOutAnimation(this, android.R.anim.slide_out_right);
        }
        viewAnimator_home_notes_map.setDisplayedChild(1);
        Menu menu = bottom_nav_view.getMenu();
        menu.findItem(R.id.home).setChecked(true);
    }
    public void navigateToNotes() {
        viewAnimator_home_notes_map.setInAnimation(this, android.R.anim.slide_in_left);
        viewAnimator_home_notes_map.setOutAnimation(this, android.R.anim.slide_out_right);
        viewAnimator_home_notes_map.setDisplayedChild(0);
        Menu menu = bottom_nav_view.getMenu();
        menu.findItem(R.id.notes).setChecked(true);
    }
    public void navigateToMap() {
        viewAnimator_home_notes_map.setInAnimation(this, R.anim.slide_in_right);
        viewAnimator_home_notes_map.setOutAnimation(this, R.anim.slide_out_left);
        viewAnimator_home_notes_map.setDisplayedChild(2);
        Menu menu = bottom_nav_view.getMenu();
        menu.findItem(R.id.map).setChecked(true);
    }
    public void navigateToDash() {
        viewAnimator_dash_home.setInAnimation(this, android.R.anim.slide_in_left);
        viewAnimator_dash_home.setOutAnimation(this, android.R.anim.slide_out_right);
        viewAnimator_dash_home.setDisplayedChild(0);
    }
    @Override
    public void onBackPressed() {
        if (viewAnimator_dash_home.getCurrentView().getId() == findViewById(R.id.dashboard).getId()) {
            moveTaskToBack(true);
        } else {
            drawerLayout.closeDrawer(Gravity.LEFT);
            navigateToDash();
        }

    }
}