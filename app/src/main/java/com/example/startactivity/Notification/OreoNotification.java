package com.example.startactivity.Notification;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

public class OreoNotification extends ContextWrapper {

    private static final String CHANNEL_ID="com.example.chatapp";
    private static final String CHANNEL_NAME="chatapp";

    private NotificationManager notificationManager;
    public OreoNotification(Context base) {
        super(base);

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            creatChannel();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void creatChannel() {

        NotificationChannel notificationChannel= new NotificationChannel(CHANNEL_ID,CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT);

        notificationChannel.enableLights(false);
        notificationChannel.enableVibration(true);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getNotificationManager().createNotificationChannel(notificationChannel);

    }

    public  NotificationManager getNotificationManager(){
        if (notificationManager==null){
            notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public Notification.Builder getOreoNotification(String title, String body, PendingIntent pendingIntent

    , Uri sounduri,String icon){

        return new Notification.Builder(getApplicationContext(),CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(Integer.parseInt(icon))
                .setSound(sounduri)
                .setAutoCancel(true);

    }










}
