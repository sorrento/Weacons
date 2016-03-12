package com.stupidpeople.weacons.Advanced;

import android.content.Context;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.SaveCallback;
import com.stupidpeople.weacons.WeaconParse;

import java.util.ArrayList;

import util.myLog;

/**
 * Created by Milenko on 04/03/2016.
 */
public abstract class Chat {

    private static ArrayList<WeaconParse> onChat = new ArrayList<>();
    private static String tag = "chat";

    public static boolean IsInChat(WeaconParse we) {
        return onChat.contains(we);
    }

    /**
     * Convert the bssid to a string appropiate for channel name
     *
     * @param bs
     * @return
     */
    private static String ChannelName(String bs) {
        return "Z_" + bs.replace(":", "_");
    }

    public static void WeChatIn(WeaconParse we) {
        //TODO LogInChat(we)
        myLog.add("Chat In: " + we.getName(), tag);
        onChat.add(we);
    }

    public static void movingOutForChat(WeaconParse we) {
        //TODO     LogOutChat(we)
        onChat.remove(we);
        myLog.add("ChatOut " + we.getName(), tag);

    }

    public static void subscribeAChannel(final String channelName, final Context context) {

        ParsePush.subscribeInBackground(channelName, new SaveCallback() {//The channel must start with a letter
            @Override
            public void done(ParseException e) {
                String text;
                if (e == null) {
                    text = "successfully subscribed to the broadcast channel." + channelName;
                } else {
                    text = "failed to subscribe for push: " + e.getMessage() + " \n" + e;
                }
                myLog.add(text, tag);
                try {
                    Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                } catch (Exception e1) {
                    myLog.add("---error subscribinb cchane: " + e1.getLocalizedMessage(), tag);
                }
            }
        });

    }

    private static void subscribeAChannel(final String channelName) {
//        subscribeAChannel(channelName, mContext); //TODO provide context
    }
}
