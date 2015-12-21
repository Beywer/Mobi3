package ru.home.beywer.mobi3;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import ru.beywer.home.mobi3.lib.Meet;
import ru.home.beywer.mobi3.constants.Constants;

public class LoadMeetsTask extends AsyncTask<Void, Void, ArrayList> {

    private static final String TAG = "LOAD_SHORT_TASK";

    @Override
    protected ArrayList doInBackground(Void... params) {
        Log.d(TAG, "doInBackground started");
        ArrayList<Meet> meets = new ArrayList<>();

        HttpURLConnection httpConnection = null;
        try {
            URL url = new URL(Constants.ALL_MEETS_ADDRESS);
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.addRequestProperty("Authorization", baseAuthorization(Constants.LOGIN, Constants.PASSWORD));
            InputStream in = new BufferedInputStream(httpConnection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

            JsonArray arr = Constants.gson.fromJson(reader.readLine(), JsonArray.class);
            for(int  i = 0; i < arr.size(); i++){
                JsonObject elem = arr.get(i).getAsJsonObject();
                Meet meet = Constants.gson.fromJson(elem.toString(), Meet.class);
                meets.add(meet);
            }

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
        super.onPostExecute(arrayList);
    }

    private String baseAuthorization(String login, String password) throws UnsupportedEncodingException {

        String loginPassword = login+":"+password;
        byte[] encodedBytes = Base64.encode(loginPassword.getBytes(), Base64.DEFAULT);
        String baseAuthorization = "Basic " + new String(encodedBytes, "UTF-8");
        Log.d(TAG, "Created base auth: " + baseAuthorization);

        return baseAuthorization;
    }
}
