package com.g.laurent.go4lunch;

import com.g.laurent.go4lunch.Models.List_Search_Nearby;
import com.g.laurent.go4lunch.Models.Place_Nearby;
import com.g.laurent.go4lunch.Models.Workmates;
import com.g.laurent.go4lunch.Utils.Search_Nearby.Geometry;
import com.g.laurent.go4lunch.Utils.Search_Nearby.Location;
import com.google.android.gms.maps.model.LatLng;

import junit.framework.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;


public class ExampleUnitTest {
    @Test
    public void TEST_request_places_nearby() {

        LatLng city = new LatLng(48.6102599, 2.474805);
        String radius = "500";
        List_Search_Nearby list_Search_Nearby = new List_Search_Nearby(city,radius,null);

        waiting_time(3000);

        Assert.assertTrue(list_Search_Nearby.getList_places_nearby().size() > 0);

    }

    @Test
    public void TEST_sort_place_nearby() {

        ListRestoFragment listRestoFragment = new ListRestoFragment();
        listRestoFragment.setCurrent_location(new LatLng(48.866667,2.333333));
        List<String> list_id = new ArrayList<>();

        // sort by distance
        List<Place_Nearby> new_list_places_nearby = build_fake_list_place_nearby();
        listRestoFragment.setList_places_nearby(new_list_places_nearby);

        listRestoFragment.sort_list_places_nearby("distance");

        list_id.add(listRestoFragment.getList_places_nearby().get(0).getPlaceId());
        list_id.add(listRestoFragment.getList_places_nearby().get(1).getPlaceId());
        list_id.add(listRestoFragment.getList_places_nearby().get(2).getPlaceId());
        list_id.add(listRestoFragment.getList_places_nearby().get(3).getPlaceId());

        Assert.assertEquals("[ID1, ID2, ID3, ID4]",list_id.toString());

        // sort by stars number
        list_id = new ArrayList<>();
        new_list_places_nearby = build_fake_list_place_nearby();
        listRestoFragment.setList_places_nearby(new_list_places_nearby);

        listRestoFragment.sort_list_places_nearby("stars");

        list_id.add(listRestoFragment.getList_places_nearby().get(0).getPlaceId());
        list_id.add(listRestoFragment.getList_places_nearby().get(1).getPlaceId());
        list_id.add(listRestoFragment.getList_places_nearby().get(2).getPlaceId());
        list_id.add(listRestoFragment.getList_places_nearby().get(3).getPlaceId());

        Assert.assertEquals("[ID1, ID2, ID3, ID4]",list_id.toString());

        // sort by workmates number
        list_id = new ArrayList<>();
        new_list_places_nearby = build_fake_list_place_nearby();
        listRestoFragment.setList_places_nearby(new_list_places_nearby);

        listRestoFragment.sort_list_places_nearby("workmates");

        list_id.add(listRestoFragment.getList_places_nearby().get(0).getPlaceId());
        list_id.add(listRestoFragment.getList_places_nearby().get(1).getPlaceId());
        list_id.add(listRestoFragment.getList_places_nearby().get(2).getPlaceId());
        list_id.add(listRestoFragment.getList_places_nearby().get(3).getPlaceId());

        Assert.assertEquals("[ID1, ID2, ID3, ID4]",list_id.toString());
    }

    private List<Place_Nearby> build_fake_list_place_nearby(){

        List<Place_Nearby> new_list_places_nearby = new ArrayList<>();

        String id1 = "ID1";
        Geometry geometry1 = new Geometry();
        Location location1 = new Location();
        location1.setLat(48.86291787682425);
        location1.setLng(2.3430747831421286);
        geometry1.setLocation(location1);
        Double rating1 = 3.3d;
        List<Workmates> list_workmates1 = new ArrayList<>();
        list_workmates1.add(new Workmates("Jean",null,null,null,null,null,null,null));
        list_workmates1.add(new Workmates("Kevin",null,null,null,null,null,null,null));
        list_workmates1.add(new Workmates("Sami",null,null,null,null,null,null,null));
        list_workmates1.add(new Workmates("Caro",null,null,null,null,null,null,null));

        String id2 = "ID2";
        Geometry geometry2 = new Geometry();
        Location location2 = new Location();
        location2.setLat(48.869614156625715);
        location2.setLng(2.3578805769043356);
        geometry2.setLocation(location2);
        Double rating2 = 2.3d;
        List<Workmates> list_workmates2 = new ArrayList<>();
        list_workmates2.add(new Workmates("Jean",null,null,null,null,null,null,null));
        list_workmates2.add(new Workmates("Kevin",null,null,null,null,null,null,null));
        list_workmates2.add(new Workmates("Sami",null,null,null,null,null,null,null));


        String id3 = "ID3";
        Geometry geometry3 = new Geometry();
        Location location3 = new Location();
        location3.setLat(48.8705513387647);
        location3.setLng(2.3683948362427145);
        geometry3.setLocation(location3);
        Double rating3 = 1.6d;
        List<Workmates> list_workmates3 = new ArrayList<>();
        list_workmates3.add(new Workmates("Jean",null,null,null,null,null,null,null));
        list_workmates3.add(new Workmates("Kevin",null,null,null,null,null,null,null));


        String id4 = "ID4";
        Geometry geometry4 = new Geometry();
        Location location4 = new Location();
        location4.setLat(48.86934316171704);
        location4.setLng(2.385861381347695);
        geometry4.setLocation(location4);
        Double rating4 = 0.9d;
        List<Workmates> list_workmates4 = new ArrayList<>();
        list_workmates4.add(new Workmates("Jean",null,null,null,null,null,null,null));

        new_list_places_nearby.add(new Place_Nearby(null,id4,geometry4,null,rating4,null,null,list_workmates4));
        new_list_places_nearby.add(new Place_Nearby(null,id1,geometry1,null,rating1,null,null,list_workmates1));
        new_list_places_nearby.add(new Place_Nearby(null,id2,geometry2,null,rating2,null,null,list_workmates2));

        new_list_places_nearby.add(new Place_Nearby(null,id3,geometry3,null,rating3,null,null,list_workmates3));

        return new_list_places_nearby;
    }

    private void waiting_time(int time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}