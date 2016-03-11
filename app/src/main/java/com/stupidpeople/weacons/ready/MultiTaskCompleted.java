package com.stupidpeople.weacons.ready;

/**
 * Created by Milenko on 21/01/2016.
 */
public interface MultiTaskCompleted {
    int i = 0;

    void OneTaskCompleted();

    void OnError(Exception e);
}
