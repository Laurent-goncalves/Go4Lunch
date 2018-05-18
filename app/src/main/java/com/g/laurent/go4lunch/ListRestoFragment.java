package com.g.laurent.go4lunch;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.g.laurent.go4lunch.Models.Callback_DetailResto;
import com.g.laurent.go4lunch.Models.Place_Nearby;
import com.g.laurent.go4lunch.Utils.Search_Nearby.Geometry;
import com.g.laurent.go4lunch.Utils.Search_Nearby.Location;
import com.g.laurent.go4lunch.Utils.Search_Nearby.OpeningHours;
import com.g.laurent.go4lunch.Views.Resto_List.ListViewAdapter;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class ListRestoFragment extends BaseRestoFragment implements ListViewAdapter.Listener {

    @BindView(R.id.list_view_resto) RecyclerView recyclerView;
    private List<Place_Nearby> list_places_nearby;
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
                    for (DataSnapshot datas : dataSnapshot.getChildren())
                        list_places_nearby.add(create_place_nearby_from_datas_firebase(datas));
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

        getLatLng_current_location();

        if(adapter == null) {
            if(getActivity()!=null) {
                // Create adapter passing in the sample user data
                adapter = new ListViewAdapter(getActivity().getApplicationContext(), list_places_nearby, current_location, this);
                // Attach the adapter to the recyclerview to populate items
                recyclerView.setAdapter(adapter);
                // Set layout manager to position the items
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
            }
        } else
            adapter.notifyDataSetChanged();

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
}
