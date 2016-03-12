package com.stupidpeople.weacons;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import util.DividerItemDecoration;
//import util.WeaconAdapter;
import util.myLog;


public class WeaconListActivity extends ActionBarActivity {
//    private RecyclerView mRecyclerView;
//    private WeaconAdapter adapter;
//    private Intent intentWeb;

    //TODO uncomment evertything
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_weacon_list);

//            mRecyclerView = new RecyclerView(this);
//            mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
//            mRecyclerView.hasFixedSize();
//            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//            mRecyclerView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(R.drawable.abc_list_divider_mtrl_alpha)));

            //Fill the list with launched

//            adapter = new WeaconAdapter(this, LogInManagement.getActiveWeacons());
//            adapter.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    WeaconParse we = (WeaconParse) v.getTag();
//
//
//                    Intent resultIntent;
//                    resultIntent = getIntent(we);
//                    WeaconListActivity.this.startActivity(resultIntent);
//                    overridePendingTransition(R.transition.trans_left_in, R.transition.trans_left_out);
//                }
//            });

//            mRecyclerView.setAdapter(adapter);
        } catch (Resources.NotFoundException e) {
            myLog.error(e);
        }
    }

    private Intent getIntent(WeaconParse we) {
        Intent intent = null;

//        if (we.isBrowser()) {
//            intent = new Intent(WeaconListActivity.this, BrowserActivity.class)
//                    .putExtra("wUrl", we.getUrl());
//        } else {
//            intent = new Intent(WeaconListActivity.this, CardsActivity.class)
//                    .putExtra("wComapanyDataObId", we.getCompanyDataObjectId())
//                    .putExtra("wCards", we.getCards())
//                    .putExtra("typeOfAiportCard", "Departures")
//                    .putExtra("wWeaconObId", we.getObjectId());
//        }

        //In common
//        intent
//                .putExtra("wName", we.getName())
//                .putExtra("wLogo", we.getLogoRounded());

        return intent;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_weacon_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

