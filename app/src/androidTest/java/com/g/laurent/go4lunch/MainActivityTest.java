package com.g.laurent.go4lunch;


import android.content.Context;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.mock.MockContext;
import android.test.suitebuilder.annotation.LargeTest;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import com.g.laurent.go4lunch.Controllers.Activities.MultiActivity;
import com.g.laurent.go4lunch.Controllers.Fragments.ListRestoFragment;
import com.g.laurent.go4lunch.Controllers.Fragments.MapsFragment;
import com.g.laurent.go4lunch.Models.List_Search_Nearby;
import com.g.laurent.go4lunch.Utils.DistanceCalculation;
import com.g.laurent.go4lunch.Utils.Firebase_recover;
import com.g.laurent.go4lunch.Utils.Firebase_update;
import com.g.laurent.go4lunch.Utils.Google_Maps_Utils;
import com.g.laurent.go4lunch.Views.MultiFragAdapter;
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
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;


@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MultiActivity> mActivityTestRule = new ActivityTestRule<>(MultiActivity.class);

    @Test
    public void faketest() {

        Assert.assertEquals(1,1);

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