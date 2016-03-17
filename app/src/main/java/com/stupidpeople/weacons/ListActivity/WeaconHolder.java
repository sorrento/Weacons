package com.stupidpeople.weacons.ListActivity;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.stupidpeople.weacons.R;

/**
 * Created by Milenko on 04/06/2015.
 */

public class WeaconHolder extends RecyclerView.ViewHolder {
    protected ImageView thumbnail;
    protected TextView title;
    protected TextView description;

    public WeaconHolder(View view) {
        super(view);
        this.thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
        this.title = (TextView) view.findViewById(R.id.name);
        this.description = (TextView) view.findViewById(R.id.description);
    }
}