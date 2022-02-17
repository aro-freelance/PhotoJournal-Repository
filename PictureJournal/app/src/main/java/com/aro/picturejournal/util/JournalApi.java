package com.aro.picturejournal.util;

import android.app.Application;

public class JournalApi extends Application {

    /*
    this handles storing data globally
     */

    private String username;
    private String userId;


    private static JournalApi instance;

    public JournalApi(){}

    //singleton
    public static JournalApi getInstance(){
        if(instance == null){
            instance = new JournalApi();
        }
        return instance;
    }



    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
