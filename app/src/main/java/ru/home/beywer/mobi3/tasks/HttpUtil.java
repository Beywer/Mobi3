package ru.home.beywer.mobi3.tasks;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import ru.beywer.home.mobi3.lib.Meet;
import ru.home.beywer.mobi3.Constants;
import ru.home.beywer.mobi3.activites.MainActivity;

public class HttpUtil {

    private static final String TAG = "HTTP_UTILL";
    private static SimpleDateFormat sdf = new SimpleDateFormat("dd/M/yyyy");

    public static void updateTask(final JsonObject message, final String name, Context context){
        Log.d(TAG, "Update meet " + name);
        final SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(context);

        new Thread(new Runnable() {
            @Override
            public void run() {

                HttpURLConnection httpConnection = null;
                try {
                    String host = mPref.getString("host","");
                    String login = mPref.getString("login","");
                    String password = mPref.getString("password","");

                    URL url = new URL(host + Constants.MEETS + name);
                    httpConnection = (HttpURLConnection) url.openConnection();
                    httpConnection.setConnectTimeout(2000);
                    httpConnection.addRequestProperty("Authorization", baseAuthorization(login, password));
                    httpConnection.setRequestMethod("POST");
                    httpConnection.setDoOutput(true);

                    OutputStream out = new BufferedOutputStream(httpConnection.getOutputStream());
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));

                    Log.d(TAG, "Writing to server: " + message.toString());
                    writer.write(message.toString());
                    writer.flush();

                    Log.d(TAG, "Resp code   " + httpConnection.getResponseMessage());
//                    if(httpConnection.getResponseCode() == 401){
//                        unauthorized = true;
//                    }

                } catch (FileNotFoundException e){
                    Log.d(TAG, "FileNotFoundException", e);
                } catch (ConnectException e){
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
            }
        }).start();
    }


    public static void deleteTask(final String name, Context context){
        Log.d(TAG, "Update meet " + name);
        final SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(context);

        new Thread(new Runnable() {
            @Override
            public void run() {

                HttpURLConnection httpConnection = null;
                try {
                    String host = mPref.getString("host","");
                    String login = mPref.getString("login","");
                    String password = mPref.getString("password","");

                    URL url = new URL(host + Constants.MEETS + name);
                    httpConnection = (HttpURLConnection) url.openConnection();
                    httpConnection.setConnectTimeout(2000);
                    httpConnection.addRequestProperty("Authorization", baseAuthorization(login, password));
                    httpConnection.setRequestMethod("DELETE");

                    Log.d(TAG, "Resp code   " + httpConnection.getResponseMessage());

                } catch (FileNotFoundException e){
                    Log.d(TAG, "FileNotFoundException", e);
                } catch (ConnectException e){
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
            }
        }).start();
    }

    public static void getAll(final Calendar from, final Calendar to, final Context context){
        Log.d(TAG, "Try to get all meets from "+from+"  to  " + to );
        final SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(context);

        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<Meet> meets = new ArrayList<>();
                HttpURLConnection httpConnection = null;
                try {
                    String host = mPref.getString("host", "");
                    String login = mPref.getString("login", "");
                    String password = mPref.getString("password", "");

                    String start = URLEncoder.encode(sdf.format(from.getTime()), "UTF-8");
                    String end = URLEncoder.encode(sdf.format(to.getTime()), "UTF-8");
                    String address = host + Constants.ALL_MEETS_ADDRESS + "?from=" + start + "&to=" + end;

                    URL url = new URL(address);
                    httpConnection = (HttpURLConnection) url.openConnection();
                    httpConnection.setConnectTimeout(2000);
                    httpConnection.addRequestProperty("Authorization", baseAuthorization(login, password));

                    Log.d(TAG, "performing request GET:  " + address);
                    Log.d(TAG, "Resp code   " + httpConnection.getResponseMessage());

                    InputStream in = new BufferedInputStream(httpConnection.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

                    String resp = reader.readLine();
                    JsonArray arr = Constants.gson.fromJson(resp, JsonArray.class);
                    Log.d(TAG, "get array (str)  " + resp);
                    Log.d(TAG, "get array  " + arr);
                    for (int i = 0; i < arr.size(); i++) {
                        Log.d(TAG, i + "  " + arr.get(i));
                        JsonObject elem = arr.get(i).getAsJsonObject();
                        Meet meet = Constants.gson.fromJson(elem.toString(), Meet.class);
                        meets.add(meet);
                    }

                } catch (FileNotFoundException e) {
                    Log.d(TAG, "FileNotFoundException", e);
                } catch (ConnectException e) {
                    Log.d(TAG, "Connection exception", e);
                } catch (SocketTimeoutException e) {
                    meets = new ArrayList<>();
                    Log.d(TAG, "Timeout. Meets will be empty ", e);
                } catch (MalformedURLException e) {
                    Log.d(TAG, "Error trying connect to ", e);
                } catch (IOException e) {
                    Log.d(TAG, "While reading json from ", e);
                } finally {
                    if (httpConnection != null) {
                        httpConnection.disconnect();
                    }
                }
                Intent intent1 = new Intent(MainActivity.BROADCAST_ACTION);
                intent1.putExtra(MainActivity.REQ_TYPE, "result");
                intent1.putExtra(MainActivity.MEETS, meets);
                context.sendBroadcast(intent1);
            }
        }).start();
    }

    public static String baseAuthorization(String login, String password) throws UnsupportedEncodingException {

        String loginPassword = login+":"+password;
        byte[] encodedBytes = Base64.encode(loginPassword.getBytes(), Base64.DEFAULT);
        String baseAuthorization = "Basic " + new String(encodedBytes, "UTF-8");
        Log.d(TAG, "Created base auth: " + baseAuthorization);

        return baseAuthorization;
    }

    public static void getAll(final String str, final Context context) {
        Log.d(TAG, "Try to get all meets with desct "+str);
        final SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(context);

        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<Meet> meets = new ArrayList<>();
                HttpURLConnection httpConnection = null;
                try {
                    String host = mPref.getString("host", "");
                    String login = mPref.getString("login", "");
                    String password = mPref.getString("password", "");

                    String address = host + Constants.ALL_MEETS_ADDRESS + "?search="+ URLEncoder.encode(str, "UTF-8");

                    URL url = new URL(address);
                    httpConnection = (HttpURLConnection) url.openConnection();
                    httpConnection.setConnectTimeout(2000);
                    httpConnection.addRequestProperty("Authorization", baseAuthorization(login, password));

                    Log.d(TAG, "performing request GET:  " + address + "\n" + url.toString());
                    Log.d(TAG, "Resp code   " + httpConnection.getResponseMessage());

                    InputStream in = new BufferedInputStream(httpConnection.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

                    String resp = reader.readLine();
                    JsonArray arr = Constants.gson.fromJson(resp, JsonArray.class);
                    Log.d(TAG, "get array (str)  " + resp);
                    Log.d(TAG, "get array  " + arr);
                    for (int i = 0; i < arr.size(); i++) {
                        Log.d(TAG, i + "  " + arr.get(i));
                        JsonObject elem = arr.get(i).getAsJsonObject();
                        Meet meet = Constants.gson.fromJson(elem.toString(), Meet.class);
                        meets.add(meet);
                    }

                } catch (FileNotFoundException e) {
                    Log.d(TAG, "FileNotFoundException", e);
                } catch (ConnectException e) {
                    Log.d(TAG, "Connection exception", e);
                } catch (SocketTimeoutException e) {
                    meets = new ArrayList<>();
                    Log.d(TAG, "Timeout. Meets will be empty ", e);
                } catch (MalformedURLException e) {
                    Log.d(TAG, "Error trying connect to ", e);
                } catch (IOException e) {
                    Log.d(TAG, "While reading json from ", e);
                } finally {
                    if (httpConnection != null) {
                        httpConnection.disconnect();
                    }
                }
                Intent intent1 = new Intent(MainActivity.BROADCAST_ACTION);
                intent1.putExtra(MainActivity.REQ_TYPE, "result");
                intent1.putExtra(MainActivity.MEETS, meets);
                context.sendBroadcast(intent1);
            }
        }).start();
    }

    public static Meet get(final String id, final Context context){
        Meet result = null;
        Log.d(TAG, "Try to get meet with id " + id);
        final SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(context);

        HttpURLConnection httpConnection = null;
        try {
            String host = mPref.getString("host", "");
            String login = mPref.getString("login", "");
            String password = mPref.getString("password", "");

            String address = host + Constants.MEETS+id;

            URL url = new URL(address);
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setConnectTimeout(2000);
            httpConnection.addRequestProperty("Authorization", baseAuthorization(login, password));

            Log.d(TAG, "performing request GET:  " + address + "\n" + url.toString());
            Log.d(TAG, "Resp code   " + httpConnection.getResponseMessage());

            InputStream in = new BufferedInputStream(httpConnection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

            String resp = reader.readLine();
            result = Constants.gson.fromJson(resp, Meet.class);
            Log.d(TAG, "get meet(json)  " + resp);
            Log.d(TAG, "get meet  " + result);

        } catch (FileNotFoundException e) {
            Log.d(TAG, "FileNotFoundException", e);
        } catch (ConnectException e) {
            Log.d(TAG, "Connection exception", e);
        } catch (SocketTimeoutException e) {
            Log.d(TAG, "Timeout. Meets will be empty ", e);
        } catch (MalformedURLException e) {
            Log.d(TAG, "Error trying connect to ", e);
        } catch (IOException e) {
            Log.d(TAG, "While reading json from ", e);
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
        return result;
    }


}
