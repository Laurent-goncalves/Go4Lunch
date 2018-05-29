package com.g.laurent.go4lunch.Controllers.Fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.g.laurent.go4lunch.Models.List_Search_Nearby;
import com.g.laurent.go4lunch.Models.Place_Nearby;
import com.g.laurent.go4lunch.Models.Workmate;
import com.g.laurent.go4lunch.R;
import com.g.laurent.go4lunch.Utils.Firebase_recover;
import com.g.laurent.go4lunch.Views.Resto_List.ListViewAdapter;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class ListRestoFragment extends BaseRestoFragment implements ListViewAdapter.Listener {

    @BindView(R.id.list_view_resto) RecyclerView recyclerView;
    @BindView(R.id.sort_by_criteria) TextView title_sort;
    @BindView(R.id.sort_by_number_workmates) Button button_workmates;
    @BindView(R.id.sort_by_number_stars) Button button_stars;
    @BindView(R.id.sort_by_distance) Button button_distance;
    private LatLng current_location;
    private final static String EXTRA_LAT_CURRENT = "latitude_current_location";
    private final static String EXTRA_LONG_CURRENT = "longitude_current_location";
    private static final String EXTRA_PREFERENCES = "preferences";
    private static final String EXTRA_PREF_LANG = "language_preferences";
    private static final String EXTRA_PREF_RADIUS = "radius_preferences";
    private static final String EXTRA_PREF_TYPE_PLACE = "type_place_preferences";
    private String placeId;
    private Firebase_recover firebase_recover;
    private Context context;

    public ListRestoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_list_resto, container, false);
        ButterKnife.bind(this,view);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(EXTRA_PREFERENCES, MODE_PRIVATE);
        context = getActivity().getApplicationContext();

        // Recover list of restaurants on firebase
        firebase_recover = new Firebase_recover(context,this);

        // Recover list of restos nearby
        current_location =new LatLng(48.866667,2.333333);
        String radius = sharedPreferences.getString(EXTRA_PREF_RADIUS,"500");
        String type = sharedPreferences.getString(EXTRA_PREF_TYPE_PLACE,"restaurant");
        String api_key = getResources().getString(R.string.google_maps_key);

        System.out.println("eee api_key=" + api_key);

        new List_Search_Nearby(api_key, current_location,radius,type,this);

        create_onclicklistener_for_sorting_buttons();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void configure_recycler_view(){

        if(context!=null) {
            // Create adapter passing in the sample user data
            ListViewAdapter adapter = new ListViewAdapter(context, list_places_nearby, list_workmates, current_location, this);
            // Attach the adapter to the recyclerview to populate items
            recyclerView.setAdapter(adapter);
            // Set layout manager to position the items
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        }
    }

    private void getLatLng_current_location(){
        // recover the latitude and longitude inside the bundle
        current_location = new LatLng(getArguments().getDouble(EXTRA_LAT_CURRENT,0),
                                          getArguments().getDouble(EXTRA_LONG_CURRENT,0));
    }

    @Override
    public void onClickShowRestoDetails(String placeId) {
        mCallback_detailResto.configure_and_show_restofragment(placeId);
    }

    private void create_onclicklistener_for_sorting_buttons(){

        button_workmates.setOnClickListener(v -> {
            sort_list_places_nearby("workmates");
            change_color_button_if_selected(button_workmates);
            configure_recycler_view();
        });

        button_stars.setOnClickListener(v -> {
            sort_list_places_nearby("stars");
            change_color_button_if_selected(button_stars);
            configure_recycler_view();
        });

        button_distance.setOnClickListener(v -> {
            sort_list_places_nearby("distance");
            change_color_button_if_selected(button_distance);
            configure_recycler_view();
        });
    }

    private void change_color_button_if_selected(Button button_selected){

        if(button_selected.getTag().equals(button_workmates.getTag())){
            button_workmates.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            button_stars.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            button_distance.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        } else if(button_selected.getTag().equals(button_stars.getTag())) {
            button_workmates.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            button_stars.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            button_distance.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        } else if(button_selected.getTag().equals(button_distance.getTag())) {
            button_workmates.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            button_stars.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            button_distance.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        }
    }

    public void sort_list_places_nearby(String type_sorting) {

        List<Place_Nearby> new_list_place_nearby= new ArrayList<>();
        List<Double> list_to_sort_dbl = new ArrayList<>();
        List<Integer> list_to_sort_int = new ArrayList<>();
        List<Integer> list_index = new ArrayList<>();

        if(list_places_nearby!=null){

            if(!type_sorting.equals("workmates")) {
                list_to_sort_dbl=create_list_to_sort(list_places_nearby, type_sorting,current_location);
                list_index.addAll(set_list_sorted_dbl(list_to_sort_dbl,type_sorting));
                new_list_place_nearby=create_list_place_nearby_sorted(list_index);
                list_places_nearby.clear();
                list_places_nearby.addAll(new_list_place_nearby);

            } else
                sort_list_places_nearby_by_workmates();
        }
    }

    public void setCurrent_location(LatLng current_location) {
        this.current_location = current_location;
    }

    public void recover_list_workmates(List<Place_Nearby> list_resto) {
        this.list_places_nearby = list_resto;
        firebase_recover = new Firebase_recover(context,this);
        firebase_recover.recover_list_workmates();
    }

    public void set_list_of_workmates(List<Workmate> list_workmates){
        this.list_workmates=list_workmates;
        configure_recycler_view();
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    // ------------------------ UNUSED METHODS -----------------------------------
}
