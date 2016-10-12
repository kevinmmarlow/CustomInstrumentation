package com.kmarlow.custominstrumentation;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ControllerService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        AndromiumInstrumentation andromiumInstrumentation = AndromiumInstrumentationInjector.inject(this);

        Intent intent = new Intent(this, SubActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
