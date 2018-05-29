package com.g.laurent.go4lunch.Controllers.Fragments;

import android.app.Activity;
import android.app.Fragment;

import com.g.laurent.go4lunch.Models.Callback_DetailResto;
import com.g.laurent.go4lunch.Models.Callback_resto_fb;
import com.g.laurent.go4lunch.Models.Place_Nearby;
import com.g.laurent.go4lunch.Models.Workmate;
import com.g.laurent.go4lunch.Utils.Firebase_update;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Double.compare;

/**
 * A simple {@link Fragment} subclass.
 */
public abstract class BaseRestoFragment extends Fragment {

    protected List<Place_Nearby> list_places_nearby;
    protected Firebase_update firebase_tool;
    protected Callback_resto_fb mCallback_resto_fb;
    protected Callback_DetailResto mCallback_detailResto;
    protected List<Workmate> list_workmates;

    public BaseRestoFragment() {
        // Required empty public constructor
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

    protected Boolean is_resto_chosen_by_workmates(Place_Nearby resto, List<Workmate> workmateList){

        Boolean answer = false;

        if(workmateList!=null && resto!=null){
            for(Workmate workmate : workmateList) {
                if (workmate != null) {

                    if (workmate.getResto_id() != null) {
                        if (workmate.getResto_id().equals(resto.getPlaceId()))
                            answer = true;
                    }
                }
            }
        }

        return answer;
    }

    protected Double calculate_distance(LatLng current_location, Place_Nearby location) {

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

    protected List<Place_Nearby> create_list_place_nearby_sorted(List<Integer> list_index){

        List<Place_Nearby> new_list = new ArrayList<>();

        for(Integer index:list_index)
            new_list.add(list_places_nearby.get(index));

        return new_list;
    }

    protected List<Double> create_list_to_sort(List<Place_Nearby> new_list_places_nearby, String type_sorting, LatLng current_location){

        List<Double> list_to_sort = new ArrayList<>();

        for(Place_Nearby place : new_list_places_nearby){

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

    protected List<Integer> create_list_to_sort(List<Place_Nearby> new_list_places_nearby){

        List<Integer> list_to_sort = new ArrayList<>();

        for(Place_Nearby place : new_list_places_nearby){
            if(place!=null) {
                if(list_workmates!=null)
                    list_to_sort.add(list_workmates.size());
            }
        }

        return list_to_sort;
    }

    // -------------------------------- SORT BY WORKMATE ----------------------------------------------

    public void sort_list_places_nearby_by_workmates(){

        List<String> list_to_sort = create_list_workmates_to_sort();
        String[][] tab_number_workmates = create_tab_restoId_by_workmate_number(list_to_sort);
        List<String> list_sorted = sort_by_workmates_number(tab_number_workmates);
        List<Place_Nearby> new_list_places_nearby = new ArrayList<>();

        for(String restoId : list_sorted){
            for(Place_Nearby resto : list_places_nearby){
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

    protected List<String> create_list_workmates_to_sort(){

        List<String> list_to_sort = new ArrayList<>();

        for(Workmate workmate : list_workmates){
            if(workmate!=null) {
                if(workmate.getResto_id()!=null)
                    list_to_sort.add(workmate.getResto_id());
            }
        }

        return list_to_sort;
    }

    private String[][] create_tab_restoId_by_workmate_number(List<String> list_to_sort){

        List<String> list_to_single_restoID = new ArrayList<>();

        // create list of restoId without double id's
        for(Place_Nearby resto : list_places_nearby){
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

    private List<String> sort_by_workmates_number(String[][] tab_number_workmates){

        List<String> list_restoId_sorted = new ArrayList<>();
        List<Integer> list_to_sort = new ArrayList<>();

        for(int i = 0; i<tab_number_workmates[0].length;i++){

            int index = 0 ;
            // Search the first index not in the table
            for(Integer item : list_to_sort){
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

    protected List<Integer> set_list_sorted_int(List<Integer> list_to_sort){

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

    // -------------------------- GETTER and SETTER ----------------------------------------------------

    public void setList_places_nearby(List<Place_Nearby> list_places_nearby) {
        this.list_places_nearby = list_places_nearby;
    }

    public List<Place_Nearby> getList_places_nearby() {
        return list_places_nearby;
    }

    public List<Workmate> getList_workmates() {
        return list_workmates;
    }

    public void setList_workmates(List<Workmate> list_workmates) {
        this.list_workmates = list_workmates;
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

        try {
            mCallback_resto_fb = (Callback_resto_fb) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement Callback_resto_fb");
        }
    }
}
