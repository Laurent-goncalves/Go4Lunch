package com.g.laurent.go4lunch;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.g.laurent.go4lunch.Models.Callback_DetailResto;
import com.g.laurent.go4lunch.Models.Place_Nearby;
import com.g.laurent.go4lunch.Models.Workmates;
import com.g.laurent.go4lunch.Utils.Firebase_recover;
import com.g.laurent.go4lunch.Utils.Firebase_update;
import com.g.laurent.go4lunch.Views.Resto_List.ListViewAdapter;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;

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
    private Firebase_recover firebase_recover;
    private FirebaseUser currentUser;

    public ListRestoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_list_resto, container, false);
        ButterKnife.bind(this,view);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Recover list of restaurants on firebase
        firebase_recover = new Firebase_recover(getActivity().getApplicationContext(),this,null,null,null);

        if(currentUser!=null)
            firebase_recover.recover_list_restos(currentUser.getUid(),null);

        create_onclicklistener_for_sorting_buttons();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(currentUser!=null)
            firebase_recover.recover_list_restos(currentUser.getUid(),null);
    }

    private void configure_recycler_view(){

    if(current_location==null)
        getLatLng_current_location();

        if(getActivity()!=null) {
            // Create adapter passing in the sample user data
            ListViewAdapter adapter = new ListViewAdapter(getActivity().getApplicationContext(), list_places_nearby, current_location, this);
            // Attach the adapter to the recyclerview to populate items
            recyclerView.setAdapter(adapter);
            // Set layout manager to position the items
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        }
    }

    private void getLatLng_current_location(){

        // recover the latitude and longitude inside the bundle
        if(getArguments()!=null)
            current_location = new LatLng(getArguments().getDouble(EXTRA_LAT_CURRENT,0),
                                          getArguments().getDouble(EXTRA_LONG_CURRENT,0));
        else
            current_location=null;
    }

    @Override
    public void onClickShowRestoDetails(String placeId) {
        mCallback_detailResto.configure_and_show_restofragment(placeId);
    }

    private void create_onclicklistener_for_sorting_buttons(){

        button_workmates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sort_list_places_nearby("workmates");
                change_color_button_if_selected(button_workmates);
                configure_recycler_view();
            }
        });

        button_stars.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sort_list_places_nearby("stars");
                change_color_button_if_selected(button_stars);
                configure_recycler_view();
            }
        });

        button_distance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sort_list_places_nearby("distance");
                change_color_button_if_selected(button_distance);
                configure_recycler_view();
            }
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
            } else {
                list_to_sort_int=create_list_to_sort(list_places_nearby);
                list_index.addAll(set_list_sorted_int(list_to_sort_int));
            }

            new_list_place_nearby=create_list_place_nearby_sorted(list_index);
            list_places_nearby.clear();
            list_places_nearby.addAll(new_list_place_nearby);
        }
    }

    public void setCurrent_location(LatLng current_location) {
        this.current_location = current_location;
    }

    public void set_resto(List<Place_Nearby> list_restos) {
        this.list_places_nearby=list_restos;
        configure_recycler_view();
    }

    // ------------------------ UNUSED METHODS -----------------------------------

    @Override
    public void configure_and_show_restofragment(String placeId) {

    }
}
