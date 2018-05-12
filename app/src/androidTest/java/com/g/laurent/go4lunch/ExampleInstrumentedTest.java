package com.g.laurent.go4lunch;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.rule.ActivityTestRule;
import com.g.laurent.go4lunch.Models.List_Search_Nearby;
import com.google.android.gms.maps.model.LatLng;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.g.laurent.go4lunch", appContext.getPackageName());
    }

    @Rule
    public ActivityTestRule<MapsActivity> mActivityTestRule = new ActivityTestRule<>(MapsActivity.class,false,false);

    @Test
    public void write_and_read_list_places_nearby_in_firebase() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        mActivityTestRule.launchActivity(null);

        // get a list of nearby places
        LatLng currentloc = new LatLng(48.866667,2.333333);
        List_Search_Nearby list_search_nearby = new List_Search_Nearby(currentloc,"500");
        waiting_time(3000);
        int count_init = list_search_nearby.getList_places_nearby().size();

        // Write list of nearby places in Firebase database
        mActivityTestRule.getActivity().update_list_nearby_places_firebase(list_search_nearby);
        waiting_time(1000);

        // Retrieve list of nearby places in Firebase database
        ListRestoFragment listRestoFragment = new ListRestoFragment();
        listRestoFragment.recover_list_resto_firebase();
        waiting_time(1000);

        int count_final = listRestoFragment.getList_places_nearby().size();

        Assert.assertTrue(count_init==count_final);
    }

    private void waiting_time(int time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
