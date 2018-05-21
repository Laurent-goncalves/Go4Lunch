package com.g.laurent.go4lunch.Utils;

import android.content.Context;
import com.g.laurent.go4lunch.ListMatesFragment;
import com.g.laurent.go4lunch.ListRestoFragment;
import com.g.laurent.go4lunch.MapsFragment;
import com.g.laurent.go4lunch.Models.Callback_resto_fb;
import com.g.laurent.go4lunch.Models.Place_Nearby;
import com.g.laurent.go4lunch.Models.Workmates;
import com.g.laurent.go4lunch.RestoFragment;
import com.g.laurent.go4lunch.Utils.Search_Nearby.Geometry;
import com.g.laurent.go4lunch.Utils.Search_Nearby.Location;
import com.g.laurent.go4lunch.Utils.Search_Nearby.OpeningHours;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Firebase_recover {

    private String name_resto;
    private String placeId;
    private Geometry geometry;
    private Double rating;
    private List<Workmates> list_workmates;
    private OpeningHours openingHours;
    private String address;
    private List<String> list_types;
    private Boolean liked;
    private List<Place_Nearby> list_restos;
    private StorageReference storageRef;
    private DatabaseReference databaseReferenceWorkmates;
    private DatabaseReference databaseReferenceRestos;
    private ListRestoFragment listRestoFragment;
    private MapsFragment mapsFragment;
    private RestoFragment restoFragment;
    private ListMatesFragment listMatesFragment;
    private Workmates workmate;

    public Firebase_recover(Context context, ListRestoFragment listRestoFragment, MapsFragment mapsFragment, RestoFragment restoFragment, ListMatesFragment listMatesFragment) {

        list_restos = new ArrayList<>();

        FirebaseApp.initializeApp(context);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef= storage.getReference();

        this.listRestoFragment=listRestoFragment;
        this.mapsFragment=mapsFragment;
        this.restoFragment=restoFragment;
        this.listMatesFragment=listMatesFragment;

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReferenceWorkmates= databaseReference.child("workmates");
        databaseReferenceRestos= databaseReference.child("restaurants");
    }

    // ----------------------------------------------------------------------------------------------
    // ------------------------------- RECOVER LIST WORKMATES ---------------------------------------
    // ----------------------------------------------------------------------------------------------

    public void recover_list_workmates(){

        databaseReferenceWorkmates.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot datas) {
                if(datas!=null) {

                    for(DataSnapshot id : datas.getChildren()){

                        Workmates workmate = create_workmate_with_firebase_datas(id);

                        if(is_workmate_in_list(workmate.getId(),list_workmates))
                            list_workmates.add(workmate);
                    }

                    if(listMatesFragment!=null)
                        listMatesFragment.set_list_of_workmates(list_workmates);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("eee Cancellation");
            }
        });
    }

    // ----------------------------------------------------------------------------------------------
    // ------------------------------- RECOVER DATAS ON WORKMATE-------------------------------------
    // ----------------------------------------------------------------------------------------------

    public void recover_workmate_on_firebase(final String id_workmate){

        databaseReferenceWorkmates.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot datas) {
                if(datas!=null) {

                    for(DataSnapshot id : datas.getChildren()){

                        String id_checked = (String) id.child("id").getValue();

                        if(id_checked!=null){
                            if(id_checked.equals(id_workmate)) {
                                workmate = create_workmate_with_firebase_datas(id);

                                if(restoFragment!=null)
                                    restoFragment.set_current_user(workmate);
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("eee Cancellation");
            }
        });
    }

    private Workmates create_workmate_with_firebase_datas(DataSnapshot datas){

        // Search id of user in firebase workmates list

        List<String> list_resto_liked = new ArrayList<>();

        if (datas.child("list_resto_liked") != null) {
            for (DataSnapshot datas_child : datas.child("list_resto_liked").getChildren())
                list_resto_liked.add((String) datas_child.getValue());
        }

        return new Workmates(
                (String) datas.child("name").getValue(),
                (String) datas.child("id").getValue(),
                (String) datas.child("photoUrl").getValue(),
                (Boolean) datas.child("chosen").getValue(),
                (String) datas.child("resto_id").getValue(),
                (String) datas.child("resto_name").getValue(),
                (String) datas.child("resto_type").getValue(),
                list_resto_liked);

    }

    // ----------------------------------------------------------------------------------------------
    // -------------------------------   RECOVER DATAS ON RESTO  -------------------------------------
    // ----------------------------------------------------------------------------------------------

    public void recover_list_restos(final String user_id, final Callback_resto_fb callback){

        databaseReferenceRestos.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot datas) {
                if(datas!=null) {

                    // Create list of restaurants
                    for(DataSnapshot datas_child : datas.getChildren()) {
                        Place_Nearby new_resto=create_place_nearby_from_datas_firebase(datas_child, user_id);
                        if(!is_place_Id_in_list(new_resto.getPlaceId(),list_restos))
                            list_restos.add(new_resto);
                    }

                    // Send list of restos to fragments

                    if(user_id==null && callback!=null)
                        callback.update_chosen_list_restos(list_restos);

                    if(listRestoFragment!=null)
                        listRestoFragment.set_resto(list_restos);

                    if(mapsFragment!=null)
                        mapsFragment.set_resto(list_restos);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("eee Cancellation");
            }
        });
    }

    private Place_Nearby create_place_nearby_from_datas_firebase(DataSnapshot datas, String user_id){

        getNameRestaurant(datas);
        getIdRestaurant(datas);
        getGeometryRestaurant(datas);
        getOpeningHours(datas);
        getRating(datas);
        getTypes(datas);
        getAddress(datas);
        getWorkmatesJoining(datas);
        getLikedStatus(datas,user_id);

        return new Place_Nearby(name_resto, placeId,geometry,openingHours,rating,list_types,address,list_workmates,liked);
    }

    public void recover_resto_on_firebase(final String place_id){

        databaseReferenceRestos.child(place_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot datas) {
                if(datas!=null) {
                    Place_Nearby place_nearby = create_place_nearby_from_datas_firebase(datas,null);

                    if(restoFragment!=null)
                        restoFragment.configure_views_with_resto(place_nearby);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("eee Cancellation");
            }
        });
    }

    // ----------------------------------------------------------------------------------------------
    // ----------------------------------- TOOLS , GETTER AND SETTER --------------------------------
    // ----------------------------------------------------------------------------------------------

    public StorageReference get_picture_resto(String placeId){
        return storageRef.child(placeId + ".jpg");
    }

    private void getAddress(DataSnapshot datas){

        this.address=null;
        if(datas.child("address")!=null)
            this.address= (String) datas.child("address").getValue();
        else
            this.address= null;
    }

    private void getTypes(DataSnapshot datas){
        this.list_types=new ArrayList<>();

        if(datas.child("types")!=null){

            for(DataSnapshot datas_child : datas.child("types").getChildren()) {

                if (datas_child != null){
                    if(datas_child.getValue()!=null)
                        list_types.add(datas_child.getValue().toString());
                }
            }
        }
    }

    private void getRating(DataSnapshot datas){
        rating = 0d;
        if(datas.child("rating")!=null) {
            if(datas.child("rating").getValue()!=null)
                rating = Double.parseDouble(Objects.requireNonNull(datas.child("rating").getValue()).toString());
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
        placeId = null;
        if(datas.child("placeId")!=null)
            placeId= (String) datas.child("placeId").getValue();
        else
            placeId= null;
    }

    private void getNameRestaurant(DataSnapshot datas){
        name_resto=null;

        if(datas.child("name_restaurant")!=null)
            name_resto = (String) datas.child("name_restaurant").getValue();
        else
            name_resto= null;
    }

    private void getWorkmatesJoining(DataSnapshot datas){
        list_workmates=new ArrayList<>();

        if(datas.child("workmates_joining")!=null){

            for(DataSnapshot datas_child : datas.child("workmates_joining").getChildren()) {

                if (datas_child != null){
                    if(datas_child.getValue()!=null) {

                        List<String> list_resto_liked = new ArrayList<>();

                        if (datas_child.child("list_resto_liked") != null) {
                            for (DataSnapshot datas_CC : datas_child.child("list_resto_liked").getChildren())
                                list_resto_liked.add((String) datas_CC.getValue());
                        }

                        Workmates workmates = new Workmates(
                                (String) datas_child.child("name").getValue(),
                                (String) datas_child.child("id").getValue(),
                                (String) datas_child.child("photoUrl").getValue(),
                                (Boolean) datas_child.child("chosen").getValue(),
                                (String) datas_child.child("resto_id").getValue(),
                                (String) datas_child.child("resto_name").getValue(),
                                (String) datas_child.child("resto_type").getValue(),
                                list_resto_liked);

                        list_workmates.add(workmates);
                    }
                }
            }
        }
    }

    private void getLikedStatus(DataSnapshot datas,String user_id){
        liked = false;

        for(DataSnapshot datachild : datas.child("liked").getChildren()){
            String id_workmate = (String) datachild.getValue();
            if (id_workmate != null){
                if(id_workmate.equals(user_id)) {
                    liked = true;
                    break;
                }
            }
        }
    }

    private Boolean is_place_Id_in_list(String place_id, List<Place_Nearby> list_restos){

        for(Place_Nearby place_nearby : list_restos){
            if(place_nearby!=null){
                if(place_nearby.getPlaceId().equals(place_id))
                    return true;
            }
        }
        return false;
    }

    private Boolean is_workmate_in_list(String user_id, List<Workmates> list_workmates){

        if(list_workmates!=null) {
            for (Workmates workmate : list_workmates) {
                if (workmate != null) {
                    if (workmate.getId().equals(user_id))
                        return true;
                }
            }
        }
        return false;
    }

    public Workmates getWorkmate() {
        return workmate;
    }
}
