package ru.home.beywer.mobi3;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import ru.home.beywer.mobi3.activites.MainActivity;

public class DownloadService extends Service {

    final String LOG_TAG = "SMservice";

    public void onCreate() {
//        super.onCreate();

        Intent intent = new Intent(MainActivity.BROADCAST_ACTION);
        // сообщаем об старте задачи
        intent.putExtra("AZA", 3);
        intent.putExtra("AZA2", 4);
        sendBroadcast(intent);

        Log.d(LOG_TAG, "onCreate");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");
        someTask();
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        super.onDestroy();
    }

    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return null;
    }

    void someTask() {
    }
}