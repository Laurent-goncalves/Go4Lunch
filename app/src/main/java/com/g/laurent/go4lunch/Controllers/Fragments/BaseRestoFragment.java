package com.g.laurent.go4lunch.Controllers.Fragments;

import android.content.Context;
import android.support.v4.app.Fragment;
import com.g.laurent.go4lunch.Controllers.Activities.MultiActivity;
import com.g.laurent.go4lunch.Models.PlaceNearby;
import com.g.laurent.go4lunch.Models.Workmate;
import com.g.laurent.go4lunch.Utils.FirebaseRecover;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.List;
import static java.lang.Double.compare;

/**
 * A simple {@link Fragment} subclass.
 */
public abstract class BaseRestoFragment extends Fragment {

    protected MultiActivity activity;
    protected LatLng currentPlaceLatLng;
    protected List<PlaceNearby> list_places_nearby;
    protected List<PlaceNearby> list_places_nearby_OLD;
    protected List<Workmate> list_workmates;
    protected static final String EXTRA_LIST_RESTOS_JSON = "list_restos_json";
    protected FirebaseRecover mFirebase_recover;

    public BaseRestoFragment() {
        // Required empty public constructor
    }

    protected Boolean is_resto_chosen_by_workmates(PlaceNearby resto, List<Workmate> workmateList){

        // For each workmate of the list, check the resto id is present
        if(workmateList!=null && resto!=null){
            for(Workmate workmate : workmateList) {
                if (workmate != null) {
                    if (workmate.getResto_id() != null) {
                        if (workmate.getResto_id().equals(resto.getPlaceId()))
                            return true;
                    }
                }
            }
        }
        return false;
    }

    protected Double calculate_distance(LatLng current_location, PlaceNearby location) {

        if(current_location!=null && location!=null){
            if(location.getGeometry()!=null){
                if(location.getGeometry().getLocation()!=null){

                    Double lat1 = current_location.latitude;
                    Double lon1 = current_location.longitude;
                    Double lat2 = location.getGeometry().getLocation().getLat();
                    Double lon2 = location.getGeometry().getLocation().getLng();

                    Double latitude1 = lat1 * Math.PI / 180; // calculate latitude in radians
                    Double latitude2 = lat2 * Math.PI / 180; // calculate latitude in radians
                    Double longitude1 = lon1 * Math.PI / 180; // calculate longitude in radians
                    Double longitude2 = lon2 * Math.PI / 180; // calculate longitude in radians

                    Double Radius = 6371d; // radius of the Earth

                    return 1000 * Radius * Math.acos(Math.cos(latitude1) * Math.cos(latitude2) *
                            Math.cos(longitude2 - longitude1) + Math.sin(latitude1) *
                            Math.sin(latitude2)); // distance calculation
                }
            }
        }
        return null;
    }

    // -------------------------------- SORTING FUNCTIONALITY ----------------------------------------------

    protected List<PlaceNearby> create_list_place_nearby_sorted(List<Integer> list_index){

        List<PlaceNearby> new_list = new ArrayList<>();

        for(Integer index:list_index)
            new_list.add(list_places_nearby.get(index));

        return new_list;
    }

    protected List<Double> create_list_to_sort(List<PlaceNearby> new_list_places_nearby, String type_sorting, LatLng current_location){

        List<Double> list_to_sort = new ArrayList<>();

        for(PlaceNearby place : new_list_places_nearby){
            if(place!=null) {
                switch (type_sorting) {
                    case "stars":
                        list_to_sort.add(place.getRating());
                        break;
                    case "distance":
                        list_to_sort.add(calculate_distance(current_location, place));
                        break;
                }
            }
        }
        return list_to_sort;
    }

