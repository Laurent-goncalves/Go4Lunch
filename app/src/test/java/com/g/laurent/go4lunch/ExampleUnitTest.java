package com.g.laurent.go4lunch;

import com.g.laurent.go4lunch.Models.List_Search_Nearby;
import com.google.android.gms.maps.model.LatLng;
import junit.framework.Assert;
import org.junit.Test;


public class ExampleUnitTest {
    @Test
    public void TEST_request_places_nearby() {

        LatLng city = new LatLng(48.6102599, 2.474805);
        String radius = "500";
        List_Search_Nearby list_Search_Nearby = new List_Search_Nearby(city,radius,null);

        waiting_time(3000);

        Assert.assertTrue(list_Search_Nearby.getList_places_nearby().size() > 0);

    }

    private void waiting_time(int time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}