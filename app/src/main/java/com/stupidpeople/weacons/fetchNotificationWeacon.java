package com.stupidpeople.weacons;

import android.os.AsyncTask;

import org.json.JSONException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.concurrent.Executor;

import util.myLog;

/**
 * Created by Milenko on 11/03/2016.
 */
public class fetchNotificationWeacon extends AsyncTask<Void, Void, Connection.Response> {
    private final fetchingResults callback;
    private final String url;

    public fetchNotificationWeacon(String fetchingUrl, fetchingResults callback) {
        this.callback = callback;
        this.url = fetchingUrl;
    }


    @Override
    protected Connection.Response doInBackground(Void... params) {
        Connection.Response response = null;

        try {
            response = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .referrer("http://www.google.com")
                    .timeout(5000)
                    .followRedirects(true)
                    .execute();
        } catch (IOException e) {
            callback.onError(e);
        }

        return response;
    }

    @Override
    protected void onPostExecute(Connection.Response response) {
        super.onPostExecute(response);
        callback.onReceive(response);
    }

}