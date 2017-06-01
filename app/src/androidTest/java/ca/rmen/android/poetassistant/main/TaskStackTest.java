/*
 * Copyright (c) 2017 Carmen Alvarez
 *
 * This file is part of Poet Assistant.
 *
 * Poet Assistant is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Poet Assistant is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Poet Assistant.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.rmen.android.poetassistant.main;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.support.test.espresso.Espresso;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.main.rules.ActivityVisibleIdlingResource;
import ca.rmen.android.poetassistant.main.rules.PoetAssistantActivityTestRule;
import ca.rmen.android.poetassistant.settings.SettingsActivity;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static ca.rmen.android.poetassistant.main.CustomChecks.checkFirstDefinition;
import static ca.rmen.android.poetassistant.main.TestUiUtils.checkTitleStripOrTab;
import static ca.rmen.android.poetassistant.main.TestUiUtils.clickPreference;
import static ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerRight;
import static org.hamcrest.Matchers.allOf;

/**
 * Test for these issues:
 * https://github.com/caarmen/poet-assistant/issues/19
 * https://github.com/caarmen/poet-assistant/issues/81
 */
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4.class)
public class TaskStackTest {

    @Rule
    public PoetAssistantActivityTestRule<SettingsActivity> mActivityTestRule = new PoetAssistantActivityTestRule<>(SettingsActivity.class, false);

    @Test
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void dictionaryDeepLinkAfterThemeChangeTest() {
        deepLinkAfterThemeChangeTest("poetassistant://dictionary/muffin");
    }

    @Test
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void queryDeepLinkAfterThemeChangeTest() {
        deepLinkAfterThemeChangeTest("poetassistant://query/muffin");
    }

    private void deepLinkAfterThemeChangeTest(String deepLinkUrl) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }
        // Change the theme
        Intent intent = new Intent();
        mActivityTestRule.launchActivity(intent);
        clickPreference(R.string.pref_theme_title);
        onView(withText(R.string.pref_theme_value_light)).check(matches(isChecked()));
        onView(withText(R.string.pref_theme_value_dark)).perform(click());

        // Open a deep link
        getInstrumentation().getUiAutomation().executeShellCommand("am start -a android.intent.action.VIEW -d " + deepLinkUrl);

        // Wait for the MainActivity to appear
        ActivityVisibleIdlingResource waitForMainActivity = new ActivityVisibleIdlingResource(
                (Application) getInstrumentation().getTargetContext().getApplicationContext(),
                MainActivity.class.getName());
        Espresso.registerIdlingResources(waitForMainActivity);

        // Check the results
        Activity activity = mActivityTestRule.getActivity();
        checkTitleStripOrTab(activity, R.string.tab_dictionary);
        checkFirstDefinition("a sweet quick bread baked in a cup-shaped pan");
        Espresso.unregisterIdlingResources(waitForMainActivity);
        waitForMainActivity.destroy();
    }

}
