package com.example.latur_application.volly;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

public class AppLifecycleObserver implements LifecycleObserver {

    public static boolean isAppVisible;
    public static final String TAG = AppLifecycleObserver.class.getName();

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onEnterForeground() {
        isAppVisible = true;
        //Log.e("APP", "isAppVisible: "+isAppVisible);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onEnterBackground() {
        isAppVisible = false;
       // Log.e("APP", "isAppVisible: "+isAppVisible);
    }

}