package com.stupidpeople.weacons.ListActivity;

/**
 * Created by Milenko on 04/06/2015.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;
import com.stupidpeople.weacons.R;
import com.stupidpeople.weacons.WeaconParse;

import java.util.List;

public class WeaconAdapter extends RecyclerView.Adapter<WeaconHolder> implements View.OnClickListener {

    private List<WeaconParse> weaconItemList;
    private Context mContext;
    private View.OnClickListener listener;

    public WeaconAdapter(Context context, List<WeaconParse> weaconItemList) {
        this.weaconItemList = weaconItemList;
        this.mContext = context;
    }

    @Override
    public WeaconHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_row, null);
        v.setOnClickListener(this);
        WeaconHolder wh = new WeaconHolder(v);

        return wh;
    }

    @Override
    public void onBindViewHolder(WeaconHolder weaconHolder, int i) {
        WeaconParse we = weaconItemList.get(i);

        Picasso.with(mContext).load(we.getImageParseUrl())
                .error(R.drawable.abc_ic_ab_back_mtrl_am_alpha)
                .placeholder(R.mipmap.ic_launcher)
                .into(weaconHolder.thumbnail);

//        weaconHolder.title.setText(Html.fromHtml(weaconItem.getTitle()));
        weaconHolder.title.setText(we.getName());
        weaconHolder.description.setText(we.NotiSingleExpandedContent());
        weaconHolder.itemView.setTag(we);
    }

    @Override
    public int getItemCount() {
        return (null != weaconItemList ? weaconItemList.size() : 0);
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            listener.onClick(v);
        }
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }

}