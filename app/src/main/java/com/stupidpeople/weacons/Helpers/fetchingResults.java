package com.stupidpeople.weacons.Helpers;

import org.jsoup.Connection;

/**
 * Created by Milenko on 11/03/2016.
 */
interface fetchingResults {
    void onReceive(Connection.Response response);

    void onError(Exception e);

    void OnEmptyAnswer();
}
