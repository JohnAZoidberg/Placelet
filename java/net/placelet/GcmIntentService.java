package net.placelet;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.squareup.picasso.Picasso;

import net.placelet.connection.User;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;

    public GcmIntentService() {
        super("GcmIntentService");
    }
    public static final String TAG = "Push Notifications";

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
            System.out.println(extras.toString());
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                //sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                //sendNotification("Deleted messages on server: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                handleNotification(extras);
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void handleNotification(Bundle extras) {
        SharedPreferences prefs = getSharedPreferences("net.placelet", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String title = "Placelet";
        String content = "";
        Intent intent = new Intent(this, MainActivity.class);
        String type = extras.getString("type");
        NotificationCompat.Style style = null;
        int id = (int) System.currentTimeMillis();

        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.ic_launcher)
            .setDefaults(Notification.DEFAULT_SOUND)
            .setAutoCancel(true)
            .setVibrate(new long[]{100, 200, 100, 200, 100, 500});

        if(type.equals("message")) {
            id = -1;
            String sender = extras.getString("sender");
            String messageContent = extras.getString("content");

            Set<String> messageSet =  prefs.getStringSet("notifMessages", null);
            if(messageSet == null || messageSet.size() == 0) {
                title = sender;
                content = messageContent;
                intent.putExtra("MessagePush", title);
                style = new NotificationCompat.BigTextStyle().bigText(content);
                try {
                    mBuilder.setLargeIcon(Picasso.with(this).load("http://placelet.de/pictures/profiles/" + extras.getString("senderID") + ".jpg").get());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                messageSet = new LinkedHashSet<String>();
                messageSet.add(sender + messageContent);
            }else {
                messageSet.add(sender + messageContent);
                intent.putExtra("MessagePush", User.NOT_LOGGED_IN);
                title = "Placelet";
                content = messageSet.size() + " neue Nachricht" + (messageSet.size() == 1 ? "" : "en");
                NotificationCompat.InboxStyle testStyle = new NotificationCompat.InboxStyle();
                for(String message : messageSet) {
                    testStyle.addLine(message);
                }
                testStyle.setSummaryText(content);
                style = testStyle;
            }
            editor.putStringSet("notifMessages", messageSet);
            editor.apply();
        }else if(type.equals("pic")) {
            title = getString(R.string.new_pic);
            String picid = extras.getString("picid");
            content = extras.getString("braceName") + " " + getString(R.string.by) + " " + extras.getString("uploader");
            intent.putExtra("PicturePush", extras.getString("brid"));

            try {
                style = new NotificationCompat.BigPictureStyle()
                        .bigPicture(Picasso.with(this).load("http://placelet.de/pictures/bracelets/thumb-" + picid + ".jpg").get())
                        .setBigContentTitle(content);
            } catch (IOException e) {
                style = new NotificationCompat.BigTextStyle().bigText(content);
            }
        }else if(type.equals("comment")) {
            title = getString(R.string.comment);
            if(!extras.getString("commenter").equals("false")) title += " " + getString(R.string.by) + " " + extras.getString("commenter");
            content = extras.getString("comment");
            intent.putExtra("PicturePush", extras.getString("brid"));
            style = new NotificationCompat.BigTextStyle().bigText(content);
        }
        PendingIntent contentIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder
            .setContentIntent(contentIntent)
            .setContentTitle(title)
            .setStyle(style)
            .setContentText(content);
        mNotificationManager.notify(id, mBuilder.build());
    }
}