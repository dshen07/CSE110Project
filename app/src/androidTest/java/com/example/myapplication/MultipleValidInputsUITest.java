package com.example.myapplication;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MultipleValidInputsUITest {

    @Rule
    public ActivityScenarioRule<MapsActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(MapsActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.ACCESS_FINE_LOCATION");

    @Test
    public void multipleValidInputsUITest() {
        ViewInteraction button = onView(
                allOf(withId(R.id.addLocation), withText("-"),
                        childAtPosition(
                                allOf(withId(R.id.constraint),
                                        childAtPosition(
                                                withClassName(is("android.widget.RelativeLayout")),
                                                0)),
                                7),
                        isDisplayed()));
        button.perform(click());

        ViewInteraction editText = onView(
                allOf(withId(R.id.latitude),
                        childAtPosition(
                                childAtPosition(
                                        withId(com.google.android.material.R.id.custom),
                                        0),
                                0),
                        isDisplayed()));
        editText.perform(replaceText("-3"), closeSoftKeyboard());

        ViewInteraction editText2 = onView(
                allOf(withId(R.id.longitude),
                        childAtPosition(
                                childAtPosition(
                                        withId(com.google.android.material.R.id.custom),
                                        0),
                                1),
                        isDisplayed()));
        editText2.perform(replaceText("2"), closeSoftKeyboard());

        ViewInteraction editText3 = onView(
                allOf(withId(R.id.name),
                        childAtPosition(
                                childAtPosition(
                                        withId(com.google.android.material.R.id.custom),
                                        0),
                                2),
                        isDisplayed()));
        editText3.perform(replaceText("input1"), closeSoftKeyboard());

        ViewInteraction button2 = onView(
                allOf(withId(R.id.save), withText("Save"),
                        childAtPosition(
                                childAtPosition(
                                        withId(com.google.android.material.R.id.custom),
                                        0),
                                3),
                        isDisplayed()));
        button2.perform(click());

        ViewInteraction button3 = onView(
                allOf(withId(R.id.addLocation), withText("-"),
                        childAtPosition(
                                allOf(withId(R.id.constraint),
                                        childAtPosition(
                                                withClassName(is("android.widget.RelativeLayout")),
                                                0)),
                                7),
                        isDisplayed()));
        button3.perform(click());

        ViewInteraction editText4 = onView(
                allOf(withId(R.id.latitude),
                        childAtPosition(
                                childAtPosition(
                                        withId(com.google.android.material.R.id.custom),
                                        0),
                                0),
                        isDisplayed()));
        editText4.perform(replaceText("-32"), closeSoftKeyboard());

        ViewInteraction editText5 = onView(
                allOf(withId(R.id.longitude),
                        childAtPosition(
                                childAtPosition(
                                        withId(com.google.android.material.R.id.custom),
                                        0),
                                1),
                        isDisplayed()));
        editText5.perform(replaceText("117"), closeSoftKeyboard());

        ViewInteraction editText6 = onView(
                allOf(withId(R.id.name),
                        childAtPosition(
                                childAtPosition(
                                        withId(com.google.android.material.R.id.custom),
                                        0),
                                2),
                        isDisplayed()));
        editText6.perform(replaceText("input2"), closeSoftKeyboard());

        ViewInteraction button4 = onView(
                allOf(withId(R.id.save), withText("Save"),
                        childAtPosition(
                                childAtPosition(
                                        withId(com.google.android.material.R.id.custom),
                                        0),
                                3),
                        isDisplayed()));
        button4.perform(click());

        ViewInteraction textView = onView(
                allOf(withId(R.id.myImageViewText), withText("input1"),
                        withParent(allOf(withId(R.id.layoutTemplate),
                                withParent(IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class)))),
                        isDisplayed()));
        textView.check(matches(withText("input1")));

        ViewInteraction textView2 = onView(
                allOf(withId(R.id.myImageViewText), withText("input2"),
                        withParent(allOf(withId(R.id.layoutTemplate),
                                withParent(IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class)))),
                        isDisplayed()));
        textView2.check(matches(withText("input2")));

        ViewInteraction imageView = onView(
                allOf(withId(R.id.myImageView),
                        withParent(allOf(withId(R.id.layoutTemplate),
                                withParent(IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class)))),
                        isDisplayed()));
        imageView.check(matches(isDisplayed()));

        ViewInteraction imageView2 = onView(
                allOf(withId(R.id.myImageView),
                        withParent(allOf(withId(R.id.layoutTemplate),
                                withParent(IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class)))),
                        isDisplayed()));
        imageView2.check(matches(isDisplayed()));

        ViewInteraction button5 = onView(
                allOf(withId(R.id.center), withText("Center"),
                        childAtPosition(
                                allOf(withId(R.id.constraint),
                                        childAtPosition(
                                                withClassName(is("android.widget.RelativeLayout")),
                                                0)),
                                4),
                        isDisplayed()));
        button5.perform(click());
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
}
