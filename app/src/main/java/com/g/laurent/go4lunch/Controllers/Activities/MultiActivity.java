package com.g.laurent.go4lunch.Controllers.Activities;

import android.app.AlarmManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.FragmentTransaction;
import android.app.PendingIntent;
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
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.g.laurent.go4lunch.Controllers.Fragments.ListRestoFragment;
import com.g.laurent.go4lunch.Models.AlarmReceiver;
import com.g.laurent.go4lunch.Models.CallbackMultiActivity;
import com.g.laurent.go4lunch.Models.ListSearchNearby;
import com.g.laurent.go4lunch.Models.PlaceNearby;
import com.g.laurent.go4lunch.R;
import com.g.laurent.go4lunch.Utils.FirebaseUpdate;
import com.g.laurent.go4lunch.Utils.GoogleMapsUtils;
import com.g.laurent.go4lunch.Utils.ToolbarNavigUtils;
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
    @BindView(R.id.progressBar) ProgressBar mProgressBar;
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
    private ToolbarNavigUtils mToolbar_navig_utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Context context = getApplicationContext();
        ButterKnife.bind(this);

        // Assign and initialize variables
        FirebaseApp.initializeApp(context);
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        api_key = context.getResources().getString(R.string.google_maps_key2);
        sharedPreferences = getSharedPreferences(EXTRA_PREFERENCES, MODE_PRIVATE);
        swipeRefreshLayout.setOnRefreshListener(this);
        current_page = 0;

        if(mCurrentUser!=null){

            // Set the language of the app by getting the settings in sharedpreferrences
            setLanguageForApp();

            // 1 - Recover current location
            GoogleMapsUtils google_maps_utils = new GoogleMapsUtils(getApplicationContext(), this, null);
            google_maps_utils.getLocationPermission();
        }
    }

    public void setCurrentPlaceLatLng(LatLng currentPlaceLatLng) {

        this.currentPlaceLatLng = currentPlaceLatLng;

        // Start progressBar
        if(mProgressBar!=null)
            mProgressBar.setVisibility(View.VISIBLE);

        // 2 - Once the location is defined, configure the toolbar and the navigation drawer
        mToolbar_navig_utils = new ToolbarNavigUtils(this);
        mToolbar_navig_utils.configure_toolbar();
        mToolbar_navig_utils.configureNavigationView();

        // 3 - Create new user on Firebase by adding name, id and picture URL (or re-write it)
        if (mCurrentUser != null) {
            FirebaseUpdate firebase_update = new FirebaseUpdate(getApplicationContext());
            firebase_update.create_new_user_firebase(mCurrentUser);
        }

        // 4 - Launch a new API request to get new list of places nearby based on settings in sharedpreferences
        String radius = String.valueOf(sharedPreferences.getInt(EXTRA_PREF_RADIUS,500));
        String type = sharedPreferences.getString(EXTRA_PREF_TYPE_PLACE,"restaurant");

        new ListSearchNearby(getApplicationContext(), api_key, this.currentPlaceLatLng, radius, type, this);
    }

    public void configureViewPagerAndTabs(List<PlaceNearby> list_restos) {

        // 5 - Get ViewPager from layout, set the limit of pages to refresh to 3
        pager = findViewById(R.id.viewpager);
        pager.setOffscreenPageLimit(3);

        runOnUiThread(() -> {

            // 6 - Create the Adapter with the 3 fragments and add it to the viewpager
            if(getApplicationContext()!=null && list_restos!=null){
                pageAdapter = new MultiFragAdapter(getSupportFragmentManager(), getApplicationContext(), list_restos, currentPlaceLatLng);
                pager.setAdapter(pageAdapter);
            }

            // 7 - Get TabLayout from layout and configure it
            tabs = findViewById(R.id.activity_multi_tabs);
            tabs.setupWithViewPager(pager);
            tabs.setTabMode(TabLayout.MODE_FIXED); // Tabs have the same width
            configure_tabs();

            // 8 - Select the last tab selected before eventual refresh and stop the refresh and the progressBar
            Objects.requireNonNull(tabs.getTabAt(current_page)).select();
            swipeRefreshLayout.setRefreshing(false);
            mProgressBar.setVisibility(View.GONE);
        });
    }

    private void configure_tabs() {

        // Initialize tabs (icon and tab color)
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
                    tab.getIcon().setColorFilter(getApplicationContext().getResources().getColor(R.color.colorIconSelected), PorterDuff.Mode.SRC_IN);

                // set the current page position
                current_page = tab.getPosition();

                // if the current page is the ListMatesFragment, remove the searchView
                if(current_page==2)
                    mToolbar_navig_utils.getSearchView().setVisibility(View.GONE);
                else
                    mToolbar_navig_utils.getSearchView().setVisibility(View.VISIBLE);

                // refresh title toolbar (different according to the page selected)
                if(mToolbar_navig_utils !=null)
                    mToolbar_navig_utils.refresh_text_toolbar();

                // Disable pull to refresh when mapView is displayed
                if(tab.getPosition()==0)
                    swipeRefreshLayout.setEnabled(false);
                else
                    swipeRefreshLayout.setEnabled(true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

                // if the searchView is opened, close it
                if(mToolbar_navig_utils !=null) {
                    if (mToolbar_navig_utils.getSearchView() != null) {
                        if (!mToolbar_navig_utils.getSearchView().isIconified()) {
                            mToolbar_navig_utils.getSearchView().setIconified(true);

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
                    tab.getIcon().setColorFilter(getApplicationContext().getResources().getColor(R.color.colorIconNotSelected), PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void setLanguageForApp(){

        // Recover the language selected by the user
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

    public void message_error_API_request(String error){

        Toast toast = Toast.makeText(getApplicationContext(),getApplicationContext().getResources().getString(R.string.error_get_list_restos) +"\n"
                + error,Toast.LENGTH_LONG);
        toast.show();
        mProgressBar.setVisibility(View.GONE);
    }

    // ---------------------------------------------------------------------------------
    // -------------------         CONFIGURATION OF SETTINGS         -------------------
    // ---------------------------------------------------------------------------------

    public void configure_and_show_settings_activity() {

        Intent intent = new Intent(this, SettingActivity.class);
        int requestCode = 0;
        if (sharedPreferences != null) {
            if (sharedPreferences.getBoolean(EXTRA_ENABLE_NOTIF, false)) {
                requestCode = 1; // if the user has enabled notifications, requestCode is 1
            } else {
                requestCode = 0; // if the user hasn't enabled notifications, requestCode is 0
            }
        }
        startActivityForResult(intent, requestCode); // launch settingActivity
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

        if(mProgressBar!=null)
            mProgressBar.setVisibility(View.VISIBLE);

        // recreate activity to refresh texts (useful in case of change of language)
        GoogleMapsUtils google_maps_utils = new GoogleMapsUtils(getApplicationContext(), this, null);
        google_maps_utils.getLocationPermission();
    }

    public void configureAlarmManager() {

        // Recover in the sharedpreferences if the user has enabled or not notifications
        Boolean enable = sharedPreferences.getBoolean(EXTRA_ENABLE_NOTIF,false);

        if(enable){
            alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
            intent.putExtra(EXTRA_USER_ID, mCurrentUser.getUid()); // attach the userId to the intent
            alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            // Set the alarm to start at 12:00.
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

    @Override
    public void onRefresh() {

        if(isNetworkAvailable(getApplicationContext())){ // check if there is internet connection
            // When refreshing, we launch a new API request starting by defining current location
            GoogleMapsUtils google_maps_utils = new GoogleMapsUtils(getApplicationContext(), this, mToolbar_navig_utils);
            google_maps_utils.getLocationPermission();

            // Launch progressBar
            if(mProgressBar!=null)
                mProgressBar.setVisibility(View.VISIBLE);

        } else {
            swipeRefreshLayout.setRefreshing(false);
            Toast toast = Toast.makeText(getApplicationContext(),getApplicationContext().getResources().getString(R.string.error_no_internet),Toast.LENGTH_LONG);
            toast.show();
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        if (context == null) {
            return false;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // ----------------------------------------------------------------------------------------------------
    // -------------------------------------- GETTER and SETTER -------------------------------------------
    // ----------------------------------------------------------------------------------------------------

    public ListRestoFragment listRestoFragment;

    public void configure_and_show_listrestofragment() {
        listRestoFragment = new ListRestoFragment();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.multi_activity_fragment, listRestoFragment);
        fragmentTransaction.commit();
    }

    public MultiFragAdapter getPageAdapter() {
        return pageAdapter;
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

    public ProgressBar getProgressBar() {
        return mProgressBar;
    }

    public TabLayout getTabs() {
        return tabs;
    }
}
