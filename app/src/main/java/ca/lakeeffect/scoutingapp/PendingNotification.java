package ca.lakeeffect.scoutingapp;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import java.util.Random;


public class PendingNotification extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        send(context);
    }

    public void send(Context context) {


        System.out.println("Sending notification");

        String message;

        //TODO get the amount of pending messages

        if(context.getSharedPreferences("pendingmessages", context.MODE_PRIVATE).getInt("messageAmount", -1) <= 0){
            return;
        }

        message = "You have "+ context.getSharedPreferences("pendingmessages", context.MODE_PRIVATE).getInt("messageAmount", -1) +" unsent messages. Contact Ajay to get the data into the database ASAP!";

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


        NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle()
                .setBigContentTitle("Unsent Data!")
                .bigText(message);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.mipmap.ic_launcher))
                .setContentTitle("Unsent Data!")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVibrate(new long[]{1000,1000,1000,1000,1000})
                .setStyle(style);
        //TODO possibly change the ID so that multiple notifications can exist at the same time
        notificationManager.notify(1, notification.build());
    }

}
