package com.beckytech.lammummaakutaa8ffaa;

import android.app.Application;

import com.beckytech.lammummaakutaa8ffaa.service.AdManagerHelper;
import com.beckytech.lammummaakutaa8ffaa.service.AppOpenAdManager;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AdManagerHelper.initialize(this);
        new AppOpenAdManager(this);
    }
}
