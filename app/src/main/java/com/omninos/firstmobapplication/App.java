package com.omninos.firstmobapplication;

import android.app.Application;
import android.content.Context;

/**
 * Created by Manjinder Singh on 15 , November , 2019
 */
public class App extends Application {
    Context context;
    public static Singlton singlton;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        singlton = new Singlton();
    }

    public static Singlton getSinglton() {
        return singlton;
    }

    public static void setSinglton(Singlton singlton) {
        App.singlton = singlton;
    }
}
