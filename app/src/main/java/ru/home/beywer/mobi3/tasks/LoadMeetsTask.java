package ru.home.beywer.mobi3.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import ru.beywer.home.mobi3.lib.Meet;
import ru.home.beywer.mobi3.Constants;

public class LoadMeetsTask extends AsyncTask<Void, Void, ArrayList> {

    private static final String TAG = "LOAD_SHORT_TASK";
    private boolean connectionFailed = false;
    private boolean unauthorized = false;
    private Context context;

    public LoadMeetsTask(Context context){
        super();
        this.context = context;
    }

    @Override
    protected ArrayList doInBackground(Void... params) {
        Log.d(TAG, "Try to get all meets ");
        ArrayList<Meet> meets = new ArrayList<>();

        HttpURLConnection httpConnection = null;
        try {
            //TODO get host
            URL url = new URL(Constants.HOST + Constants.ALL_MEETS_ADDRESS);
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.addRequestProperty("Authorization", baseAuthorization(Constants.LOGIN, Constants.PASSWORD));

            Log.d(TAG, "Resp code   " + httpConnection.getResponseMessage());
            if(httpConnection.getResponseCode() == 401){
                unauthorized = true;
            }

            InputStream in = new BufferedInputStream(httpConnection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

            String resp = reader.readLine();
            JsonArray arr = Constants.gson.fromJson(resp, JsonArray.class);
            Log.d(TAG, "get array (str)  " + resp);
            Log.d(TAG, "get array  " + arr);
            for(int  i = 0; i < arr.size(); i++){
                Log.d(TAG,  i+"  " + arr.get(i));
                JsonObject elem = arr.get(i).getAsJsonObject();
                Meet meet = Constants.gson.fromJson(elem.toString(), Meet.class);
                meets.add(meet);
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
        return meets;
    }

    @Override
    protected void onPostExecute(ArrayList arrayList) {
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
}