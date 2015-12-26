package ru.home.beywer.mobi3;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Constants {

    public static final String ALL_MEETS_ADDRESS = "/api/meets/all";
    public static final String MEETS = "/api/meets/";

    public static long UPDATE_INTERVAL = 5000;

    public static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();

}
