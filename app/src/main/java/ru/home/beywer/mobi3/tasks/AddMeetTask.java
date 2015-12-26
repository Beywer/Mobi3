package ru.home.beywer.mobi3.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.UUID;

import ru.beywer.home.mobi3.lib.Meet;
import ru.beywer.home.mobi3.lib.MeetPriority;
import ru.beywer.home.mobi3.lib.User;
import ru.home.beywer.mobi3.Constants;

public class AddMeetTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "ADD_MEET_TASK";
    private boolean connectionFailed = false;
    private boolean unauthorized = false;
    private Meet newMeet;
    private Context context;
    private SharedPreferences mPref;

    public AddMeetTask(Context context, String name, String descr, Calendar start, Calendar end, MeetPriority priority){
        super();
        this.context = context;
        mPref = PreferenceManager.getDefaultSharedPreferences(context);

        Meet meet = new Meet();
        meet.setDescription(descr);
        meet.setName(name);
        meet.setStart(start.getTimeInMillis());
        meet.setEnd(end.getTimeInMillis());
        meet.setMeetPriority(priority);
        this.newMeet = meet;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.d(TAG, "Add meet ");

        HttpURLConnection httpConnection = null;
        try {
            String host = mPref.getString("host","");
            String login = mPref.getString("login","");
            String password = mPref.getString("password","");

            User owner = new User();
            owner.setLogin(login);
            newMeet.setOwner(owner);
            newMeet.addParticipant(owner);

            //TODO get host
            String taskId = UUID.randomUUID().toString();
            newMeet.setId(taskId);
            URL url = new URL(host + Constants.MEETS + taskId);
            Log.d(TAG, "Address: " + host + Constants.MEETS + taskId);
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.addRequestProperty("Authorization", HttpUtil.baseAuthorization(login, password));
            httpConnection.setRequestMethod("PUT");
            httpConnection.setDoOutput(true);

            OutputStream out = new BufferedOutputStream(httpConnection.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));

            Log.d(TAG, "Writing to server: " + Constants.gson.toJson(newMeet));
            writer.write(Constants.gson.toJson(newMeet));
            writer.flush();

            Log.d(TAG, "Resp   " + httpConnection.getResponseMessage());
            Log.d(TAG, "Resp code   " + httpConnection.getResponseMessage());
            if(httpConnection.getResponseCode() == 401){
                unauthorized = true;
            }

        } catch (FileNotFoundException e){
            Log.d(TAG, "FileNotFoundException", e);
        } catch (ConnectException e){
            connectionFailed = true;
            Log.d(TAG, "Connection exception", e);
        } catch (MalformedURLException e) {
            Log.d(TAG, "Error trying connect to " + "http://www.android.com/", e);
        } catch (IOException e) {
            Log.d(TAG, "While reading json from " + "http://www.android.com/", e);
        }finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
        return null;
    }

    @Override
    protected void onCancelled() {
        if(connectionFailed){
            Toast
                    .makeText(context, "Проблемы с соединением", Toast.LENGTH_SHORT)
                    .show();
        }
        if(unauthorized){
            Toast
                    .makeText(context, "Ошибка авторизации", Toast.LENGTH_SHORT)
                    .show();
        }
    }

//    private void displayMessage(final String mess){
//        final Handler mHandler = new Handler();
//        final Runnable mUpdateResults = new Runnable() {
//            public void run() {
//                new Toast(this, mess, Toast.LENGTH_SHORT).show();
//            }
//        };
//
//        new Thread() {
//            public void run() {
//                mHandler.post(mUpdateResults);
//            }
//        }.start();
//    }
}