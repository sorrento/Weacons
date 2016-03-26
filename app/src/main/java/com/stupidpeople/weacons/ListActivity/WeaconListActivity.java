package com.stupidpeople.weacons.ListActivity;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.stupidpeople.weacons.LogInManagement;
import com.stupidpeople.weacons.R;

import util.myLog;

//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import util.DividerItemDecoration;
//import util.WeaconAdapter;


public class WeaconListActivity extends ActionBarActivity {
    private RecyclerView mRecyclerView;
    private WeaconAdapter adapter;
//    private Intent intentWeb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_weacon_list);

            myLog.add("opening la lista activity", "aut");
            mRecyclerView = new RecyclerView(this);
            mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
            mRecyclerView.hasFixedSize();
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(R.drawable.abc_list_divider_mtrl_alpha)));

            //Fill the list with launched

            adapter = new WeaconAdapter(this, LogInManagement.getActiveWeacons());
            adapter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //TODO make work click over item
//                    WeaconParse we = (WeaconParse) v.getTag();
//
//                    Intent resultIntent;
//
//                    resultIntent = null;
////                    resultIntent = we.getIntent();
//                    WeaconListActivity.this.startActivity(resultIntent);
//                    overridePendingTransition(R.transition.trans_left_in, R.transition.trans_left_out);
                }
            });

            mRecyclerView.setAdapter(adapter);

        } catch (Resources.NotFoundException e) {
            myLog.error(e);
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_weacon_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

