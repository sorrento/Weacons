package com.stupidpeople.weacons.ListActivity;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import com.stupidpeople.weacons.R;

/**
 * Created by Milenko on 24/09/2015.
 */
public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    private Drawable mDivider;

    public DividerItemDecoration(Context context, AttributeSet attrs) {
        final TypedArray a = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.listDivider});
        mDivider = a.getDrawable(0);
        a.recycle();
    }

    public DividerItemDecoration(Context context, Drawable divider) {
//        mDivider = divider;

//        mDivider = divider.setColorFilter( 0xffff0000, PorterDuff.Mode.MULTIPLY );
//        mDivider = divider.setColorFilter(Color.parseColor("#AE6118"));
        divider.setColorFilter(ContextCompat.getColor(context, R.color.colorDivider), PorterDuff.Mode.MULTIPLY);
        mDivider = divider;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (mDivider == null) return;
        if (parent.getChildPosition(view) < 1) return;

        if (getOrientation(parent) == LinearLayoutManager.VERTICAL)
            outRect.top = mDivider.getIntrinsicHeight();
        else outRect.left = mDivider.getIntrinsicWidth();
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent) {
        if (mDivider == null) {
            super.onDrawOver(c, parent);
            return;
        }

        if (getOrientation(parent) == LinearLayoutManager.VERTICAL) {
            final int left = parent.getPaddingLeft();
            final int right = parent.getWidth() - parent.getPaddingRight();
            final int childCount = parent.getChildCount();

            for (int i = 1; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                final int size = mDivider.getIntrinsicHeight();
                final int top = child.getTop() - params.topMargin;
                final int bottom = top + size;
                mDivider.setBounds(left + 80, top, right, bottom);
                mDivider.draw(c);
            }
        } else { //horizontal
            final int top = parent.getPaddingTop();
            final int bottom = parent.getHeight() - parent.getPaddingBottom();
            final int childCount = parent.getChildCount();

            for (int i = 1; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                final int size = mDivider.getIntrinsicWidth();
                final int left = child.getLeft() - params.leftMargin;
                final int right = left + size;
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }
    }

    private int getOrientation(RecyclerView parent) {
        if (parent.getLayoutManager() instanceof LinearLayoutManager) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) parent.getLayoutManager();
            return layoutManager.getOrientation();
        } else
            throw new IllegalStateException("DividerItemDecoration can only be used with a LinearLayoutManager.");
    }

}
