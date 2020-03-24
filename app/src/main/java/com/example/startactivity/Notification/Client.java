package com.example.startactivity.Notification;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Client {

    private  static Retrofit retrofit=null;
    public static Retrofit getInstance(String uri){
        if (retrofit==null){
            retrofit=new Retrofit.Builder()
                    .baseUrl(uri)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
