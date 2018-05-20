package com.g.laurent.go4lunch;

import android.app.Fragment;
import android.net.Uri;

import com.g.laurent.go4lunch.Models.Place_Nearby;
import com.g.laurent.go4lunch.Models.Workmates;
import com.g.laurent.go4lunch.Utils.Search_Nearby.Geometry;
import com.g.laurent.go4lunch.Utils.Search_Nearby.Location;
import com.g.laurent.go4lunch.Utils.Search_Nearby.OpeningHours;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public abstract class BaseRestoFragment extends Fragment {

    protected String name_resto;
    protected String placeId;
    protected Geometry geometry;
    protected Double rating;
    protected OpeningHours openingHours;
    protected List<String> types;
    protected String address;
    protected List<Workmates> list_workmates;
    protected Place_Nearby resto;
    protected List<Place_Nearby> list_places_nearby;

    public BaseRestoFragment() {
        // Required empty public constructor
    }

    protected void getAddress(DataSnapshot datas){

        this.address=null;
        if(datas.child("address")!=null)
            this.address= (String) datas.child("address").getValue();
        else
            this.address= null;
    }

    protected void getTypes(DataSnapshot datas){
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

    protected void getRating(DataSnapshot datas){
        rating = 0d;
        if(datas.child("rating")!=null) {
            if(datas.child("rating").getValue()!=null)
                rating = Double.parseDouble(datas.child("rating").getValue().toString());
        }
    }

    protected void getOpeningHours(DataSnapshot datas){

        this.openingHours = new OpeningHours();

        if(datas.child("openingHours")!=null){
            if(datas.child("openingHours").child("openNow")!=null)
                openingHours.setOpenNow((Boolean) datas.child("openingHours").child("openNow").getValue());
        }
    }

    protected void getGeometryRestaurant(DataSnapshot datas){

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

    protected void getIdRestaurant(DataSnapshot datas){
        placeId = null;
        if(datas.child("placeId")!=null)
            placeId= (String) datas.child("placeId").getValue();
        else
            placeId= null;
    }

    protected void getNameRestaurant(DataSnapshot datas){
        name_resto=null;

        if(datas.child("name_restaurant")!=null)
            name_resto = (String) datas.child("name_restaurant").getValue();
        else
            name_resto= null;
    }

    protected void getWorkmatesJoining(DataSnapshot datas){
        list_workmates=new ArrayList<>();

        if(datas.child("workmates_joining")!=null){

            for(DataSnapshot datas_child : datas.child("workmates_joining").getChildren()) {

                if (datas_child != null){
                    if(datas_child.getValue()!=null) {

                        Workmates workmates = new Workmates(
                                (String) datas_child.child("name").getValue(),
                                (String) datas_child.child("id").getValue(),
                                (String) datas_child.child("photoUrl").getValue(),
                                (Boolean) datas_child.child("chosen").getValue(),
                                (String) datas_child.child("resto_id").getValue(),
                                (String) datas_child.child("resto_name").getValue(),
                                (String) datas_child.child("resto_type").getValue());

                        list_workmates.add(workmates);
                    }
                }
            }
        }
    }

    protected Place_Nearby create_place_nearby_from_datas_firebase(DataSnapshot datas){

        getNameRestaurant(datas);
        getIdRestaurant(datas);
        getGeometryRestaurant(datas);
        getOpeningHours(datas);
        getRating(datas);
        getTypes(datas);
        getAddress(datas);
        getWorkmatesJoining(datas);

        return new Place_Nearby(name_resto, placeId,geometry,openingHours,rating,types,address,list_workmates);
    }

    protected Boolean is_id_resto_in_list(String id_to_check){

        Boolean answer = false;

        for(Place_Nearby place_nearby : list_places_nearby){
            if(place_nearby!=null){
                if(place_nearby.getPlaceId()!=null){
                    if(place_nearby.getPlaceId().equals(id_to_check)){
                        answer=true;
                        break;
                    }
                }
            }
        }

        return answer;
    }

    protected Double calulate_distance(LatLng current_location, Place_Nearby location) {

        if(current_location!=null && location!=null){

            if(location.getGeometry()!=null){

                if(location.getGeometry().getLocation()!=null){

                    Double lat1 = current_location.latitude;
                    Double lon1 = current_location.longitude;
                    Double lat2 = location.getGeometry().getLocation().getLat();
                    Double lon2 = location.getGeometry().getLocation().getLng();

                    Double latitude1 = lat1 * Math.PI / 180;
                    Double latitude2 = lat2 * Math.PI / 180;
                    Double longitude1 = lon1 * Math.PI / 180;
                    Double longitude2 = lon2 * Math.PI / 180;

                    Double Radius = 6371d;

                    return 1000 * Radius * Math.acos(Math.cos(latitude1) * Math.cos(latitude2) *
                            Math.cos(longitude2 - longitude1) + Math.sin(latitude1) *
                            Math.sin(latitude2));
                }
            }
        }
        return null;
    }
}
