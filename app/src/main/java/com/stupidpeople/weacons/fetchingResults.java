package com.stupidpeople.weacons;

import org.jsoup.Connection;

import java.util.List;

/**
 * Created by Milenko on 11/03/2016.
 */
public interface fetchingResults {
    void onReceive(Connection.Response response);

    void onError(Exception e);
}
