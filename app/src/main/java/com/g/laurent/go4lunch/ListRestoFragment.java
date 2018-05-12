package com.g.laurent.go4lunch;


import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.g.laurent.go4lunch.Models.List_Search_Nearby;
import com.g.laurent.go4lunch.Models.Place_Nearby;
import com.g.laurent.go4lunch.Utils.Search_Nearby.Geometry;
import com.g.laurent.go4lunch.Utils.Search_Nearby.Location;
import com.g.laurent.go4lunch.Utils.Search_Nearby.OpeningHours;
import com.g.laurent.go4lunch.Utils.Search_Nearby.Photo;
import com.g.laurent.go4lunch.Views.ListViewAdapter;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ListRestoFragment extends Fragment {

    private List<Place_Nearby> list_places_nearby;
    private ListViewAdapter adapter;
    private LatLng current_location;
    private final static String EXTRA_LAT_CURRENT = "latitude_current_location";
    private final static String EXTRA_LONG_CURRENT = "longitude_current_location";

    String name_resto;
    String id;
    Geometry geometry;
    Double rating;
    List<Photo> photoList;
    OpeningHours openingHours;
    List<String> types;
    String address;

    @BindView(R.id.list_view_resto) RecyclerView recyclerView;

    public ListRestoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list_resto, container, false);
    }

    public void recover_list_resto_firebase() {

        list_places_nearby = new ArrayList<>();
       // FirebaseApp.initializeApp(getActivity().getApplicationContext());
        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference().child("restaurants");
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot!=null) {
                    for (DataSnapshot datas : dataSnapshot.getChildren())
                        list_places_nearby.add(create_place_nearby_from_datas_firebase(datas));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("eee Cancellation");
            }
        });
    }

    private Place_Nearby create_place_nearby_from_datas_firebase(DataSnapshot datas){

        getNameRestaurant(datas);
        getIdRestaurant(datas);
        getGeometryRestaurant(datas);
        getOpeningHours(datas);
        getListphotos(datas);
        getRating(datas);
        getTypes(datas);
        getAddress(datas);

        return new Place_Nearby(name_resto, id,geometry,openingHours,photoList,rating,types,address);
    }

    private void getAddress(DataSnapshot datas){

        this.address=null;
        if(datas.child("address")!=null)
            this.address= (String) datas.child("address").getValue();
        else
            this.address= null;
    }

    private void getTypes(DataSnapshot datas){
        this.types=new ArrayList<>();

        if(datas.child("types")!=null){

            for(DataSnapshot datas_child : datas.child("types").getChildren()) {

                if (datas_child != null){
                    if(datas_child.getValue()!=null)
                        types.add(datas_child.getValue().toString());
                }
            }
        }
    }

    private void getRating(DataSnapshot datas){

        rating= null;
        if(datas.child("rating")!=null) {
            if(datas.child("rating").getValue()!=null)
                rating = Double.parseDouble(datas.child("rating").getValue().toString());
        }
        else
            rating= null;
    }

    private void getListphotos(DataSnapshot datas){

        this.photoList= new ArrayList<>();
        Photo photo = new Photo();
        List<String> list_html_attr = new ArrayList<>();

        if(datas.child("photos")!=null){
            if(datas.child("photos").getChildren()!=null){

                for(DataSnapshot datas_child : datas.child("photos").getChildren()){

                    if(datas_child.child("htmlAttributions")!=null) {

                        for(DataSnapshot datas_child_child : datas_child.child("htmlAttributions").getChildren()){

                            list_html_attr.add((String) datas_child_child.getValue());
                            photo.setHtmlAttributions(list_html_attr);
                            photoList.add(photo);


                        }


                    }
                }
            }
        }
    }

    private void getOpeningHours(DataSnapshot datas){

        this.openingHours = new OpeningHours();

        if(datas.child("openingHours")!=null){
            if(datas.child("openingHours").child("openNow")!=null)
                openingHours.setOpenNow((Boolean) datas.child("openingHours").child("openNow").getValue());
        }
    }

    private void getGeometryRestaurant(DataSnapshot datas){

        this.geometry = new Geometry();
        Location location = new Location();

        if(datas.child("geometry")!=null){
            if(datas.child("geometry").child("location")!=null) {

                if(datas.child("geometry").child("location").child("lat")!=null)
                    location.setLat((Double) datas.child("geometry").child("location").child("lat").getValue());

                if(datas.child("geometry").child("location").child("lng")!=null)
                    location.setLng((Double) datas.child("geometry").child("location").child("lng").getValue());

            }
        }

        this.geometry.setLocation(location);
    }

    private void getIdRestaurant(DataSnapshot datas){
        id = null;
        if(datas.child("id")!=null)
            id= (String) datas.child("id").getValue();
        else
            id= null;
    }

    private void getNameRestaurant(DataSnapshot datas){
        name_resto=null;

        if(datas.child("name_restaurant")!=null)
            name_resto = (String) datas.child("name_restaurant").getValue();
        else
            name_resto= null;
    }

    private void configure_recycler_view(){

        getLatLng_current_location();

        if(adapter == null) {
            // Create adapter passing in the sample user data
            adapter = new ListViewAdapter(getActivity().getApplicationContext(),null,current_location);
            // Attach the adapter to the recyclerview to populate items
            recyclerView.setAdapter(adapter);
            // Set layout manager to position the items
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        } else
            adapter.notifyDataSetChanged();

    }

    private void getLatLng_current_location(){

        if(getArguments()!=null)
            current_location = new LatLng(getArguments().getDouble(EXTRA_LAT_CURRENT,0),
                                          getArguments().getDouble(EXTRA_LONG_CURRENT,0));
        else
            current_location=null;
    }

    public List<Place_Nearby> getList_places_nearby() {
        return list_places_nearby;
    }
}


        /* = FirebaseDatabase.getInstance();
        ref = database.getReferenceFromUrl("https://go4lunch-203512.firebaseio.com/");*/





  /*      Location location1= new Location();
        location1.setLat(38.2222);
        location1.setLng(2.33669);
        Geometry geometry1 = new Geometry();
        geometry1.setLocation(location1);

        OpeningHours openingHours = new OpeningHours();
        openingHours.setOpenNow(true);

        List<Photo> listphoto = new ArrayList<Photo>();
        Photo photo = new Photo();
        List<String> list_html_attr = new ArrayList<>();
        list_html_attr.add("htmlattribution");
        photo.setHtmlAttributions(list_html_attr);
        listphoto.add(photo);

        List<String> types = new ArrayList<>();
        types.add("bar");
        types.add("restaurant");
        types.add("japonais");

        mDatabase.child("resto1").setValue(new Place_Nearby("Resto 1", "ID1",geometry1,openingHours,listphoto,3.3,types,"rue du Périgord"));
        mDatabase.child("resto2").setValue(new Place_Nearby("Resto 2", "ID2",null,null,null,2.1,null,"rue Rivoli"));
*/