package com.g.laurent.go4lunch.Models;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

    private static callbackAlarm mcallbackAlarm;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(mcallbackAlarm!=null)
            mcallbackAlarm.create_new_list_nearby_places();
    }

    public interface callbackAlarm {
        void create_new_list_nearby_places();
    }

    public void createCallbackAlarm(callbackAlarm mcallbackAlarm){
        AlarmReceiver.mcallbackAlarm = mcallbackAlarm;
    }
}
