package com.g.laurent.go4lunch;

import android.content.Context;
import android.test.mock.MockContext;

import com.g.laurent.go4lunch.Controllers.Fragments.ListRestoFragment;
import com.g.laurent.go4lunch.Models.List_Search_Nearby;
import com.g.laurent.go4lunch.Models.Place_Nearby;
import com.g.laurent.go4lunch.Models.Workmate;
import com.g.laurent.go4lunch.Utils.DetailsPlace.Close;
import com.g.laurent.go4lunch.Utils.DetailsPlace.Open;
import com.g.laurent.go4lunch.Utils.DetailsPlace.OpeningHours;
import com.g.laurent.go4lunch.Utils.DetailsPlace.Period;
import com.g.laurent.go4lunch.Utils.DetailsPlace.Geometry;
import com.g.laurent.go4lunch.Utils.DetailsPlace.Location;
import com.g.laurent.go4lunch.Utils.DistanceCalculation;
import com.g.laurent.go4lunch.Utils.Google_Maps_Utils;
import com.g.laurent.go4lunch.Utils.TimeCalculation;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import junit.framework.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import java.util.ArrayList;
import java.util.List;


public class ExampleUnitTest {

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
        List<Workmate> new_list_workmates = new ArrayList<>();
        new_list_workmates = build_fake_list_workmates();

        listRestoFragment.setList_places_nearby(new_list_places_nearby);
        listRestoFragment.set_list_of_workmates(new_list_workmates);

        listRestoFragment.sort_list_places_nearby_by_workmates();

        list_id.add(listRestoFragment.getList_places_nearby().get(0).getPlaceId());
        list_id.add(listRestoFragment.getList_places_nearby().get(1).getPlaceId());
        list_id.add(listRestoFragment.getList_places_nearby().get(2).getPlaceId());
        list_id.add(listRestoFragment.getList_places_nearby().get(3).getPlaceId());

