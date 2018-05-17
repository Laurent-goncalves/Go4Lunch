package com.g.laurent.go4lunch;

import android.app.Fragment;
import android.net.Uri;

import com.g.laurent.go4lunch.Models.Place_Nearby;
import com.g.laurent.go4lunch.Models.Workmates;
import com.g.laurent.go4lunch.Utils.Search_Nearby.Geometry;
import com.g.laurent.go4lunch.Utils.Search_Nearby.Location;
import com.g.laurent.go4lunch.Utils.Search_Nearby.OpeningHours;
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

        if(datas.child("list_workmates")!=null){

            for(DataSnapshot datas_child : datas.child("list_workmates").getChildren()) {

                if (datas_child != null){
                    if(datas_child.getValue()!=null) {

                        Workmates workmates = new Workmates(
                                (String) datas_child.child("name").getValue(),
                                (String) datas_child.child("id").getValue(),
                                (Uri) datas_child.child("photoUrl").getValue(),
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

}
