package com.g.laurent.go4lunch.Utils;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
import com.g.laurent.go4lunch.Controllers.Activities.MultiActivity;
import com.g.laurent.go4lunch.Controllers.Activities.RestoActivity;
import com.g.laurent.go4lunch.Models.List_Search_Nearby;
import com.g.laurent.go4lunch.Models.Place_Nearby;
import com.g.laurent.go4lunch.R;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import java.util.List;


public class Toolbar_navig_Utils implements NavigationView.OnNavigationItemSelectedListener{

    private static final String EXTRA_PREF_RADIUS = "radius_preferences";
    private static final String EXTRA_RESTO_JSON = "resto_to_json";
    private final static String EXTRA_RESTO_DETAILS = "resto_details";
    private static final int SIGN_OUT_TASK = 10;
    private SearchView searchView;
    private TextView title_toolbar;
    private ImageButton hamburger;
    private Context context;
    private MultiActivity activity;
    private LatLngBounds bounds;
    private LatLng currentPlaceLatLng;
    private FirebaseUser mCurrentUser;
    private SearchView.SearchAutoComplete searchAutoComplete;
    private int radius;
    private String api_key;
    private List<Place_Nearby> list_place_nearby_autocomplete;

    public Toolbar_navig_Utils(MultiActivity activity) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.mCurrentUser = activity.getCurrentUser();
        this.currentPlaceLatLng=activity.getCurrentPlaceLatLng();
        this.api_key = activity.get_API_KEY();
    }

    // ---------------------------------------------------------------------------------
    // -----------------     CONFIGURATION OF TOOLBAR  ---------------------------------
    // ---------------------------------------------------------------------------------

    public void configure_toolbar(){

        Toolbar toolbar = activity.findViewById(R.id.activity_main_toolbar);
        activity.setSupportActionBar(toolbar);

        //Assign icons
        title_toolbar = toolbar.findViewById(R.id.title_toolbar);

        if(activity.getCurrentPage()==2)
            title_toolbar.setText(context.getResources().getString(R.string.available_workmates));
        else
            title_toolbar.setText(context.getResources().getString(R.string.toolbar_mapview));

        hamburger = toolbar.findViewById(R.id.button_hamburger);
        searchView = toolbar.findViewById(R.id.searchView);

        // configure hamburger menu to open the navigation drawer
        hamburger.setOnClickListener(v -> activity.getDrawerLayout().openDrawer(Gravity.START));
        configure_searchView();
    }

    private void create_onClickListener_Opening_searchView(){
        searchView.setOnSearchClickListener(v -> {
            title_toolbar.setVisibility(View.GONE);
            hamburger.setVisibility(View.GONE);
            searchView.setMaxWidth(Integer.MAX_VALUE);

            int radius = activity.getSharedPreferences().getInt(EXTRA_PREF_RADIUS, 500);

            // Define bounds for search
            DistanceCalculation tool_calcul_distance = new DistanceCalculation();
            bounds = tool_calcul_distance.toBounds(currentPlaceLatLng, radius);
        });
    }

    private void create_onClickListener_Closing_searchView(){
        searchView.setOnCloseListener(() -> {
            title_toolbar.setVisibility(View.VISIBLE);
            hamburger.setVisibility(View.VISIBLE);

            // Recover the previous list of places nearby generated
            switch (activity.getCurrentPage()) {
                case 0:
                    activity.getPageAdapter().getMapsFragment().recover_previous_state();
                    break;
                case 1:
                    activity.getPageAdapter().getListRestoFragment().recover_previous_state();
                    break;
            }

            return false;
        });
    }

    private void create_onClickListener_Select_Item_searchView(){
        // when clicking on an item from the list autocomplete
        searchAutoComplete.setOnItemClickListener((parent, view, position, id) -> {

            List<String> list_placesId = new ArrayList<>();
            list_placesId.add(list_place_nearby_autocomplete.get(position).getPlaceId());

            if(activity.getCurrentPage()==0) {
                new List_Search_Nearby(api_key, list_placesId, activity.get_Page_Adapter().getMapsFragment());
            } else if(activity.getCurrentPage()==1) {
                new List_Search_Nearby(api_key, list_placesId, activity.get_Page_Adapter().getListRestoFragment());
            }
        });
    }

    private void create_setOnQueryTextListener_searchView(){

        Google_Maps_Utils google_maps_utils = new Google_Maps_Utils(context,activity,this);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                radius = activity.getSharedPreferences().getInt(EXTRA_PREF_RADIUS,500);
                DistanceCalculation distanceCalculation = new DistanceCalculation();
                bounds = distanceCalculation.toBounds(currentPlaceLatLng,radius);

                switch (activity.getCurrentPage()) {
                    case 0:
                        google_maps_utils.googleplacespredictions(activity.get_API_KEY(), query, bounds, null, activity.get_Page_Adapter().getMapsFragment());
                        break;
                    case 1:
                        google_maps_utils.googleplacespredictions(activity.get_API_KEY(), query, bounds, activity.get_Page_Adapter().getListRestoFragment(), null);
                        break;
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                if(activity.getCurrentPage()==0 || activity.getCurrentPage()==1) // only for mapsFragment and listRestoFragment
                    google_maps_utils.get_list_places_prediction(s,bounds);

                return false;
            }
        });

    }

    private void configure_searchView(){

        // Add query hint in the search area
        searchView.setQueryHint(context.getResources().getString(R.string.Search_restaurants));

        // Assign searchAutoComplete
        searchAutoComplete = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);

        // change color of the text query area
        searchAutoComplete.setTextColor(context.getResources().getColor(R.color.colorIconNotSelected));

        // change color of close icon in searchview
        ImageView icon_close_search = searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        icon_close_search.setColorFilter(context.getResources().getColor(R.color.colorGrey));

        create_onClickListener_Opening_searchView();
        create_onClickListener_Closing_searchView();
        create_onClickListener_Select_Item_searchView();
        create_setOnQueryTextListener_searchView();
    }

    public void display_list_predictions(ArrayList<AutocompletePrediction> al) {

        list_place_nearby_autocomplete = new ArrayList<>();

        for(AutocompletePrediction place_predic : al){
            list_place_nearby_autocomplete.add(new Place_Nearby(place_predic.getFullText(null).toString(),place_predic.getPlaceId(),null,null,null,null,null,null,null,null,null));
        }

        String dataArr[] = new String[list_place_nearby_autocomplete.size()];
        for(int i = 0; i<list_place_nearby_autocomplete.size();i++)
            dataArr[i] = list_place_nearby_autocomplete.get(i).getName_restaurant();

        ArrayAdapter<String> autocomplete_adapter = new ArrayAdapter<>(activity, android.R.layout.simple_dropdown_item_1line, dataArr);
        searchAutoComplete.setAdapter(autocomplete_adapter);

    }

    // ---------------------------------------------------------------------------------
    // -----------------     CONFIGURATION OF DRAWER  ----------------------------------
    // ---------------------------------------------------------------------------------

    public void configureNavigationView() {

        activity.getNavigationView().setNavigationItemSelectedListener(this);

        configure_title_menu_navigDrawer();

        if (mCurrentUser != null) {

            ImageView picture_user = activity.getNavigationView().getHeaderView(0).findViewById(R.id.current_user_image_drawer);
            TextView name_user = activity.getNavigationView().getHeaderView(0).findViewById(R.id.current_user_name_drawer);
            TextView email_user = activity.getNavigationView().getHeaderView(0).findViewById(R.id.current_user_email_drawer);

            name_user.setText(mCurrentUser.getDisplayName());
            email_user.setText(mCurrentUser.getEmail());

            if (mCurrentUser.getPhotoUrl() != null)
                Glide.with(context)
                        .load(mCurrentUser.getPhotoUrl().toString())
                        .apply(RequestOptions.circleCropTransform())
                        .into(picture_user);

        }
    }

    private void configure_title_menu_navigDrawer(){

        MenuItem menuYourLunch = activity.getNavigationView().getMenu().findItem(R.id.activity_main_drawer_your_lunch);
        menuYourLunch.setTitle(context.getResources().getString(R.string.your_lunch));

        MenuItem menuSettings = activity.getNavigationView().getMenu().findItem(R.id.activity_main_drawer_settings);
        menuSettings.setTitle(context.getResources().getString(R.string.settings));

        MenuItem menuLogOut = activity.getNavigationView().getMenu().findItem(R.id.activity_main_drawer_logout);
        menuLogOut.setTitle(context.getResources().getString(R.string.logout));
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        switch (id) {
            case R.id.activity_main_drawer_your_lunch:

                String resto_json = activity.getSharedPreferences().getString(EXTRA_RESTO_JSON, null);

                if(resto_json!=null){
                    Intent intent = new Intent(context, RestoActivity.class);
                    intent.putExtra(EXTRA_RESTO_DETAILS, resto_json);
                    activity.startActivity(intent);
                } else {
                    Toast toast = Toast.makeText(context,context.getResources().getString(R.string.no_resto_chosen),Toast.LENGTH_LONG);
                    toast.show();
                }
                break;
            case R.id.activity_main_drawer_settings:
                activity.configure_and_show_settings_activity();
                break;
            case R.id.activity_main_drawer_logout:
                signOutUserFromFirebase();
                break;
            default:
                break;
        }
        activity.getDrawerLayout().closeDrawer(GravityCompat.START);
        return true;
    }

    private void signOutUserFromFirebase() {
        AuthUI.getInstance()
                .signOut(context)
                .addOnSuccessListener(activity, updateUIAfterRESTRequestsCompleted(SIGN_OUT_TASK));
    }

    private OnSuccessListener<Void> updateUIAfterRESTRequestsCompleted(final int origin) {
        return aVoid -> {
            switch (origin) {
                case SIGN_OUT_TASK:
                    activity.finish();
                    break;
                default:
                    break;
            }
        };
    }

    public SearchView getSearchView() {
        return searchView;
    }

    public void refresh_text_toolbar() {

        if(activity.getCurrentPage()==2)
            title_toolbar.setText(context.getResources().getString(R.string.available_workmates));
        else
            title_toolbar.setText(context.getResources().getString(R.string.toolbar_mapview));

        searchView.setQueryHint(context.getResources().getString(R.string.Search_restaurants));
    }
}


/*

                System.out.println("eee  listRestoFragment ");

                if(activity.get_Page_Adapter().getListRestoFragment()!=null)
                    System.out.println("eee  getListRestoFragment non null ");
                else
                    System.out.println("eee  getListRestoFragment NULL ");

 */