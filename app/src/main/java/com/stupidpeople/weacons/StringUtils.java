package com.stupidpeople.weacons;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

/**
 * Created by Milenko on 14/03/2016.
 */
public class StringUtils {
    /**
     * The first m characters are BOLD and slighty bigger
     *
     * @param text
     * @param m
     * @return
     */
    @NonNull
    public static SpannableString getSpannableString(String text, int m) {
        SpannableString span = new SpannableString(text);

        span.setSpan(new ForegroundColorSpan(Color.BLACK), 0, m, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, m, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new RelativeSizeSpan(1.1f), 0, m, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return span;
    }


}
