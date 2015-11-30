package com.github.pengrad.mapsplaces;

import android.app.Activity;

/**
 * Stas Parshin
 * 01 December 2015
 */
public class AnimationUtils {

    public static void animateTransition(Activity activity) {
        activity.overridePendingTransition(R.anim.activity_transition_enter, R.anim.activity_transition_exit);
    }

}
