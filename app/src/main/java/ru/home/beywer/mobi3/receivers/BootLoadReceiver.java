package ru.home.beywer.mobi3.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import ru.home.beywer.mobi3.Constants;
import ru.home.beywer.mobi3.DownloadService;
import ru.home.beywer.mobi3.activites.MainActivity;

public class BootLoadReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent2 = new Intent(context, BootLoadReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent2, 0);
        AlarmManager am =(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
        am.set(AlarmManager.RTC, System.currentTimeMillis()+ Constants.UPDATE_INTERVAL, pi);


        Intent downloadIntent = new Intent(context, DownloadService.class);
        downloadIntent.putExtra(MainActivity.REQ_TYPE, "load");
        context.startService(downloadIntent);
        Log.d("BootLoadReceiver", "sended query for update from  BootLoadReceiver");
    }
}

