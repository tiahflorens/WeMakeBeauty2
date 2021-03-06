package com.jnu.dns.tiah.wemakebeauty;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.jnu.dns.tiah.wemakebeauty.others.DBAdapter;

import java.util.Calendar;
import java.util.Date;

public class GCMIntentService extends GCMBaseIntentService {

    Context context;
    public static final int NOTIFICATION_ID = 1;

    public GCMIntentService() {
        super("604471277635");
    }


    private void sendNotification(String due, String brand) {


        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(Long.parseLong(due));

        //알림상태바에 알림을 추가합니다.
        NotificationManager mNotificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);

        // PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
        // mBuilder.setContentIntent(contentIntent);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                this).setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(brand)
                .setStyle(new NotificationCompat.BigTextStyle().bigText("new message"))
                .setContentText(  c.get(Calendar.MONTH) +"-"+c.get(Calendar.DATE) + " 까지 세일해요!");
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private void sendNotification(String due, String brand,int i) {


        //알림상태바에 알림을 추가합니다.



        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(R.mipmap.ic_launcher, due, System.currentTimeMillis());
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notification.defaults = Notification.DEFAULT_SOUND;// | Notification.DEFAULT_VIBRATE ;
        notification.number = 13;

        Intent intent = null;
        // /Intent intent =new Intent(this, DialogView.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //intent.putExtra("id",msg.getFromEmail());
        //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //notification.setLatestEventInfo(this, msg.getFromEmail(), msg.getMsg(), pendingIntent);
        nm.notify(1234, notification);
    }

    @Override
    protected void onError(Context arg0, String arg1) {
        Log.d("tiah", "on error");
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        //push가 들어오면 이 함수를 통해 메세지를 받습니다.
        String brand = intent.getStringExtra("brand");
        String memo =intent.getStringExtra("memo");
        String due=intent.getStringExtra("due");

        DBAdapter db = new DBAdapter(context);
        db.open();
        db.insert(brand,due,memo);
        Log.d("tiah", " onMessage.getmessage:" + brand + " , " + due);

        sendNotification(due,brand);


    }

    @Override
    protected void onRegistered(Context context, String reg_id) {
        Log.d("tiah", reg_id + " onresitered");
    }

    @Override
    protected void onUnregistered(Context arg0, String arg1) {
        Log.d("tiah", "on un register");
    }

}
