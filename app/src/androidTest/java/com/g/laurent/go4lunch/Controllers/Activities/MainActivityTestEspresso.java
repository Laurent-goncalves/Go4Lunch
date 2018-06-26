package com.g.laurent.go4lunch.Controllers.Activities;


import android.app.Activity;
import android.support.design.widget.TabLayout;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Switch;

import com.g.laurent.go4lunch.Controllers.Fragments.SettingsFragment;
import com.g.laurent.go4lunch.Models.Place_Nearby;
import com.g.laurent.go4lunch.Models.Workmate;
import com.g.laurent.go4lunch.R;
import com.g.laurent.go4lunch.Utils.DetailsPlace.Geometry;
import com.g.laurent.go4lunch.Utils.DetailsPlace.Location;
import com.g.laurent.go4lunch.Views.MultiFragAdapter;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import junit.framework.Assert;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityTestEspresso {

    @Rule
    public GrantPermissionRule mGrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

    @Rule
    public ActivityTestRule<MultiActivity> mActivityTestRule = new ActivityTestRule<>(MultiActivity.class);

    @Test
    public void mainActivityTestEspresso() {

        //FirebaseDatabase.getInstance().goOffline();

        waiting_time(5000);
        mActivityTestRule.getActivity().configureViewPagerAndTabs(build_fake_list_place_nearby());
        waiting_time(10000);

        mActivityTestRule.getActivity().setToolbar();

        ViewInteraction tabView = onView(
                allOf(childAtPosition(
                        childAtPosition(
                                withId(R.id.activity_multi_tabs),
                                0),
                        1),
                        isDisplayed()));
        tabView.perform(click());

        ViewInteraction tabView2 = onView(
                allOf(childAtPosition(
                        childAtPosition(
                                withId(R.id.activity_multi_tabs),
                                0),
                        2),
                        isDisplayed()));
        tabView2.perform(click());

        ViewInteraction tabView3 = onView(
                allOf(childAtPosition(
                        childAtPosition(
                                withId(R.id.activity_multi_tabs),
                                0),
                        1),
                        isDisplayed()));
        tabView3.perform(click());

        waiting_time(3000);

        mActivityTestRule.getActivity().getPageAdapter().getListRestoFragment().setList_places_nearby(build_fake_list_place_nearby());

        mActivityTestRule.getActivity().getPageAdapter().getListRestoFragment().configure_recycler_view();

        waiting_time(3000);

        onView(withId(R.id.list_view_resto))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        waiting_time(5000);

        pressBack();

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html

        waiting_time(5000);

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.sort_by_number_workmates), withText("colleague"),
                        childAtPosition(
                                allOf(withId(R.id.linearlayout_buttons_sorting),
                                        childAtPosition(
                                                withClassName(is("android.widget.RelativeLayout")),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatButton2.perform(click());

        waiting_time(1000);
        ViewInteraction appCompatButton3 = onView(
                allOf(withId(R.id.sort_by_number_stars), withText("stars"),
                        childAtPosition(
                                allOf(withId(R.id.linearlayout_buttons_sorting),
                                        childAtPosition(
                                                withClassName(is("android.widget.RelativeLayout")),
                                                0)),
                                2),
                        isDisplayed()));
        appCompatButton3.perform(click());

        waiting_time(1000);
        ViewInteraction appCompatButton4 = onView(
                allOf(withId(R.id.sort_by_distance), withText("distance"),
                        childAtPosition(
                                allOf(withId(R.id.linearlayout_buttons_sorting),
                                        childAtPosition(
                                                withClassName(is("android.widget.RelativeLayout")),
                                                0)),
                                3),
                        isDisplayed()));
        appCompatButton4.perform(click());
    }

    @Test
    public void TEST_change_language(){

        waiting_time(5000);
        mActivityTestRule.getActivity().configureViewPagerAndTabs(build_fake_list_place_nearby());

        waiting_time(8000);

        mActivityTestRule.getActivity().configure_and_show_settings_activity();


        waiting_time(2000);
        SettingActivity settingActivity = getActivityInstance();

        waiting_time(5000);

        ViewInteraction switch_ = onView(
                allOf(withId(R.id.switch_french_english),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        1),
                                1),
                        isDisplayed()));
        switch_.perform(click());


        // Get the language set by the user
        SettingsFragment settingsFragment = (SettingsFragment) settingActivity.getSettingsFragment();
        Switch button_switch = settingsFragment.getSwitch_fr_eng();
        String lang;

        if(button_switch.isChecked())
            lang = "en";
        else
            lang = "fr";


        // Click on "done"
        waiting_time(1000);
        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.done_button), childAtPosition(
                                allOf(withId(R.id.framelayout_setting_frag),
                                        childAtPosition(
                                                withId(R.id.setting_activity_main),
                                                1)),
                                2),
                        isDisplayed()));
        appCompatButton2.perform(click());

        waiting_time(3000);

        mActivityTestRule.getActivity().configureViewPagerAndTabs(build_fake_list_place_nearby());
        waiting_time(10000);

        // Check the language of tabs
        TabLayout tabs = mActivityTestRule.getActivity().getTabs();

        switch(lang){
            case "fr":
                Assert.assertEquals("Carte", Objects.requireNonNull(tabs.getTabAt(0)).getText());
                break;
            case "en":
                Assert.assertEquals("Map View", Objects.requireNonNull(tabs.getTabAt(0)).getText());
                break;
        }
    }


    public SettingActivity getActivityInstance() {

        final SettingActivity[] currentActivity = new SettingActivity[1];

        getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                Collection resumedActivities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
                if (resumedActivities.iterator().hasNext()) {

                    if(resumedActivities.iterator().next() instanceof SettingActivity)
                        currentActivity[0] = (SettingActivity) resumedActivities.iterator().next();
                }
            }
        });

        return currentActivity[0];
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
        list_workmates1.add(new Workmate("Jean",null,null,null,null,null,null,null,null));
        list_workmates1.add(new Workmate("Kevin",null,null,null,null,null,null,null,null));
        list_workmates1.add(new Workmate("Sami",null,null,null,null,null,null,null,null));
        list_workmates1.add(new Workmate("Caro",null,null,null,null,null,null,null,null));

        String id2 = "ID2";
        Geometry geometry2 = new Geometry();
        Location location2 = new Location();
        location2.setLat(48.869614156625715);
        location2.setLng(2.3578805769043356);
        geometry2.setLocation(location2);
        Double rating2 = 2.3d;
        List<Workmate> list_workmates2 = new ArrayList<>();
        list_workmates2.add(new Workmate("Jean",null,null,null,null,null,null,null,null));
        list_workmates2.add(new Workmate("Kevin",null,null,null,null,null,null,null,null));
        list_workmates2.add(new Workmate("Sami",null,null,null,null,null,null,null,null));


        String id3 = "ID3";
        Geometry geometry3 = new Geometry();
        Location location3 = new Location();
        location3.setLat(48.8705513387647);
        location3.setLng(2.3683948362427145);
        geometry3.setLocation(location3);
        Double rating3 = 1.6d;
        List<Workmate> list_workmates3 = new ArrayList<>();
        list_workmates3.add(new Workmate("Jean",null,null,null,null,null,null,null,null));
        list_workmates3.add(new Workmate("Kevin",null,null,null,null,null,null,null,null));


        String id4 = "ID4";
        Geometry geometry4 = new Geometry();
        Location location4 = new Location();
        location4.setLat(48.86934316171704);
        location4.setLng(2.385861381347695);
        geometry4.setLocation(location4);
        Double rating4 = 0.9d;
        List<Workmate> list_workmates4 = new ArrayList<>();
        list_workmates4.add(new Workmate("Jean",null,null,null,null,null,null,null,null));

        new_list_places_nearby.add(new Place_Nearby("Resto 4",id4,geometry4,null,rating4,null,"rue saint nicolas",null,null,null,null));
        new_list_places_nearby.add(new Place_Nearby("Resto 1",id1,geometry1,null,rating1,null,"rue leo lagrange",null,null,null,null));
        new_list_places_nearby.add(new Place_Nearby("Resto 2",id2,geometry2,null,rating2,null,"rue Kennedy",null,null,null,null));
        new_list_places_nearby.add(new Place_Nearby("Resto 3",id3,geometry3,null,rating3,null,"rue Johnny Hallyday",null,null,null,null));

        return new_list_places_nearby;
    }

    private List<Workmate> build_fake_list_workmates(){

        List<Workmate> new_list_workmates = new ArrayList<>();

        new_list_workmates.add(new Workmate("Jean",null,null,null,"ID1",null,null,null,null));
        new_list_workmates.add(new Workmate("Kevin",null,null,null,"ID4",null,null,null,null));
        new_list_workmates.add(new Workmate("Sami",null,null,null,"ID1",null,null,null,null));
        new_list_workmates.add(new Workmate("Caro",null,null,null,"ID1",null,null,null,null));
        new_list_workmates.add(new Workmate("Jean",null,null,null,"ID3",null,null,null,null));
        new_list_workmates.add(new Workmate("Kevin",null,null,null,"ID2",null,null,null,null));
        new_list_workmates.add(new Workmate("Sami",null,null,null,"ID3",null,null,null,null));
        new_list_workmates.add(new Workmate("Sami",null,null,null,"ID4",null,null,null,null));
        new_list_workmates.add(new Workmate("Sami",null,null,null,"ID3",null,null,null,null));
        new_list_workmates.add(new Workmate("Sami",null,null,null,"ID1",null,null,null,null));
        new_list_workmates.add(new Workmate("Sami",null,null,null,"ID3",null,null,null,null));

        return new_list_workmates;
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
