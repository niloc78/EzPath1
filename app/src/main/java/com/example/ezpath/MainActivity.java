package com.example.ezpath;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RatingBar;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.Slider;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, AddErrandDialog.AddErrandDialogListener {

    ViewAnimator viewAnimator_dash_home;
    ViewAnimator viewAnimator_home_notes_map;
    BottomNavigationView bottom_nav_view;
    SupportMapFragment mapFragment;
    GoogleMap map;
    ListView errands_list;
    ArrayList<ErrandResults> errandArray;
    ErrandAdapter errandArrayAdapter;
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
    ArrayList<Marker> markers;
    Polyline polyline;
    Chip chip_distance;
    Chip chip_rating;
    Button price_level_0;
    Button price_level_1;
    Button price_level_2;
    Button price_level_3;
    Button price_level_4;
    private int price_level = 0;
    private double rating = 1;
    private int radius = 1500;
    Slider radiusSlider;
    RatingBar ratingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        drawerSetUp();

        viewAnimator_dash_home = (ViewAnimator) findViewById(R.id.view_animator);

        viewAnimator_home_notes_map = (ViewAnimator) findViewById(R.id.view_animator_home_map_notes);

        botNavSetUp();

        ratingBar = (RatingBar) findViewById(R.id.rating_bar);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                rating = ratingBar.getRating();
                Log.d("rating", "rating: " + rating);
            }
        });

        radiusSlider = (Slider) findViewById(R.id.radius_slider);
        radiusSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                radius = (int) radiusSlider.getValue();
            }
        });

        chip_rating = (Chip) findViewById(R.id.chip_rating);
        chip_rating.setChecked(true);


        checkRequestPermissions();

        priceLevelButtonsSetUp();

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
        markers = new ArrayList<Marker>();
        mResultCallBack = new IResult() {
            @Override
            public void notifySuccess(String requestType, JSONObject response, String errand) {
                Log.d("errand test response", "Volley requester" + requestType);
                Log.d("errand test response", "Volley JSON post" + response);
                try {
                    ErrandResults errandResults = objectMapper.readValue(response.toString(), ErrandResults.class);// map json results to object
                    errandResults.setErrand(errand); // set errand string
                    testErrandResults = errandResults; // rename
                    Result[] results = errandResults.getResults(); // all possible https results
                    errandResults.calculateDistanceMatrix(currPlace); // matrix of distances from source to each result
                    Result bestPlace = errandResults.chooseBestPlace(radius, price_level, rating, chip_rating.isChecked());
                    bestResults.add(bestPlace);

                    errandArray.add(errandResults); // change to add errandResults object
                    errandArrayAdapter.notifyDataSetChanged();

                    markers.add(map.addMarker(new MarkerOptions()
                            .position(bestPlace.getGeometry().getLocation().getLatLng())
                            .title(bestPlace.getName())));
                    updatePolyMap();
                    Log.d("best results size", "best results size: " + bestResults.size());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            public void notifySuccess(String requestType, JSONObject response) {
                //not used/ stay empty
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



    public void priceLevelButtonsSetUp() {

        price_level_0 = (Button) findViewById(R.id.price_level_0);
        price_level_1 = (Button) findViewById(R.id.price_level_1);
        price_level_2 = (Button) findViewById(R.id.price_level_2);
        price_level_3 = (Button) findViewById(R.id.price_level_3);
        price_level_4 = (Button) findViewById(R.id.price_level_4);

        price_level_0.setPressed(true);
        price_level_0.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.darklimegreen));

        price_level_0.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                price_level_0.setPressed(true);
                price_level = 0;
                price_level_0.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.darklimegreen));

                price_level_1.setPressed(false);
                price_level_1.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.quantum_grey));

                price_level_2.setPressed(false);
                price_level_2.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.quantum_grey));

                price_level_3.setPressed(false);
                price_level_3.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.quantum_grey));

                price_level_4.setPressed(false);
                price_level_4.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.quantum_grey));
                return true;
            }
        });

        price_level_1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                price_level_1.setPressed(true);
                price_level = 1;
                price_level_1.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.darklimegreen));

                price_level_2.setPressed(false);
                price_level_2.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.quantum_grey));

                price_level_3.setPressed(false);
                price_level_3.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.quantum_grey));

                price_level_4.setPressed(false);
                price_level_4.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.quantum_grey));

                price_level_0.setPressed(false);
                price_level_0.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.quantum_grey));

                return true;
            }
        });

        price_level_2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                price_level_2.setPressed(true);
                price_level = 2;
                price_level_2.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.darklimegreen));

                price_level_1.setPressed(false);
                price_level_1.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.quantum_grey));

                price_level_3.setPressed(false);
                price_level_3.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.quantum_grey));

                price_level_4.setPressed(false);
                price_level_4.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.quantum_grey));

                price_level_0.setPressed(false);
                price_level_0.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.quantum_grey));
                return true;
            }
        });

        price_level_3.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                price_level_3.setPressed(true);
                price_level = 3;
                price_level_3.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.darklimegreen));

                price_level_1.setPressed(false);
                price_level_1.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.quantum_grey));

                price_level_2.setPressed(false);
                price_level_2.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.quantum_grey));

                price_level_4.setPressed(false);
                price_level_4.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.quantum_grey));

                price_level_0.setPressed(false);
                price_level_0.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.quantum_grey));
                return true;
            }
        });

        price_level_4.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                price_level_4.setPressed(true);
                price_level = 4;
                price_level_4.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.darklimegreen));

                price_level_1.setPressed(false);
                price_level_1.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.quantum_grey));

                price_level_2.setPressed(false);
                price_level_2.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.quantum_grey));

                price_level_3.setPressed(false);
                price_level_3.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.quantum_grey));

                price_level_0.setPressed(false);
                price_level_0.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.quantum_grey));
                return true;
            }
        });

    }

    public void updatePolyMap() {
        if (bestResults.size() == 0) {
            polyline.remove();
        } else {
            testPath = new Path(bestResults, this);
            testPath.getPolyline(currPlace, new PolyCallback() {
                @Override
                public void onSuccess() {
                    if (polyline != null) {
                        polyline.remove();
                    }
                    polyline = map.addPolyline(new PolylineOptions().addAll(testPath.getDecoded_poly()).width(17).color(Color.DKGRAY));
                }
            });
        }
    }

    public void uwu2Test(View v) {
        polyline = map.addPolyline(new PolylineOptions().addAll(testPath.getDecoded_poly()));
    }


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
                if (currPlace != null) {
                    openErrandDialog();
                } else {
                    Toast.makeText(MainActivity.this, "You must select your start location", Toast.LENGTH_LONG).show();
                }

            }
        });

        errandArray = new ArrayList<ErrandResults>();
        errandArrayAdapter = new ErrandAdapter(MainActivity.this, R.layout.errand_item, errandArray);
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
        mGetUrlContent.getDataVolley("GETCALL", url, keywords);
    }

    @Override
    public void addErrand(String errand) {
        if (errandArray.size() < 9 && currPlace != null) { // max errands is 9
            getErrandSearchPlaceResults(currPlace, errand, 1);
        }
    }

    public void removeErrand(int index) {
        //remove from errandArray
        //notify array adapter change
        //remove from bestresults
        bestResults.remove(index);
        markers.get(index).remove();
        markers.remove(index);
        Log.d("best result size: ", "best results size: " + bestResults.size());

    }


    public void openErrandDialog() {
        AddErrandDialog errandDialog = new AddErrandDialog();
        errandDialog.show(getSupportFragmentManager(), "errand dialog");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(0, 0)));
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
                if (map != null) {
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(10).build();
                    map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    if (checkPermission()) {
                        map.setMyLocationEnabled(true);
                    }


                    if (currMarker == null) {
                        currMarker = map.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title("Current Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
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
                currPlace = null;
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