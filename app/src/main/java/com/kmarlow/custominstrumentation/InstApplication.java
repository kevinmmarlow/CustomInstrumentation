package com.kmarlow.custominstrumentation;

import android.content.Context;
import android.content.Intent;
import android.support.multidex.MultiDexApplication;

import com.kmarlow.custominstrumentation.sdk.AndromiumControllerService;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;


public class InstApplication extends MultiDexApplication {

    public static RefWatcher getRefWatcher(Context context) {
        InstApplication application = (InstApplication) context.getApplicationContext();
        return application.refWatcher;
    }

    private RefWatcher refWatcher;

    @Override
    public void onCreate() {
        super.onCreate();

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        refWatcher = LeakCanary.install(this);

        Intent intent = new Intent(this, AndromiumControllerService.class);
        startService(intent);
    }
}
