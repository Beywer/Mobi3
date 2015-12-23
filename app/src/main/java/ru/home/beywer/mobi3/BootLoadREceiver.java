package ru.home.beywer.mobi3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ru.home.beywer.mobi3.activites.MainActivity;

public class BootLoadREceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startServiceIntent = new Intent(context, DownloadService.class);
        startServiceIntent.putExtra(MainActivity.REQ_TYPE, "load");
        context.startService(startServiceIntent);
    }
}

