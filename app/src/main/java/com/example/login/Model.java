package com.example.login;

import android.content.Context;
import android.content.SharedPreferences;

public class Model {
    private static final String PREF_NAME = "UserData";
    private static final int KEY_DARKMODE = 0;
    private static final int KEY_BENACHRICHTIGUNGEN = 0;
    private static final int KEY_LANGUAGE = 0;
    private static final String KEY_SCHLUESSEL = null;
    private static Model instance;
    private boolean darkmode;
    private int benachrichtigungen;
    private int language;
    private String schluessel;

    private Model() {
    }

    public static Model getInstance() {
        if (instance == null) {
            instance = new Model();
        }
        return instance;
    }

    public void save(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("KEY_DARKMODE", this.darkmode);
        editor.putInt("KEY_BENACHRICHTIGUNGEN", benachrichtigungen);
        editor.putInt("KEY_LANGUAGE", language);
        editor.putString("KEY_SCHLUESSEL", schluessel);

        editor.apply();
    }

    public void load(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserData", Context.MODE_PRIVATE);

        this.darkmode = sharedPreferences.getBoolean("KEY_DARKMODE", false);
        benachrichtigungen = sharedPreferences.getInt("KEY_BENACHRICHTIGUNGEN", KEY_BENACHRICHTIGUNGEN);
        language = sharedPreferences.getInt("KEY_LANGUAGE", KEY_LANGUAGE);
        schluessel = sharedPreferences.getString("KEY_SCHLUESSEL", KEY_SCHLUESSEL);
    }

    public void setDarkmode(boolean darkmode) {
        this.darkmode = darkmode;
    }

    public boolean getDarkmode() {
        return this.darkmode;
    }

    public int getBenachrichtigungen() {
        return benachrichtigungen;
    }

    public void setBenachrichtigungen(int benachrichtigungen) {
        this.benachrichtigungen = benachrichtigungen;
    }

    public int getLanguage() {
        return language;
    }

    public void setLanguage(int language) {
        this.language = language;
    }

    public String getSchluessel() {
        return schluessel;
    }

    public void setSchluessel(String schluessel) {
        this.schluessel = schluessel;
    }

}
