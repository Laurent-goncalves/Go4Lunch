package com.g.laurent.go4lunch.Controllers.Activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.design.widget.NavigationView;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import com.g.laurent.go4lunch.Models.AlarmReceiver;
import com.g.laurent.go4lunch.Models.CallbackMultiActivity;
import com.g.laurent.go4lunch.Models.List_Search_Nearby;
import com.g.laurent.go4lunch.Models.Place_Nearby;
import com.g.laurent.go4lunch.R;
import com.g.laurent.go4lunch.Utils.Firebase_update;
import com.g.laurent.go4lunch.Utils.Google_Maps_Utils;
import com.g.laurent.go4lunch.Utils.Toolbar_navig_Utils;
import com.g.laurent.go4lunch.Views.MultiFragAdapter;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MultiActivity extends AppCompatActivity implements CallbackMultiActivity, SwipeRefreshLayout.OnRefreshListener {

    private static final String EXTRA_PREF_RADIUS = "radius_preferences";
    private static final String EXTRA_USER_ID = "user_id_alarm";
    private static final String EXTRA_PREFERENCES = "preferences";
    private static final String EXTRA_PREF_TYPE_PLACE = "type_place_preferences";
    private static final String EXTRA_ENABLE_NOTIF = "enable_notif";
    private static final String EXTRA_PREF_LANG = "language_preferences";
    @BindView(R.id.activity_main_drawer_layout) DrawerLayout drawerLayout;
    @BindView(R.id.activity_main_nav_view) NavigationView navigationView;
    @BindView(R.id.swiperefresh) SwipeRefreshLayout swipeRefreshLayout;
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    private TabLayout tabs;
    private MultiFragAdapter pageAdapter;
    private ViewPager pager;
    private int current_page;
    private SharedPreferences sharedPreferences;
    private String api_key;
    private LatLng currentPlaceLatLng;
    private FirebaseUser mCurrentUser;
    private Toolbar_navig_Utils toolbar_navig_utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Context context = getApplicationContext();
        ButterKnife.bind(this);

        // Assign and initialize variables
        FirebaseApp.initializeApp(context);
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mCurrentUser=null;

        if(mCurrentUser!=null){
            sharedPreferences = getSharedPreferences(EXTRA_PREFERENCES, MODE_PRIVATE);
            current_page = 0;

            setLanguageForApp();

            // Recover current location
            Google_Maps_Utils google_maps_utils = new Google_Maps_Utils(getApplicationContext(), this, null);
            google_maps_utils.getLocationPermission();
        }
    }


    private void setLanguageForApp(){

        String lang = sharedPreferences.getString(EXTRA_PREF_LANG,"en");

        Locale locale = new Locale(lang);

        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        configuration.setLocale(locale);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            getApplicationContext().createConfigurationContext(configuration);
        } else {
            resources.updateConfiguration(configuration,displayMetrics);
        }
        getApplicationContext().getResources().updateConfiguration(configuration, getApplicationContext().getResources().getDisplayMetrics());
    }

    @Override
    public void configureViewPagerAndTabs(List<Place_Nearby> list_restos) {

        // Get ViewPager from layout
        pager = findViewById(R.id.viewpager);

        if(getApplicationContext()!=null && list_restos!=null)
            pageAdapter = new MultiFragAdapter(getSupportFragmentManager(), getApplicationContext(), list_restos, currentPlaceLatLng);

        runOnUiThread(() -> {
            pager.setAdapter(pageAdapter);

            // Get TabLayout from layout
            tabs = findViewById(R.id.activity_multi_tabs);
            tabs.setupWithViewPager(pager);

            // Glue TabLayout and ViewPager together
            configure_tabs();

            // Design purpose. Tabs have the same width
            tabs.setTabMode(TabLayout.MODE_FIXED);

            // Select the last tab selected before eventual refresh and stop the refresh
            Objects.requireNonNull(tabs.getTabAt(current_page)).select();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void configure_tabs() {

        // Initialize tabs
        Objects.requireNonNull(tabs.getTabAt(0)).setIcon(R.drawable.baseline_map_white_24);
        Objects.requireNonNull(Objects.requireNonNull(tabs.getTabAt(0)).getIcon()).setColorFilter(getResources().getColor(R.color.colorIconSelected), PorterDuff.Mode.SRC_IN);
        Objects.requireNonNull(tabs.getTabAt(1)).setIcon(R.drawable.baseline_view_list_white_24);
        Objects.requireNonNull(Objects.requireNonNull(tabs.getTabAt(1)).getIcon()).setColorFilter(getResources().getColor(R.color.colorIconNotSelected), PorterDuff.Mode.SRC_IN);
        Objects.requireNonNull(tabs.getTabAt(2)).setIcon(R.drawable.baseline_people_white_24);
        Objects.requireNonNull(Objects.requireNonNull(tabs.getTabAt(2)).getIcon()).setColorFilter(getResources().getColor(R.color.colorIconNotSelected), PorterDuff.Mode.SRC_IN);

        swipeRefreshLayout.setEnabled(false);

        // Set on Tab selected listener
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Change color of the tab -> orange
                if (tab.getIcon() != null)
                    tab.getIcon().setColorFilter(getResources().getColor(R.color.colorIconSelected), PorterDuff.Mode.SRC_IN);

                current_page = tab.getPosition();

                // Change tab title if required

                if(toolbar_navig_utils!=null)
                    toolbar_navig_utils.refresh_text_toolbar();

                // Disable pull to refresh when mapView is displayed
                if(tab.getPosition()==0)
                    swipeRefreshLayout.setEnabled(false);
                else
                    swipeRefreshLayout.setEnabled(true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

                // if the searchView is opened, close it
                if(toolbar_navig_utils!=null) {
                    if (toolbar_navig_utils.getSearchView() != null) {
                        if (!toolbar_navig_utils.getSearchView().isIconified()) {
                            toolbar_navig_utils.getSearchView().setIconified(true);

                            // Recover the previous list of places nearby generated
                            switch (current_page) {
                                case 0:
                                    getPageAdapter().getMapsFragment().recover_previous_state();
                                    break;
                                case 1:
                                    getPageAdapter().getListRestoFragment().recover_previous_state();
                                    break;
                            }
                        }
                    }
                }

                // Change color of the tab -> black
                if (tab.getIcon() != null)
                    tab.getIcon().setColorFilter(getResources().getColor(R.color.colorIconNotSelected), PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    // ---------------------------------------------------------------------------------
    // ------------------------ CREATE NEW LIST OF RESTAURANTS -------------------------
    // ---------------------------------------------------------------------------------

    public void configureAlarmManager() {

        Boolean enable = sharedPreferences.getBoolean(EXTRA_ENABLE_NOTIF,false);

        if(enable){
            alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
            intent.putExtra(EXTRA_USER_ID, mCurrentUser.getUid());
            alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            // Set the alarm to start at 12:00 p.m.
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 12);
            calendar.set(Calendar.MINUTE, 0);

            // setRepeating() lets you specify a precise custom interval
            if (alarmMgr != null)
                alarmMgr.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY, alarmIntent);
        }
    }

    // ---------------------------------------------------------------------------------
    // -------------------         CONFIGURATION OF SETTINGS         -------------------
    // ---------------------------------------------------------------------------------

    public void configure_and_show_settings_activity() {

        Intent intent = new Intent(this, SettingActivity.class);
        int requestCode = 0;
        if (sharedPreferences != null) {
            if (sharedPreferences.getBoolean(EXTRA_ENABLE_NOTIF, false)) {
                requestCode = 1;
            } else {
                requestCode = 0;
            }
        }
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // request code 1 = enable notif ; request code 0 = disable notif
        if (requestCode == 1) {
            if (resultCode == RESULT_CANCELED) { // if the user decided to disable notification, whereas it was enabled when opening settings
                if (alarmMgr != null) {
                    alarmMgr.cancel(alarmIntent);
                }
            }
        } else {
            if (resultCode == RESULT_FIRST_USER) { // if the user has enabled notif, whereas it was disabled when opening settings
                configureAlarmManager();
            }
        }
        // recreate activity to refresh texts (useful in case of change of language)
        recreate();
    }

    // ----------------------------------------------------------------------------------------------------
    // -------------------------------------- GETTER and SETTER -------------------------------------------
    // ----------------------------------------------------------------------------------------------------

    public void setCurrentPlaceLatLng(LatLng currentPlaceLatLng) {
        this.currentPlaceLatLng = currentPlaceLatLng;

        // Configure toolbar and navigation drawer
        toolbar_navig_utils = new Toolbar_navig_Utils(this);
        toolbar_navig_utils.configure_toolbar();
        toolbar_navig_utils.configureNavigationView();

        api_key = getResources().getString(R.string.google_maps_key2);

        Firebase_update firebase_update = new Firebase_update(getApplicationContext());

        if (mCurrentUser != null)
            firebase_update.create_new_user_firebase(mCurrentUser);

        // this.configureAlarmManager();

        tabs = findViewById(R.id.activity_multi_tabs);

        String radius = String.valueOf(sharedPreferences.getInt(EXTRA_PREF_RADIUS,500));
        String type = sharedPreferences.getString(EXTRA_PREF_TYPE_PLACE,"restaurant");

        new List_Search_Nearby(api_key, this.currentPlaceLatLng, radius, type, this);

        swipeRefreshLayout.setOnRefreshListener(this);
    }

    public void setToolbar() {
        toolbar_navig_utils = new Toolbar_navig_Utils(this);
        toolbar_navig_utils.configureNavigationView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public MultiFragAdapter getPageAdapter() {
        return pageAdapter;
    }

    public void setPageAdapter(MultiFragAdapter pageAdapter) {
        this.pageAdapter = pageAdapter;
    }

    public int getCurrentPage(){
        return current_page;
    }

    public String get_API_KEY(){
        return api_key;
    }

    public DrawerLayout getDrawerLayout() {
        return drawerLayout;
    }

    public NavigationView getNavigationView() {
        return navigationView;
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public FirebaseUser getCurrentUser() {
        return mCurrentUser;
    }

    public LatLng getCurrentPlaceLatLng() {
        return currentPlaceLatLng;
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }

    public TabLayout getTabs() {
        return tabs;
    }

    @Override
    public void onRefresh() {
        Google_Maps_Utils google_maps_utils = new Google_Maps_Utils(getApplicationContext(), this, null);
        google_maps_utils.getLocationPermission();
    }
}
