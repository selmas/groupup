package ch.epfl.sweng.groupup.settings;

import android.support.test.espresso.contrib.BuildConfig;
import android.support.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;

import ch.epfl.sweng.groupup.activity.settings.SettingsActivity;

public class SettingsTest {

    @Rule
    public ActivityTestRule<SettingsActivity> mActivityRule = new ActivityTestRule<>(
            SettingsActivity.class);

    @Test
    public void launchedWithoutErrors(){
        if (BuildConfig.DEBUG){
            throw new AssertionError();
        }
    }

}
