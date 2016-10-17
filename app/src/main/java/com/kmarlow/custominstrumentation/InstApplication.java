package com.kmarlow.custominstrumentation;

import android.app.Application;
import android.content.Intent;


public class InstApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Intent intent = new Intent(this, AndromiumControllerService.class);
        startService(intent);
    }
}
