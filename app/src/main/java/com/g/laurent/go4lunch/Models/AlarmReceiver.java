package com.g.laurent.go4lunch.Models;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import com.g.laurent.go4lunch.R;
import com.g.laurent.go4lunch.Utils.FirebaseRecover;

import java.util.List;

public class AlarmReceiver extends BroadcastReceiver implements Callback_alarm {

    private Context mContext;
    private String user_id;
    private String place_id;
    private String name_resto;
    private String address_resto;
    private int number_mates_joining;
    private StringBuilder workmates_joining;
    private static final String EXTRA_USER_ID = "user_id_alarm";

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext=context;
        Callback_alarm callback = this;
        if(intent!=null){
            if(intent.getExtras()!=null)
                user_id=intent.getExtras().getString(EXTRA_USER_ID,null);
        }

        FirebaseRecover firebase_recover = new FirebaseRecover(mContext, callback);
        firebase_recover.recover_list_workmates();
    }

    private void find_data_on_resto_chosen(List<Workmate> workmateList){
        for (Workmate user : workmateList){
            if(user!=null){
                if(user.getId().equals(user_id)){ // if is the current user
                    place_id = user.getResto_id();
                    name_resto = user.getResto_name();
                    address_resto = user.getResto_address();
                }
            }
        }
    }

    private void find_users_joining_resto_chosen(List<Workmate> workmateList){

        number_mates_joining=0;
        workmates_joining = new StringBuilder();

        for (Workmate user : workmateList){
            if(user!=null){
                if(!user.getId().equals(user_id)){ // if is NOT the current user

                    if(user.getResto_id()!=null){

                        if(user.getResto_id().equals(place_id)){ // if the workmate has chosen the same resto as the user
                            workmates_joining.append(user.getName());
                            workmates_joining.append(",");
                            number_mates_joining++;
                        }
                    }
                }
            }
        }
    }

    private String define_text_notification(){

        String text_notif;

        if(number_mates_joining>0){ // if at least one workmate joining
            text_notif=mContext.getResources().getString(R.string.you_lunch) + " " + name_resto + " " +
                    mContext.getResources().getString(R.string.at_the_address) + " " + address_resto + " " +
                    mContext.getResources().getString(R.string.with) + " " + workmates_joining.toString();

        } else {  // if the user will be alone ...
            text_notif=mContext.getResources().getString(R.string.you_lunch) + " " + name_resto + " " +
                    mContext.getResources().getString(R.string.at_the_address) + " " + address_resto;
        }

        return text_notif;
    }

    public void create_and_send_notification(String text_notif){

        String title_notif = "Your lunch";

        String CHANNEL_ID = "my_channel_02";

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            CharSequence name = "my_channel";
            String Description = "This is my channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(mChannel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text_notif))
                .setContentTitle(title_notif)
                .setContentText(text_notif);

        if (notificationManager != null)
            notificationManager.notify(1, builder.build());
    }

    @Override
    public void send_notification(List<Workmate> workmateList){

        if(workmateList!=null) {

            // Recover the place_id, name and address of the resto selected by the user
            find_data_on_resto_chosen(workmateList);

            // Get the number and names of workmates coming with the user to the restaurant
            find_users_joining_resto_chosen(workmateList);

            // Create the text of the notification
            String text_notif = define_text_notification();

            // send notification
            create_and_send_notification(text_notif);
        }
    }

}
