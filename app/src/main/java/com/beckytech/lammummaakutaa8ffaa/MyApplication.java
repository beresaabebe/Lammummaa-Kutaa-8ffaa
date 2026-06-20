package com.beckytech.lammummaakutaa8ffaa;

import android.app.Application;
import android.content.Context;

import com.beckytech.lammummaakutaa8ffaa.service.AdManagerHelper;
import com.beckytech.lammummaakutaa8ffaa.service.AppOpenAdManager;
import com.beckytech.lammummaakutaa8ffaa.service.LocaleHelper;

public class MyApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "en"));
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}

