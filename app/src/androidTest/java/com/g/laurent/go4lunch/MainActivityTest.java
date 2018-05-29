package com.g.laurent.go4lunch;


import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.g.laurent.go4lunch.Controllers.Activities.MultiActivity;
import com.g.laurent.go4lunch.Controllers.Fragments.ListRestoFragment;
import com.g.laurent.go4lunch.R;
import com.g.laurent.go4lunch.Utils.Firebase_recover;
import com.g.laurent.go4lunch.Utils.Firebase_update;

import junit.framework.Assert;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsAnything.anything;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MultiActivity> mActivityTestRule = new ActivityTestRule<>(MultiActivity.class);

    @Test
    public void Test_navigation_between_menus() {

        mActivityTestRule.getActivity().configure_and_show_MapsFragment();

        // Click on List view menu
        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.list_view_button), withText("List View"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.window_sign_in),
                                        1),
                                1),
                        isDisplayed()));
        appCompatButton2.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click on menu workmates
        ViewInteraction appCompatButton3 = onView(
                allOf(withId(R.id.workmates_button), withText("Workmates"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.window_sign_in),
                                        1),
                                2),
                        isDisplayed()));
        appCompatButton3.perform(click());

        // Click on menu list view
        ViewInteraction appCompatButton4 = onView(
                allOf(withId(R.id.list_view_button), withText("List View"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.window_sign_in),
                                        1),
                                1),
                        isDisplayed()));
        appCompatButton4.perform(click());

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click on restaurant item in the list
        onView(withId(R.id.list_view_resto))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        // Click on menu workmates
        ViewInteraction appCompatButton5 = onView(
                allOf(withId(R.id.workmates_button), withText("Workmates"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.window_sign_in),
                                        1),
                                2),
                        isDisplayed()));
        appCompatButton5.perform(click());

        // Click to open naviation drawer
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

        // Click on setting menu
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
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Click on button DONE
        ViewInteraction appCompatButton6 = onView(
                allOf(withId(R.id.done_button), withText("Done"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.setting_activity_main),
                                        1),
                                1),
                        isDisplayed()));
        appCompatButton6.perform(click());

    }

    @Test
    public void Test_restoId_saving_on_Firebase() {

        // Re-initialize list of restaurants liked and the restaurant chosen by the userId
        Firebase_update firebase_update = new Firebase_update(mActivityTestRule.getActivity().getApplicationContext());
        firebase_update.initialize_like_status_and_chosen_restaurant("UXKUE5wPVUfwqgkeSelNRi0MoQU2");

        waiting_time(1000);
        mActivityTestRule.getActivity().configure_and_show_ListRestoFragment();

        waiting_time(15000);

        ListRestoFragment list_restos_fragment = mActivityTestRule.getActivity().getListRestoFragment();
        String placeId_ref = list_restos_fragment.getList_places_nearby().get(0).getPlaceId();

        // Click on restaurant item in the list
        onView(withId(R.id.list_view_resto))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        // Click on button choose restaurant
        ViewInteraction circleImageView = onView(
                allOf(withId(R.id.valid_restaurant),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        0),
                                2)));
        circleImageView.perform(scrollTo(), click());

        waiting_time(8000);
        Firebase_recover firebase_recover = new Firebase_recover(mActivityTestRule.getActivity().getApplicationContext(),
                list_restos_fragment);

        firebase_recover.recover_workmate_restoId("UXKUE5wPVUfwqgkeSelNRi0MoQU2");
        waiting_time(8000);
        Assert.assertEquals(placeId_ref,list_restos_fragment.getPlaceId());
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
*/
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
