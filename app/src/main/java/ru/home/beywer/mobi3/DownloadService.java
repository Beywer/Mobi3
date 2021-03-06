package ru.home.beywer.mobi3;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import ru.beywer.home.mobi3.lib.Meet;
import ru.home.beywer.mobi3.activites.MainActivity;
import ru.home.beywer.mobi3.tasks.LoadMeetsTask;

public class DownloadService extends Service {

    private static final String TAG = "DownloadService";
    private static ArrayList<Meet> meets = new ArrayList<>();

    public void onCreate() {
        Log.d(TAG, "onCreate");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        String type = intent.getStringExtra(MainActivity.REQ_TYPE);
        switch (type){
            case "load":
                boolean needNotif = intent.getBooleanExtra("needNotif", true);
                load(needNotif);
                break;
            case "get":
                sendResult();
                break;
        }

        stopSelf(startId);
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return null;
    }


    private void load(final boolean needNotif){
        final Thread tr = new Thread(new Runnable() {
            @Override
            public void run() {
                LoadMeetsTask task = new LoadMeetsTask(DownloadService.this);
                task.execute();
                try {
                    ArrayList<Meet> loadedMeets = task.get();

                    Log.d(TAG, "Main activity is shown " + MainActivity.shown);
                    Log.d(TAG, "needNotif extra " + needNotif);
                    Log.d(TAG, "Founded new meets " + (loadedMeets.size() > meets.size()));
                    if(!MainActivity.shown && needNotif && loadedMeets.size() > meets.size()) {
                        Log.d(TAG, "Need notify");
                    } else Log.d(TAG, "No need notify");
                    if(!MainActivity.shown && needNotif && loadedMeets.size() > meets.size()) {
                        sendNotif();
                    }

                    meets.clear();
                    meets.addAll(loadedMeets);
                    if(MainActivity.needResult){
                        sendResult();
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
        tr.start();
        Toast.makeText(this, "Load " + meets.size(), Toast.LENGTH_SHORT).show();
    }

    private void sendResult(){
        Intent intent1 = new Intent(MainActivity.BROADCAST_ACTION);
        intent1.putExtra(MainActivity.REQ_TYPE, "result");
        intent1.putExtra(MainActivity.MEETS, meets);
        sendBroadcast(intent1);
    }

    void sendNotif() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.not_icon)
                        .setContentTitle("Новые встречи")
                        .setAutoCancel(true)
                        .setContentText("Были обнаружены новые встречи!");

        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(0, mBuilder.build());
    }
}