package com.kmarlow.custominstrumentation;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.Field;

public class MyService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        injectAndromiumInstrumentation();

        Intent intent = new Intent(this, SubActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public static final String SERVICE_PACKAGE = "android.app.Service";
    public static final String ACTIVITY_THREAD_PACKAGE = "android.app.ActivityThread";
    public static final String INSTRUMENTATION_PACKAGE = "android.app.Instrumentation";
    public static final String ACTIVITY_THREAD_VAR = "mThread";
    public static final String INSTRUMENTATION_VAR = "mInstrumentation";

    private void injectAndromiumInstrumentation() {
        Class activityThreadClazz = null;
        try {
            activityThreadClazz = Class.forName(ACTIVITY_THREAD_PACKAGE);
        } catch (ClassNotFoundException ignored) {
            // Just return
            Log.wtf("KEVIN", "ActivityThread class has changed package structure.");
        }

        if (activityThreadClazz == null) {
            return;
        }


        Class serviceClass = this.getClass();

        while (serviceClass != null && !serviceClass.getName().equals(SERVICE_PACKAGE)) {
            serviceClass = serviceClass.getSuperclass();
        }

        if (serviceClass == null) {
            return;
        }


        try {
            Field activityThread = serviceClass.getDeclaredField(ACTIVITY_THREAD_VAR);

            if (activityThread == null) {

                Field[] allFields = serviceClass.getDeclaredFields();
                for (Field field : allFields) {
                    if (field.getType().getName().equals(ACTIVITY_THREAD_PACKAGE)) {
                        activityThread = field;
                        break;
                    }
                }

                if (activityThread == null) {
                    Log.wtf("KEVIN", "Service no longer contains ActivityThread.");
                    return;
                }
            }

            activityThread.setAccessible(true);
            Object realActivityThread = activityThread.get(this);

            Field instrumentation = realActivityThread.getClass().getDeclaredField(INSTRUMENTATION_VAR);

            if (instrumentation == null) {

                Field[] allFields = realActivityThread.getClass().getDeclaredFields();
                for (Field field : allFields) {
                    if (field.getType().getName().equals(INSTRUMENTATION_PACKAGE)) {
                        instrumentation = field;
                        break;
                    }
                }

                if (instrumentation == null) {
                    Log.wtf("KEVIN", "ActivityThread no longer contains Instrumentation.");
                    return;
                }
            }

            instrumentation.setAccessible(true);
            instrumentation.set(realActivityThread, new NoOpInstrumentation());


        } catch (Exception e) {
            // Something crazy happened, rethrow, or potentially just don't open that app.
            throw new RuntimeException(e);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
