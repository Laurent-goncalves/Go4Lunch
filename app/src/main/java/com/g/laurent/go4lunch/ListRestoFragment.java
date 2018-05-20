package com.g.laurent.go4lunch;

import android.app.Activity;
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
import com.g.laurent.go4lunch.Utils.Search_Nearby.Geometry;
import com.g.laurent.go4lunch.Utils.Search_Nearby.Location;
import com.g.laurent.go4lunch.Utils.Search_Nearby.OpeningHours;
import com.g.laurent.go4lunch.Views.Resto_List.ListViewAdapter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static java.lang.Double.compare;

/**
 * A simple {@link Fragment} subclass.
 */
public class ListRestoFragment extends BaseRestoFragment implements ListViewAdapter.Listener {

    @BindView(R.id.list_view_resto) RecyclerView recyclerView;
    @BindView(R.id.sort_by_criteria) TextView title_sort;
    @BindView(R.id.sort_by_number_workmates) Button button_workmates;
    @BindView(R.id.sort_by_number_stars) Button button_stars;
    @BindView(R.id.sort_by_distance) Button button_distance;
    private ListViewAdapter adapter;
    private LatLng current_location;
    private final static String EXTRA_LAT_CURRENT = "latitude_current_location";
    private final static String EXTRA_LONG_CURRENT = "longitude_current_location";
    private Callback_DetailResto mCallback_detailResto;

    public ListRestoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_list_resto, container, false);
        ButterKnife.bind(this,view);
        recover_list_resto_firebase();
        create_onclicklistener_for_sorting_buttons();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        recover_list_resto_firebase();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback_detailResto = (Callback_DetailResto) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement Callback_DetailResto");
        }
    }

    public void recover_list_resto_firebase() {

        list_places_nearby = new ArrayList<>();
        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference().child("restaurants");
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot!=null) {
                    for (DataSnapshot datas : dataSnapshot.getChildren()) {
                        if(!is_id_resto_in_list((String) datas.child("placeId").getValue()))
                            list_places_nearby.add(create_place_nearby_from_datas_firebase(datas));
                    }
                }
                configure_recycler_view();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("eee Cancellation");
            }
        });
    }

    private void configure_recycler_view(){

    if(current_location==null)
        getLatLng_current_location();

        if(getActivity()!=null) {
            // Create adapter passing in the sample user data
            adapter = new ListViewAdapter(getActivity().getApplicationContext(), list_places_nearby, current_location, this);
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

    public List<Place_Nearby> getList_places_nearby() {
        return list_places_nearby;
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
        System.out.println("eee -----------------------------------------------------");

        if(list_places_nearby!=null){

            if(!type_sorting.equals("workmates")) {
                list_to_sort_dbl.addAll(create_list_to_sort(list_places_nearby, type_sorting));
                list_index.addAll(set_list_sorted_dbl(list_to_sort_dbl,type_sorting));
            } else {
                list_to_sort_int.addAll(create_list_to_sort(list_places_nearby));
                list_index.addAll(set_list_sorted_int(list_to_sort_int));
            }

            new_list_place_nearby.addAll(create_list_place_nearby_sorted(list_index));
            list_places_nearby.clear();
            list_places_nearby.addAll(new_list_place_nearby);


            for(Place_Nearby place_nearby : list_places_nearby)
                System.out.println("eee "+ place_nearby.getName_restaurant() + "    nb_workmates="+place_nearby.getWorkmatesList().size());

        }
    }

    private List<Place_Nearby> create_list_place_nearby_sorted(List<Integer> list_index){

        List<Place_Nearby> new_list = new ArrayList<>();

        for(Integer index:list_index)
            new_list.add(list_places_nearby.get(index));

        return new_list;
    }

    private List<Double> create_list_to_sort(List<Place_Nearby> new_list_places_nearby, String type_sorting){

        List<Double> list_to_sort = new ArrayList<>();

        for(Place_Nearby place : new_list_places_nearby){

            if(place!=null) {
                switch (type_sorting) {
                    case "stars":
                        list_to_sort.add(place.getRating());
                        break;
                    case "distance":
                        list_to_sort.add(calulate_distance(current_location, place));
                        break;
                }
            }
        }

        return list_to_sort;
    }

    private List<Integer> create_list_to_sort(List<Place_Nearby> new_list_places_nearby){

        List<Integer> list_to_sort = new ArrayList<>();

        for(Place_Nearby place : new_list_places_nearby){
            if(place!=null) {
                if(place.getWorkmatesList()!=null)
                    list_to_sort.add(place.getWorkmatesList().size());
            }
        }

        return list_to_sort;
    }

    private List<Integer> set_list_sorted_dbl(List<Double> list_to_sort, String type_sorting){

        List<Integer> list_index_sorted = new ArrayList<>();

        for(int i = 0; i<list_to_sort.size();i++){

            int index = 0 ;
            // Search the first index not in the table
            for(Double item : list_to_sort){
                if(!is_index_in_the_list(index,list_index_sorted))
                    break;
                else
                    index++;
            }

            int index_to_add = index;
            Double item_ref = list_to_sort.get(index);
            index = -1;

            for(Double item : list_to_sort) {
                index++;
                if (item != null && !is_index_in_the_list(index, list_index_sorted)) {

                    switch (type_sorting) {
                        case "stars":
                            if (compare(item,item_ref)>=0) {
                                index_to_add = index;
                                item_ref = item;
                            }
                            break;
                        case "distance":
                            if (compare(item,item_ref)<=0) {
                                index_to_add = index;
                                item_ref = item;
                            }
                            break;
                    }
                }
            }

            list_index_sorted.add(index_to_add);
        }

        return list_index_sorted;
    }

    private List<Integer> set_list_sorted_int(List<Integer> list_to_sort){

        List<Integer> list_index_sorted = new ArrayList<>();

        for(int i = 0; i<list_to_sort.size();i++){

            int index = 0 ;
            // Search the first index not in the table
            for(Integer item : list_to_sort){
                if(!is_index_in_the_list(index,list_index_sorted))
                    break;
                else
                    index++;
            }

            int index_to_add = index;
            Integer item_ref = list_to_sort.get(index);
            index = -1;

            for(Integer item : list_to_sort) {
                index++;
                if (item != null && !is_index_in_the_list(index, list_index_sorted)) {
                    if (compare(item,item_ref)>=0) {
                        index_to_add = index;
                        item_ref = item;
                    }
                }
            }

            list_index_sorted.add(index_to_add);
        }

        return list_index_sorted;
    }

    public void setList_places_nearby(List<Place_Nearby> list_places_nearby) {
        this.list_places_nearby = list_places_nearby;
    }

    public void setCurrent_location(LatLng current_location) {
        this.current_location = current_location;
    }

    private Boolean is_index_in_the_list(int index, List<Integer> list_index) {

        Boolean answer = false;

        for(int item : list_index){
            if(item == index) {
                answer=true;
                break;
            }
        }
        return answer;
    }

}
