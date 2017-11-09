package com.tonightstudio.sdktestdemo;

import android.app.Application;

import com.tonightstudio.mysdk.CrashHandler;

/**
 * Created by liyiwei
 * on 2017/11/9.
 */

public class MainApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        CrashHandler.getInstance().init(this);
    }
}
