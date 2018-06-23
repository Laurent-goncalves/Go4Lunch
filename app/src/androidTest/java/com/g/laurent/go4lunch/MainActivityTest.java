package com.g.laurent.go4lunch;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import com.g.laurent.go4lunch.Controllers.Activities.MultiActivity;
import com.g.laurent.go4lunch.Controllers.Fragments.MapsFragment;
import com.g.laurent.go4lunch.Models.List_Search_Nearby;
import com.g.laurent.go4lunch.Models.Workmate;
import com.g.laurent.go4lunch.Utils.DetailsPlace.Close;
import com.g.laurent.go4lunch.Utils.DetailsPlace.Open;
import com.g.laurent.go4lunch.Utils.DetailsPlace.OpeningHours;
import com.g.laurent.go4lunch.Utils.DetailsPlace.Period;
import com.g.laurent.go4lunch.Utils.Firebase_update;
import com.g.laurent.go4lunch.Utils.TimeCalculation;
import com.google.android.gms.common.data.DataBufferUtils;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBufferResponse;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import junit.framework.Assert;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import static android.content.ContentValues.TAG;


@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MultiActivity> mActivityTestRule = new ActivityTestRule<>(MultiActivity.class);

   // @Test
    public void create_new_users_firebase() {

        SharedPreferences sharedPreferences = mActivityTestRule.getActivity().getSharedPreferences();

        String EXTRA_LAT_CURRENT = "latitude_current_location";
        String EXTRA_LONG_CURRENT = "longitude_current_location";

        sharedPreferences.edit().putFloat(EXTRA_LAT_CURRENT,48.866667f).apply();
        sharedPreferences.edit().putFloat(EXTRA_LONG_CURRENT,2.333333f).apply();


        Firebase_update firebase_update = new Firebase_update(mActivityTestRule.getActivity().getApplicationContext());

        waiting_time(5000);

        firebase_update.update_full_workmate_data(new Workmate("Sean","ID1","https://i.pinimg.com/originals/e7/c4/dc/e7c4dc04867ad87c2437f22cc1859f5d.jpg",true,"IDresto1","McDo",null,null,null));
        firebase_update.update_full_workmate_data(new Workmate("Hugh","ID2","https://upload.wikimedia.org/wikipedia/commons/thumb/f/f7/Hugh_Jackman_%282017%29.jpg/1200px-Hugh_Jackman_%282017%29.jpg",false,null,null,null,null,null));
        firebase_update.update_full_workmate_data(new Workmate("George","ID3","https://gal.img.pmdstatic.net/fit/http.3A.2F.2Fprd2-bone-image.2Es3-website-eu-west-1.2Eamazonaws.2Ecom.2Fprismamedia_people.2F2017.2F06.2F30.2F2249dbc4-7761-4990-87af-258d04ba95ee.2Ejpeg/2419x1677/quality/80/george-clooney.jpg",true,"IDresto3",null,"Le Trucanous",null,null));
        firebase_update.update_full_workmate_data(new Workmate("Brigitte","ID4","https://pbs.twimg.com/profile_images/898819805083467776/IqAVGrO4_400x400.jpg",false,null,null,null,null,null));
    }

    @Test
    public void TEST_text_opening_hours() {

        OpeningHours openingHours = set_fake_openingHours();
        TimeCalculation timeCalculation = new TimeCalculation(mActivityTestRule.getActivity().getApplicationContext());
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

    //@Test
    public void TEST_SearchView_AutoComplete(){

        /*Toolbar_navig_Utils toolbar_navig_utils = new Toolbar_navig_Utils(mActivityTestRule.getActivity());

        mActivityTestRule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toolbar_navig_utils.configure_toolbar();

                waiting_time(5000);

                ViewInteraction searchAutoComplete = onView(
                        allOf(withId(R.id.search_src_text),
                                childAtPosition(
                                        allOf(withId(R.id.search_plate),
                                                childAtPosition(
                                                        withId(R.id.search_edit_frame),
                                                        1)),
                                        0),
                                isDisplayed()));
                searchAutoComplete.perform(replaceText("japonais"), closeSoftKeyboard());
            }
        });*/



       // toolbar_navig_utils.getSearchAutoComplete().setText("japonais");



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

    /*
    @Test
    public void check_liked_and_chosen_resto() {

        Firebase_update firebase_update = new Firebase_update(mActivityTestRule.getActivity().getApplicationContext());
        firebase_update.initialize_like_status_and_chosen_restaurant("UXKUE5wPVUfwqgkeSelNRi0MoQU2");

        waiting_time(1000);

        // Click on List Restos View
        ViewInteraction tabView = onView(
                allOf(childAtPosition(
                        childAtPosition(
                                withId(R.id.activity_multi_tabs),
                                0),
                        1),
                        isDisplayed()));
        tabView.perform(click());
        waiting_time(2000);

        // click on Workmates fragment
        ViewInteraction tabView2 = onView(
                allOf(childAtPosition(
                        childAtPosition(
                                withId(R.id.activity_multi_tabs),
                                0),
                        2),
                        isDisplayed()));
        tabView2.perform(click());
        waiting_time(2000);

        // Click on List Restos View
        ViewInteraction tabView3 = onView(
                allOf(childAtPosition(
                        childAtPosition(
                                withId(R.id.activity_multi_tabs),
                                0),
                        1),
                        isDisplayed()));
        tabView3.perform(click());
        waiting_time(2000);

        // click on 1st item of recyclerView
        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.list_view_resto),
                        childAtPosition(
                                withClassName(is("android.widget.RelativeLayout")),
                                2)));
        recyclerView.perform(actionOnItemAtPosition(0, click()));

        // Find the id of the first item
        MultiFragAdapter adapter= mActivityTestRule.getActivity().getPageAdapter();

        waiting_time(2000);

        ListRestoFragment list_restos_fragment = adapter.getListRestoFragment();
        String placeId_ref = list_restos_fragment.getList_places_nearby().get(0).getPlaceId();


        // click on button to choose the restaurant
        ViewInteraction circleImageView = onView(
                allOf(withId(R.id.valid_restaurant),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        0),
                                2)));
        circleImageView.perform(scrollTo(), click());
        waiting_time(2000);

        // click on button to like the restaurant
        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.like_button), withText("LIKE"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        1),
                                1)));
        appCompatButton2.perform(scrollTo(), click());
        waiting_time(2000);

        // Recover the list of liked restos and the resto chosen by the user
        Firebase_recover firebase_recover = new Firebase_recover(mActivityTestRule.getActivity().getApplicationContext(),
                "UXKUE5wPVUfwqgkeSelNRi0MoQU2");

        firebase_recover.recover_workmate_liked_restos();
        firebase_recover.recover_workmate_chosen_resto();
        waiting_time(5000);


        // Check if the resto liked is among resto_id of the list on Firebase
        List<String> list_places = firebase_recover.getList_restos_liked();
        Boolean resto_chosen = false;

        for(String restoid : list_places){
            if(restoid.equals(placeId_ref))
                resto_chosen=true;
        }

        Assert.assertTrue(resto_chosen);

        // Check if the resto_id on Firebase is the one chosen by the user
        String restoid_chosen=firebase_recover.getResto_id_chosen();
        Assert.assertEquals(placeId_ref,restoid_chosen);

    }



  /*  @Test
    public void Test_like_resto_saving_on_Firebase() {

        mActivityTestRule.getActivity().configure_and_show_ListRestoFragment();

        waiting_time(5000);

        ListRestoFragment list_restos_fragment = mActivityTestRule.getActivity().getListRestoFragment();
        String placeId_ref =list_restos_fragment.getList_places_nearby().get(0).getPlaceId();

        // Click on restaurant item in the list
        onView(withId(R.id.list_view_resto))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        // Click on button like restaurant
        ViewInteraction appCompatButton3 = onView(
                allOf(withId(R.id.like_button), withText("LIKE"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        1),
                                1)));
        appCompatButton3.perform(scrollTo(), click());

        waiting_time(2000);
        Firebase_recover firebase_recover = new Firebase_recover(mActivityTestRule.getActivity().getApplicationContext(),
                list_restos_fragment);

        firebase_recover.recover_workmate_restoId("UXKUE5wPVUfwqgkeSelNRi0MoQU2");
        waiting_time(3000);
        Assert.assertEquals(placeId_ref,list_restos_fragment.getPlaceId());
    }


    @Test
    public void TEST_bounds() {

        Context context = mActivityTestRule.getActivity().getApplicationContext();

        LatLng current_location = new LatLng(48.87116360802959,2.337829608029594);
        int radius = 500;

        DistanceCalculation tool_calcul_distance = new DistanceCalculation();
        LatLngBounds bounds = tool_calcul_distance.create_LatLngBounds(radius, current_location);


        //Google_Maps_Utils google_maps_utils = new Google_Maps_Utils(context);

        googleplacespredictions("Starbuck",context);


        String distance = tool_calcul_distance.calulate_distance(bounds.southwest.latitude,bounds.southwest.longitude,current_location.latitude,current_location.longitude);

        // System.out.println(bounds);
        // Assert.assertEquals(String.valueOf(radius),distance);

    }*/


    public void googleplacespredictions(String query, Context context){

        List<String> list_places_nearby = new ArrayList<>();
        LatLngBounds bounds = new LatLngBounds(new LatLng(38.46572222050097, -107.75668023304138),new LatLng(39.913037779499035, -105.88929176695862));
        GeoDataClient mGeoDataClient = Places.getGeoDataClient(context);

        Task<AutocompletePredictionBufferResponse> results =
                mGeoDataClient.getAutocompletePredictions(query, bounds, GeoDataClient.BoundsMode.STRICT, null);

        try {
            Tasks.await(results, 60, TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            e.printStackTrace();
        }

        try {
            AutocompletePredictionBufferResponse autocompletePredictions = results.getResult();

            // Freeze the results immutable representation that can be stored safely.
            ArrayList<AutocompletePrediction> al = DataBufferUtils.freezeAndClose(autocompletePredictions);

            for (AutocompletePrediction p : al) {
                list_places_nearby.add(p.getPlaceId());
            }

            MapsFragment mapsFragment = mActivityTestRule.getActivity().getPageAdapter().getMapsFragment();

            List_Search_Nearby list_search_nearby = new List_Search_Nearby("AIzaSyCAzX1ILkJlqSsTMkRJHSGEMAQWuqxSxKA",list_places_nearby,mapsFragment);

            waiting_time(5000);


        } catch (RuntimeExecutionException e) {
            // If the query did not complete successfully return null
            Log.e(TAG, "Error getting autocomplete prediction API call", e);
        }
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }

    private void waiting_time(int time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
/*
        ViewInteraction appCompatImageButton = onView(
                allOf(withContentDescription("Open navigation drawer"),
                        childAtPosition(
                                allOf(withId(R.id.activity_main_toolbar),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                0)),
                                0),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        ViewInteraction navigationMenuItemView = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.design_navigation_view),
                                childAtPosition(
                                        withId(R.id.activity_main_nav_view),
                                        0)),
                        3),
                        isDisplayed()));
        navigationMenuItemView.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(3586940);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(R.id.done_button), withText("Done"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.setting_activity_main),
                                        1),
                                1),
                        isDisplayed()));
        appCompatButton3.perform(click());*/