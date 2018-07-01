package com.g.laurent.go4lunch;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Switch;
import com.g.laurent.go4lunch.Controllers.Activities.MultiActivity;
import com.g.laurent.go4lunch.Controllers.Activities.RestoActivity;
import com.g.laurent.go4lunch.Controllers.Activities.SettingActivity;
import com.g.laurent.go4lunch.Controllers.Fragments.SettingsFragment;
import com.g.laurent.go4lunch.Models.PlaceNearby;
import com.g.laurent.go4lunch.Utils.DetailsPlace.Close;
import com.g.laurent.go4lunch.Utils.DetailsPlace.Geometry;
import com.g.laurent.go4lunch.Utils.DetailsPlace.Location;
import com.g.laurent.go4lunch.Utils.DetailsPlace.Open;
import com.g.laurent.go4lunch.Utils.DetailsPlace.OpeningHours;
import com.g.laurent.go4lunch.Utils.DetailsPlace.Period;
import com.g.laurent.go4lunch.Utils.TimeCalculation;
import com.google.gson.Gson;
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
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public GrantPermissionRule mGrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

    @Rule
    public ActivityTestRule<MultiActivity> mActivityTestRule = new ActivityTestRule<>(MultiActivity.class, false,false);

    @Test
    public void TEST_click_on_tabs() {

        mActivityTestRule.launchActivity(null);
        mActivityTestRule.getActivity().getProgressBar().setVisibility(View.GONE);
        List<PlaceNearby> list_places = build_fake_list_place_nearby();

        waiting_time(10000);
        mActivityTestRule.getActivity().configureViewPagerAndTabs(list_places);

        waiting_time(10000);

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

        waiting_time(5000);
        mActivityTestRule.finishActivity();
    }

    @Test
    public void TEST_list_restofragment(){

        String EXTRA_RESTO_DETAILS = "resto_details";
        mActivityTestRule.launchActivity(null);
        mActivityTestRule.getActivity().getProgressBar().setVisibility(View.GONE);
        List<PlaceNearby> list_places = build_fake_list_place_nearby();

        waiting_time(10000);

        mActivityTestRule.getActivity().configure_and_show_listrestofragment();

        waiting_time(5000);

        mActivityTestRule.getActivity().listRestoFragment.setList_places_nearby(list_places);
        mActivityTestRule.getActivity().listRestoFragment.configure_recycler_view();

        waiting_time(8000);

        mActivityTestRule.getActivity().runOnUiThread(() -> {

            mActivityTestRule.getActivity().listRestoFragment.getButton_workmates().performClick();
            waiting_time(1000);
            mActivityTestRule.getActivity().listRestoFragment.getButton_stars().performClick();
            waiting_time(1000);
            mActivityTestRule.getActivity().listRestoFragment.getButton_distance().performClick();
        });

        waiting_time(20000);

        Intent intent = new Intent(mActivityTestRule.getActivity().getApplicationContext(),RestoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Gson gson = new Gson();
        String resto_json = gson.toJson(list_places.get(0));
        intent.putExtra(EXTRA_RESTO_DETAILS,resto_json);
        mActivityTestRule.getActivity().getApplicationContext().startActivity(intent);

        waiting_time(5000);

        pressBack();
        mActivityTestRule.finishActivity();
    }

    private String lang;

    @Test
    public void TEST_change_language(){

        mActivityTestRule.launchActivity(null);
        mActivityTestRule.getActivity().getProgressBar().setVisibility(View.GONE);
        waiting_time(5000);

        mActivityTestRule.getActivity().configureViewPagerAndTabs(build_fake_list_place_nearby());
        waiting_time(1000);

        mActivityTestRule.getActivity().configure_and_show_settings_activity();
        waiting_time(5000);

        SettingActivity settingActivity = getActivityInstance();

        // Get the language set by the user
        SettingsFragment settingsFragment = settingActivity.getSettingsFragment();
        Switch button_switch = settingsFragment.getSwitch_fr_eng();


        mActivityTestRule.getActivity().runOnUiThread(() -> {

            if(button_switch.isChecked()){
                button_switch.setChecked(false);
                lang = "en";
            } else {
                button_switch.setChecked(true);
                lang = "fr";
            }
        });

        // Click on "done"
        waiting_time(5000);
        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.done_button), childAtPosition(
                        allOf(withId(R.id.framelayout_setting_frag),
                                childAtPosition(
                                        withId(R.id.setting_activity_main),
                                        1)),
                        2),
                        isDisplayed()));
        appCompatButton2.perform(click());

        waiting_time(10000);

        mActivityTestRule.getActivity().configureViewPagerAndTabs(build_fake_list_place_nearby());
        waiting_time(1000);

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

        mActivityTestRule.finishActivity();
    }

    @Test
    public void TEST_text_opening_hours() {

        mActivityTestRule.launchActivity(null);
        mActivityTestRule.getActivity().getProgressBar().setVisibility(View.GONE);
        waiting_time(5000);

        OpeningHours openingHours = set_fake_openingHours();
        TimeCalculation timeCalculation = new TimeCalculation(mActivityTestRule.getActivity().getApplicationContext());
        int current_day = 2;
        int current_time = 1000;

        //      0h                  11h         13h15            19h          22h30            23h59
        //      |____________________|oooooooooooo|_______________|ooooooooooooo|________________|
        //                     /\
        //                 current_time

        String text = mActivityTestRule.getActivity().getApplicationContext().getResources().getString(R.string.open_at);

        Assert.assertEquals(text + " 11h",timeCalculation.getInformationAboutOpeningAndClosure(openingHours.getPeriods(),current_time,current_day-1));

        current_time = 1200;

        //      0h                  11h         13h15            19h          22h30            23h59
        //      |____________________|oooooooooooo|_______________|ooooooooooooo|________________|
        //                                /\
        //                          current_time

        text = mActivityTestRule.getActivity().getApplicationContext().getResources().getString(R.string.open_until);

        Assert.assertEquals(text +" 13h15",timeCalculation.getInformationAboutOpeningAndClosure(openingHours.getPeriods(),current_time,current_day-1));

        current_time = 1300;

        //      0h                  11h         13h15            19h          22h30            23h59
        //      |____________________|oooooooooooo|_______________|ooooooooooooo|________________|
        //                                      /\
        //                                 current_time

        text = mActivityTestRule.getActivity().getApplicationContext().getResources().getString(R.string.closed_soon);

        Assert.assertEquals(text + " 15 min)",timeCalculation.getInformationAboutOpeningAndClosure(openingHours.getPeriods(),current_time,current_day-1));

        current_time = 1400;

        //      0h                  11h         13h15            19h          22h30            23h59
        //      |____________________|oooooooooooo|_______________|ooooooooooooo|________________|
        //                                              /\
        //                                        current_time

        text = mActivityTestRule.getActivity().getApplicationContext().getResources().getString(R.string.open_at);

        Assert.assertEquals(text + " 19h",timeCalculation.getInformationAboutOpeningAndClosure(openingHours.getPeriods(),current_time,current_day-1));

        current_time = 2130;

        //      0h                  11h         13h15            19h          22h30            23h59
        //      |____________________|oooooooooooo|_______________|ooooooooooooo|________________|
        //                                                                /\
        //                                                            current_time

        text = mActivityTestRule.getActivity().getApplicationContext().getResources().getString(R.string.open_until);

        Assert.assertEquals(text + " 22h30",timeCalculation.getInformationAboutOpeningAndClosure(openingHours.getPeriods(),current_time,current_day-1));


        current_time = 2300;

        //      0h                  11h         13h15            19h          22h30            23h59
        //      |____________________|oooooooooooo|_______________|ooooooooooooo|________________|
        //                                                                            /\
        //                                                                       current_time

        text = mActivityTestRule.getActivity().getApplicationContext().getResources().getString(R.string.closed_now);

        Assert.assertEquals(text,timeCalculation.getInformationAboutOpeningAndClosure(openingHours.getPeriods(),current_time,current_day-1));

        mActivityTestRule.finishActivity();
    }

    public SettingActivity getActivityInstance() {

        final SettingActivity[] currentActivity = new SettingActivity[1];

        getInstrumentation().runOnMainSync(() -> {
            Collection resumedActivities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
            if (resumedActivities.iterator().hasNext()) {

                if(resumedActivities.iterator().next() instanceof SettingActivity)
                    currentActivity[0] = (SettingActivity) resumedActivities.iterator().next();
            }
        });

        return currentActivity[0];
    }

    private List<PlaceNearby> build_fake_list_place_nearby(){

        List<PlaceNearby> new_list_places_nearby = new ArrayList<>();

        String id1 = "ID1";
        Geometry geometry1 = new Geometry();
        Location location1 = new Location();
        location1.setLat(48.86291787682425);
        location1.setLng(2.3430747831421286);
        geometry1.setLocation(location1);
        Double rating1 = 3.3d;

        String id2 = "ID2";
        Geometry geometry2 = new Geometry();
        Location location2 = new Location();
        location2.setLat(48.869614156625715);
        location2.setLng(2.3578805769043356);
        geometry2.setLocation(location2);
        Double rating2 = 2.3d;

        String id3 = "ID3";
        Geometry geometry3 = new Geometry();
        Location location3 = new Location();
        location3.setLat(48.8705513387647);
        location3.setLng(2.3683948362427145);
        geometry3.setLocation(location3);
        Double rating3 = 1.6d;

        String id4 = "ID4";
        Geometry geometry4 = new Geometry();
        Location location4 = new Location();
        location4.setLat(48.86934316171704);
        location4.setLng(2.385861381347695);
        geometry4.setLocation(location4);
        Double rating4 = 0.9d;

        new_list_places_nearby.add(new PlaceNearby("Resto 4",id4,geometry4,null,rating4,null,"rue saint nicolas",null,null,null,null));
        new_list_places_nearby.add(new PlaceNearby("Resto 1",id1,geometry1,null,rating1,null,"rue leo lagrange",null,null,null,null));
        new_list_places_nearby.add(new PlaceNearby("Resto 2",id2,geometry2,null,rating2,null,"rue Kennedy",null,null,null,null));
        new_list_places_nearby.add(new PlaceNearby("Resto 3",id3,geometry3,null,rating3,null,"rue Johnny Hallyday",null,null,null,null));

        return new_list_places_nearby;
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
