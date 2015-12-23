package ru.home.beywer.mobi3.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonObject;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import ru.beywer.home.mobi3.lib.Meet;
import ru.beywer.home.mobi3.lib.MeetPriority;
import ru.home.beywer.mobi3.Constants;

public class AddMeetTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "ADD_MEET_TASK";
    private boolean connectionFailed = false;
    private boolean unauthorized = false;
    private Meet newMeet;
    private Context context;

    public AddMeetTask(Context context, String name, String descr, Calendar start, Calendar end, MeetPriority priority){
        super();
        this.context = context;

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
        Log.d(TAG, "Try to get all meets ");

        String taskId = UUID.randomUUID().toString();

        HttpURLConnection httpConnection = null;
        try {
            //TODO get host
            URL url = new URL(Constants.HOST + Constants.ADD_METT_ADDRESS + taskId);
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.addRequestProperty("Authorization", baseAuthorization(Constants.LOGIN, Constants.PASSWORD));
            httpConnection.setRequestMethod("PUT");
            httpConnection.setDoOutput(true);

            OutputStream out = new BufferedOutputStream(httpConnection.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));

            Log.d(TAG, "Writing to server: " + Constants.gson.toJson(newMeet));
            writer.write(Constants.gson.toJson(newMeet));
            writer.flush();

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

    private String baseAuthorization(String login, String password) throws UnsupportedEncodingException {

        String loginPassword = login+":"+password;
        byte[] encodedBytes = Base64.encode(loginPassword.getBytes(), Base64.DEFAULT);
        String baseAuthorization = "Basic " + new String(encodedBytes, "UTF-8");
        Log.d(TAG, "Created base auth: " + baseAuthorization);

        return baseAuthorization;
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