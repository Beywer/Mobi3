package ru.home.beywer.mobi3;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by Beywer on 21.12.2015.
 */
public class Constants {

    public static final String HOST = "http://192.168.1.3:8080";
    public static final String ALL_MEETS_ADDRESS = "/api/meets/all";
    public static final String ADD_METT_ADDRESS = "/api/meets/";

    public static String LOGIN = "Beywer";
    public static String PASSWORD = "37927";

    public static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();

}
