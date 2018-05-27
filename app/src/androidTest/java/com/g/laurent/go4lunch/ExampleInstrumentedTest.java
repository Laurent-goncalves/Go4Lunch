package com.g.laurent.go4lunch;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.rule.ActivityTestRule;
import com.g.laurent.go4lunch.Models.List_Search_Nearby;
import com.g.laurent.go4lunch.Models.Workmate;
import com.g.laurent.go4lunch.Utils.Firebase_recover;
import com.g.laurent.go4lunch.Utils.Firebase_update;
import com.google.android.gms.maps.model.LatLng;

import junit.framework.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;


/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Rule
    public ActivityTestRule<MultiActivity> mActivityTestRule = new ActivityTestRule<>(MultiActivity.class,false,false);

    @Test
    public void write_and_read_list_places_nearby_in_firebase() {
        // Context of the app under test.
        //Context appContext = InstrumentationRegistry.getTargetContext();
        mActivityTestRule.launchActivity(null);

        // get a list of nearby places
        LatLng currentloc = new LatLng(48.866667,2.333333);
        List_Search_Nearby list_search_nearby = new List_Search_Nearby(currentloc,"500",null);
        waiting_time(3000);
        int count_init = list_search_nearby.getList_places_nearby().size();

        // Write list of nearby places in Firebase database
        mActivityTestRule.getActivity().update_list_nearby_places_firebase(list_search_nearby.getList_places_nearby());
        waiting_time(1000);

        // Retrieve list of nearby places in Firebase database
        ListRestoFragment listRestoFragment = new ListRestoFragment();
        //listRestoFragment.recover_list_resto_firebase();
        waiting_time(1000);

        int count_final = listRestoFragment.getList_places_nearby().size();

        Assert.assertTrue(count_init==count_final);
    }

    @Test
    public void update_and_recover_data_firebase() {
        // Context of the app under test.
        //Context appContext = InstrumentationRegistry.getTargetContext();
        mActivityTestRule.launchActivity(null);

        //FirebaseApp.initializeApp(mActivityTestRule.getActivity().getApplicationContext());
        Firebase_update firebase_workmates_update = new Firebase_update(mActivityTestRule.getActivity().getApplicationContext(),null);

        // Create a new user in Firebase
        List<String> list_resto_liked = new ArrayList<>();
        list_resto_liked.add("ID_RESTO_1");
        list_resto_liked.add("ID_RESTO_2");
        list_resto_liked.add("ID_RESTO_3");
        Workmate workmate = new Workmate("Jean", "ID1", "html_photo_Url", true, "ID_RESTO_1", "Le resto 1", "pizzeria",list_resto_liked);

        firebase_workmates_update.update_full_workmate_data(workmate);

        // Recover the new user in Firebase
        Firebase_recover firebase_workmates_recover = new Firebase_recover(mActivityTestRule.getActivity().getApplicationContext(),null,null,null,null);

        firebase_workmates_recover.recover_workmate_on_firebase("ID1");

        waiting_time(2000);
        firebase_workmates_recover.recover_workmate_on_firebase("ID1");

        Workmate workmate_recovered= firebase_workmates_recover.getWorkmate();

        Assert.assertEquals("Jean",workmate_recovered.getName());
        Assert.assertEquals("ID1",workmate_recovered.getId());
        Assert.assertEquals(list_resto_liked,workmate_recovered.getList_resto_liked());
    }




    private void waiting_time(int time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}


