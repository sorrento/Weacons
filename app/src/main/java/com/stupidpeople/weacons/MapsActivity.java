package com.stupidpeople.weacons;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.stupidpeople.weacons.Helpers.WeaconParse;
import com.stupidpeople.weacons.Location.GPSCoordinates;
import com.stupidpeople.weacons.Location.LocationAsker;
import com.stupidpeople.weacons.Location.LocationCallback;

import java.util.List;

import util.myLog;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private Context mContext;
    private String tag = "MAP";
    private GoogleMap mMap;
    private Marker yo;

    private boolean mapLoaded, freeBusStopsLoaded;
    private List<WeaconParse> paradas;
    private TextView txtParadas;
    private Circle circle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mContext = this;

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        txtParadas = (TextView) findViewById(R.id.txtConquered);

        getNearFreeBusstopsAndDraw();
        getMyBusStops();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mapLoaded = true;

        mMap.setMyLocationEnabled(true);

        if (freeBusStopsLoaded) pintarParadasEnMapa();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMap.clear();
        freeBusStopsLoaded = false;
    }

    private void getMyBusStops() {
        final ParseUser currentUser = ParseUser.getCurrentUser();

        ParseQuery<WeaconParse> q = ParseQuery.getQuery(WeaconParse.class);
        q.whereEqualTo("Owner", currentUser);
        q.countInBackground(new CountCallback() {
            @Override
            public void done(int i, ParseException e) {
                if (e == null) {
                    txtParadas.setText(String.format(getString(R.string.map_conquered_n), i));
                } else {
                    txtParadas.setText("Not possible to connect..");
                    myLog.addToParse("erroren gertmybus. " + e.getLocalizedMessage(), tag);
                }
            }
        });

    }

    private void getNearFreeBusstopsAndDraw() {
        LocationCallback call = new LocationCallback() {

            @Override
            public void NotPossibleToReachAccuracy() {
                final String s = "Not possible to reach accuracy for the map";
                Toast.makeText(MapsActivity.this, s, Toast.LENGTH_SHORT).show();
                myLog.addToParse(s, tag);
            }

            @Override
            public void LocationReceived(GPSCoordinates gps, double accuracy) {

                if (mapLoaded) {
                    LatLng aqui = gps.getLatLng();

                    yo = mMap.addMarker(new MarkerOptions().position(aqui).title("Yo")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                    circle = mMap.addCircle(new CircleOptions()
                            .center(gps.getLatLng())
                            .radius(accuracy)
                            .strokeColor(Color.RED).strokeWidth(3)
                            .fillColor(0x5500ff00));

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(aqui, 15));
                }

                loadParadaslibres(gps);

            }
        };

        new LocationAsker(mContext, call);
    }

    private void loadParadaslibres(GPSCoordinates gps) {
        ParseQuery<WeaconParse> q = ParseQuery.getQuery(WeaconParse.class);
        q.whereNear("GPS", gps.getGeoPoint())
                .whereEqualTo("Type", "bus_stop")
                .whereDoesNotExist("n_scannings")
                .setLimit(40)
                .findInBackground(new FindCallback<WeaconParse>() {
                    @Override
                    public void done(List<WeaconParse> list, ParseException e) {
                        paradas = list;
                        freeBusStopsLoaded = true;

                        if (mapLoaded) pintarParadasEnMapa();
                    }
                });
    }

    private void pintarParadasEnMapa() {
        for (WeaconParse we : paradas) {
            Marker marker = mMap.addMarker(buildMarker(we)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
//                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_map_weacon)));
            marker.setVisible(true);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        getNearFreeBusstopsAndDraw();

    }

    @NonNull
    private MarkerOptions buildMarker(WeaconParse we) {
        ParseGeoPoint gps = we.getGPS();

        return new MarkerOptions().position(new LatLng(gps.getLatitude(), gps.getLongitude())).snippet(we.getParadaId()).title(we.getName());
    }

}
