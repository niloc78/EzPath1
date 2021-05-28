package com.example.ezpath;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.ViewAnimator;
import android.widget.ViewFlipper;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    ViewAnimator viewAnimator_dash_home;
    ViewAnimator viewAnimator_home_notes_map;
    BottomNavigationView bottom_nav_view;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewAnimator_dash_home = (ViewAnimator) findViewById(R.id.view_animator);

        viewAnimator_home_notes_map = (ViewAnimator) findViewById(R.id.view_animator_home_map_notes);
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



//        location_search_bar = (SearchView) findViewById(R.id.location_search_bar);
//        location_search_bar.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                location_search_bar.setIconified(false);
//            }
//        });
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
}