package com.stupidpeople.weacons.Helpers;

import android.text.SpannableString;

/**
 * Created by Milenko on 06/06/2016.
 */
public interface fetchableElement {
    SpannableString oneLineSummary();

    /**
     * lonsg description for the list activity
     *
     * @return
     */
    SpannableString getLongSpan();

    /**
     * Take a few chareacters of each feachet item to summarize them all
     * For instace: L1:5m | L2:3m
     *
     * @return
     */
    String veryShortSummary();

}
