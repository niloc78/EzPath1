package com.example.ezpath;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Toast;
import android.widget.ViewAnimator;
import android.widget.ViewFlipper;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    ViewAnimator viewAnimator_dash_home;
    ViewAnimator viewAnimator_home_notes_map;
    BottomNavigationView bottom_nav_view;
    private static final int PERMISSION_REQUEST_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewAnimator_dash_home = (ViewAnimator) findViewById(R.id.view_animator);

        viewAnimator_home_notes_map = (ViewAnimator) findViewById(R.id.view_animator_home_map_notes);

        botNavSetUp();

        checkRequestPermissions();

        initializePlaces();
        initPlaceSelectionListener();

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
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));
        //handle response
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Toast.makeText(MainActivity.this, "place selected", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
            }
        });
        ((View) findViewById(R.id.places_autocomplete_search_button)).setVisibility(View.GONE);
        ((EditText) findViewById(R.id.places_autocomplete_search_input)).setHint("Enter your location");
    }

    public void initializePlaces() {
        String apiKey = getString(R.string.api_key);
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

    public void nextView(View v) {
        viewAnimator_dash_home.setInAnimation(this, R.anim.slide_in_right);
        viewAnimator_dash_home.setOutAnimation(this, R.anim.slide_out_left);
        viewAnimator_dash_home.showNext();
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
            navigateToDash();
        }

    }
}