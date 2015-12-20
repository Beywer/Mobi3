package ru.home.beywer.mobi3;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class LoadShortMeetsTask extends AsyncTask<URL, Void, ArrayList> {

    private static final String TAG = "LOAD_SHORT_TASK";

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected ArrayList doInBackground(URL... params) {
        ArrayList<String> meets = new ArrayList<>();
        Log.d(TAG, "doInBackground list created " + meets);

        URL url = null;
        HttpURLConnection httpConnection = null;
        if(params.length > 0){
            url = params[0];
        } else {
            return meets;
        }
        try {
            Log.d(TAG, String.format("New url %s", url));
            httpConnection = (HttpURLConnection) url.openConnection();
            Log.d(TAG, String.format("Opened urlConnection %s", httpConnection));
            InputStream in = new BufferedInputStream(httpConnection.getInputStream());
            Log.d(TAG, String.format("Opened InputStream %s", in));

            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            Log.d(TAG, String.format("Crated BufferedReader %s", reader));

            Log.d(TAG, reader.readLine());

        } catch (MalformedURLException e) {
            Log.d(TAG, "Error trying connect to " + "http://www.android.com/", e);
        } catch (IOException e) {
            Log.d(TAG, "While reading json from " + "http://www.android.com/", e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(ArrayList arrayList) {
        super.onPostExecute(arrayList);
    }
}
