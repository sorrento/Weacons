package com.stupidpeople.weacons.Notifications;

/**
 * Created by Milenko on 18/05/2016.
 */
public class NotifFeatures {
    public boolean sound;
    public boolean refreshButton;
    public boolean silenceButton;

    /**
     * Gets and stores the features of the next notification
     *
     * @param sound
     * @param refreshBtn
     * @param silenceBtn
     */
    public NotifFeatures(boolean sound, boolean refreshBtn, boolean silenceBtn) {
        refreshButton = refreshBtn;
        silenceButton = silenceBtn;
        this.sound = sound;
//        this.automaticFetching = automaticFetching;
    }
}