    protected List<Integer> set_list_sorted_dbl(List<Double> list_to_sort, String type_sorting){

        List<Integer> list_index_sorted = new ArrayList<>();

        for(int i = 0; i<list_to_sort.size();i++){

            int index = 0 ;
            // Search the first index not in the table
            for(Double ignored : list_to_sort){
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
                if (item != null && item_ref!=null && !is_index_in_the_list(index, list_index_sorted)) {

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

    public void sort_list_places_nearby_by_workmates(){

        if(list_workmates!=null){

            String[][] tab_number_workmates = create_tab_restoId_by_workmate_number();
            List<String> list_sorted = sort_by_workmates_number(tab_number_workmates);
            List<PlaceNearby> new_list_places_nearby = new ArrayList<>();

            for(String restoId : list_sorted){
                for(PlaceNearby resto : list_places_nearby){
                    if(resto!=null && restoId!=null){
                        if(resto.getPlaceId()!=null){
                            if(resto.getPlaceId().equals(restoId)){
                                new_list_places_nearby.add(resto);
                                break;
                            }
                        }
                    }
                }
            }
            list_places_nearby = new_list_places_nearby;
        }
    }

    private String[][] create_tab_restoId_by_workmate_number(){

        List<String> list_to_single_restoID = new ArrayList<>();

        // create list of restoId without double id's
        for(PlaceNearby resto : list_places_nearby){
            if(resto!=null) {
                if (!is_restoId_in_tab(resto.getPlaceId(), list_to_single_restoID)) {
                    list_to_single_restoID.add(resto.getPlaceId());
                }
            }
        }

        // With this list of single id's, get a table with the number of workmate for each id

        String[][] tab_number_workmates = new String[2][list_places_nearby.size()];
        int i = 0;
        int count;

        for(String restoId : list_to_single_restoID){

            tab_number_workmates[0][i] = restoId;
            count = 0;

            for(Workmate workmate : list_workmates){
                if(workmate!=null) {
                    if(workmate.getResto_id()!=null){
                        if(workmate.getResto_id().equals(restoId))
                            count++;
                    }
                }
            }
            tab_number_workmates[1][i] = String.valueOf(count);
            i++;
        }
        return tab_number_workmates;
    }

    private List<String> sort_by_workmates_number(String[][] tab_number_workmates){

        List<String> list_restoId_sorted = new ArrayList<>();
        List<Integer> list_to_sort = new ArrayList<>();

        for(int i = 0; i<tab_number_workmates[0].length;i++){

            int index = 0 ;
            // Search the first index not in the table
            for(Integer ignored : list_to_sort){
                if(!is_index_in_the_list(index,list_to_sort))
                    break;
                else
                    index++;
            }

            int index_to_add = index;
            int item_ref = Integer.parseInt(tab_number_workmates[1][index]);

            for(int j = index_to_add; j<tab_number_workmates[0].length;j++){
                if (!is_index_in_the_list(j, list_to_sort)) {
                    if (compare(Integer.parseInt(tab_number_workmates[1][j]),item_ref)>=0) {
                        index_to_add = j;
                        item_ref = Integer.parseInt(tab_number_workmates[1][j]);
                    }
                }
            }
            list_to_sort.add(index_to_add);
        }

        // Build list of sorted restoId
        for(int item : list_to_sort)
            list_restoId_sorted.add(tab_number_workmates[0][item]);

        return list_restoId_sorted;
    }

    private Boolean is_restoId_in_tab(String restoId, List<String> list_to_single_restoID){

        if(list_to_single_restoID!=null){

            for(String id : list_to_single_restoID){
                if(id!=null){
                    if(id.equals(restoId))
                        return true;
                }
            }
        }

        return false;
    }

    protected Boolean is_index_in_the_list(int index, List<Integer> list_index) {

        Boolean answer = false;

        for(int item : list_index){
            if(item == index) {
                answer=true;
                break;
            }
        }
        return answer;
    }

    public void recover_list_workmates(List<PlaceNearby> list_resto) {
        if(list_places_nearby_OLD!=null && list_places_nearby!=null){
            if(list_places_nearby_OLD.size()==0){ // if there is no place nearby in the old list, it means this method is called for the search
                list_places_nearby_OLD.addAll(list_places_nearby);
            }
        }

        this.list_places_nearby = list_resto;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MultiActivity){
            activity=(MultiActivity) context;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mFirebase_recover !=null)
            mFirebase_recover.recover_list_workmates();
    }

    // -------------------------- GETTER and SETTER ----------------------------------------------------

    public void setList_places_nearby(List<PlaceNearby> list_places_nearby) {
        this.list_places_nearby = list_places_nearby;
    }

    public List<PlaceNearby> getList_places_nearby() {
        return list_places_nearby;
    }

    public List<Workmate> getList_workmates() {
        return list_workmates;
    }

    public void setList_workmates(List<Workmate> list_workmates) {
        this.list_workmates = list_workmates;
    }
}