        Assert.assertEquals("[ID3, ID1, ID4, ID2]",list_id.toString());
    }

    @Test
    public void TEST_text_opening_hours() {

        Place_Nearby place_nearby = Mockito.mock(Place_Nearby.class);
        OpeningHours openingHours = set_fake_openingHours();
        TimeCalculation timeCalculation = new TimeCalculation(new MockContext());
        int current_day = 2;
        int current_time = 1000;

        //      0h                  11h         13h15            19h          22h30            23h59
        //      |____________________|oooooooooooo|_______________|ooooooooooooo|________________|
        //                     /\
        //                 current_time

        Assert.assertEquals("Open at 11h",timeCalculation.getInformationAboutOpeningAndClosure(openingHours.getPeriods(),current_time,current_day-1));

        current_time = 1200;

        //      0h                  11h         13h15            19h          22h30            23h59
        //      |____________________|oooooooooooo|_______________|ooooooooooooo|________________|
        //                                /\
        //                          current_time

        Assert.assertEquals("Open until 13h15",timeCalculation.getInformationAboutOpeningAndClosure(openingHours.getPeriods(),current_time,current_day-1));

        current_time = 1300;

        //      0h                  11h         13h15            19h          22h30            23h59
        //      |____________________|oooooooooooo|_______________|ooooooooooooo|________________|
        //                                      /\
        //                                 current_time

        Assert.assertEquals("Closed soon (in 15 min)",timeCalculation.getInformationAboutOpeningAndClosure(openingHours.getPeriods(),current_time,current_day-1));

        current_time = 1400;

        //      0h                  11h         13h15            19h          22h30            23h59
        //      |____________________|oooooooooooo|_______________|ooooooooooooo|________________|
        //                                              /\
        //                                        current_time

        Assert.assertEquals("Open at 19h",timeCalculation.getInformationAboutOpeningAndClosure(openingHours.getPeriods(),current_time,current_day-1));

        current_time = 2130;

        //      0h                  11h         13h15            19h          22h30            23h59
        //      |____________________|oooooooooooo|_______________|ooooooooooooo|________________|
        //                                                                /\
        //                                                            current_time

        Assert.assertEquals("Open until 22h30",timeCalculation.getInformationAboutOpeningAndClosure(openingHours.getPeriods(),current_time,current_day-1));


        current_time = 2300;

        //      0h                  11h         13h15            19h          22h30            23h59
        //      |____________________|oooooooooooo|_______________|ooooooooooooo|________________|
        //                                                                            /\
        //                                                                       current_time

        Assert.assertEquals("Closed now",timeCalculation.getInformationAboutOpeningAndClosure(openingHours.getPeriods(),current_time,current_day-1));


    }

    @Test
    public void TEST_request_places_nearby() {

        String type = "restaurant";
        String radius = "500";
        String api_key = "AIzaSyBiRkG6clZcF-KhwQIPGq5t8h-KBk-8ldA";
        LatLng latLng = new LatLng(48.6102599, 2.474805);

        List_Search_Nearby list_search_nearby = new List_Search_Nearby(api_key,latLng,radius,type);

        waiting_time(5000);
        Assert.assertTrue(list_search_nearby.getList_places_nearby().size()>0);
    }


    private OpeningHours set_fake_openingHours(){

        OpeningHours openingHours = new OpeningHours();
        List<Period> periods = new ArrayList<>();

        Open open1 = new Open();
        Close close1 = new Close();
        open1.setDay(1);
        open1.setTime("1100");
        close1.setDay(1);
        close1.setTime("1315");
        Period period1 = new Period();
        period1.setClose(close1);
        period1.setOpen(open1);


        Open open2 = new Open();
        Close close2 = new Close();
        open2.setDay(1);
        open2.setTime("1900");
        close2.setDay(1);
        close2.setTime("2230");
        Period period2 = new Period();
        period2.setClose(close2);
        period2.setOpen(open2);


        periods.add(period1);
        periods.add(period2);

        openingHours.setPeriods(periods);

        return openingHours;
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
        List<Workmate> list_workmates1 = new ArrayList<>();
        list_workmates1.add(new Workmate("Jean",null,null,null,null,null,null,null));
        list_workmates1.add(new Workmate("Kevin",null,null,null,null,null,null,null));
        list_workmates1.add(new Workmate("Sami",null,null,null,null,null,null,null));
        list_workmates1.add(new Workmate("Caro",null,null,null,null,null,null,null));

        String id2 = "ID2";
        Geometry geometry2 = new Geometry();
        Location location2 = new Location();
        location2.setLat(48.869614156625715);
        location2.setLng(2.3578805769043356);
        geometry2.setLocation(location2);
        Double rating2 = 2.3d;
        List<Workmate> list_workmates2 = new ArrayList<>();
        list_workmates2.add(new Workmate("Jean",null,null,null,null,null,null,null));
        list_workmates2.add(new Workmate("Kevin",null,null,null,null,null,null,null));
        list_workmates2.add(new Workmate("Sami",null,null,null,null,null,null,null));


        String id3 = "ID3";
        Geometry geometry3 = new Geometry();
        Location location3 = new Location();
        location3.setLat(48.8705513387647);
        location3.setLng(2.3683948362427145);
        geometry3.setLocation(location3);
        Double rating3 = 1.6d;
        List<Workmate> list_workmates3 = new ArrayList<>();
        list_workmates3.add(new Workmate("Jean",null,null,null,null,null,null,null));
        list_workmates3.add(new Workmate("Kevin",null,null,null,null,null,null,null));


        String id4 = "ID4";
        Geometry geometry4 = new Geometry();
        Location location4 = new Location();
        location4.setLat(48.86934316171704);
        location4.setLng(2.385861381347695);
        geometry4.setLocation(location4);
        Double rating4 = 0.9d;
        List<Workmate> list_workmates4 = new ArrayList<>();
        list_workmates4.add(new Workmate("Jean",null,null,null,null,null,null,null));

        new_list_places_nearby.add(new Place_Nearby(null,id4,geometry4,null,rating4,null,null,null,null,null,null));
        new_list_places_nearby.add(new Place_Nearby(null,id1,geometry1,null,rating1,null,null,null,null,null,null));
        new_list_places_nearby.add(new Place_Nearby(null,id2,geometry2,null,rating2,null,null,null,null,null,null));
        new_list_places_nearby.add(new Place_Nearby(null,id3,geometry3,null,rating3,null,null,null,null,null,null));

        return new_list_places_nearby;
    }

    private List<Workmate> build_fake_list_workmates(){

        List<Workmate> new_list_workmates = new ArrayList<>();

        new_list_workmates.add(new Workmate("Jean",null,null,null,"ID1",null,null,null));
        new_list_workmates.add(new Workmate("Kevin",null,null,null,"ID4",null,null,null));
        new_list_workmates.add(new Workmate("Sami",null,null,null,"ID1",null,null,null));
        new_list_workmates.add(new Workmate("Caro",null,null,null,"ID1",null,null,null));
        new_list_workmates.add(new Workmate("Jean",null,null,null,"ID3",null,null,null));
        new_list_workmates.add(new Workmate("Kevin",null,null,null,"ID2",null,null,null));
        new_list_workmates.add(new Workmate("Sami",null,null,null,"ID3",null,null,null));
        new_list_workmates.add(new Workmate("Sami",null,null,null,"ID4",null,null,null));
        new_list_workmates.add(new Workmate("Sami",null,null,null,"ID3",null,null,null));
        new_list_workmates.add(new Workmate("Sami",null,null,null,"ID1",null,null,null));
        new_list_workmates.add(new Workmate("Sami",null,null,null,"ID3",null,null,null));

        return new_list_workmates;
    }

    private void waiting_time(int time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}