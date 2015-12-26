package ru.home.beywer.mobi3.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.Preference;
import android.preference.PreferenceManager;
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
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;

import ru.beywer.home.mobi3.lib.Meet;
import ru.home.beywer.mobi3.Constants;

public class LoadMeetsTask extends AsyncTask<Void, Void, ArrayList> {

    private static final String TAG = "LOAD_SHORT_TASK";
    private boolean connectionFailed = false;
    private boolean unauthorized = false;
    private Context context;
    private SharedPreferences mPref;

    public LoadMeetsTask(Context context){
        super();
        this.context = context;
        mPref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    protected ArrayList doInBackground(Void... params) {
        Log.d(TAG, "Try to get all meets ");
        ArrayList<Meet> meets = new ArrayList<>();

        HttpURLConnection httpConnection = null;
        try {
            String host = mPref.getString("host","");
            String login = mPref.getString("login","");
            String password = mPref.getString("password","");

            URL url = new URL(host + Constants.ALL_MEETS_ADDRESS);
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setConnectTimeout(2000);
            httpConnection.addRequestProperty("Authorization", baseAuthorization(login, password));

            Log.d(TAG, "performing request GET:  " + host + Constants.ALL_MEETS_ADDRESS);
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
        } catch (SocketTimeoutException e){
            meets = new ArrayList<>();
            Log.d(TAG, "Timeout. Meets will be empty ", e);
        } catch (MalformedURLException e) {
            Log.d(TAG, "Error trying connect to ", e);
        } catch (IOException e) {
            Log.d(TAG, "While reading json from ", e);
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