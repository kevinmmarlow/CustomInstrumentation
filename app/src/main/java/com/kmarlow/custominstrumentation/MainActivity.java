package com.kmarlow.custominstrumentation;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.lang.reflect.Field;

public class MainActivity extends AppCompatActivity {

    public static final String ACTIVITY_PACKAGE = "android.app.Activity";
    public static final String ACTIVITY_THREAD_PACKAGE = "android.app.ActivityThread";
    public static final String INSTRUMENTATION_PACKAGE = "android.app.Instrumentation";
    public static final String ACTIVITY_THREAD_VAR = "mMainThread";
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


        Class activityClass = this.getClass();

        while (activityClass != null && !activityClass.getName().equals(ACTIVITY_PACKAGE)) {
            activityClass = activityClass.getSuperclass();
        }

        if (activityClass == null) {
            return;
        }


        try {
            Field activityThread = activityClass.getDeclaredField(ACTIVITY_THREAD_VAR);

            if (activityThread == null) {

                Field[] allFields = activityClass.getDeclaredFields();
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

    public MainActivity() {
//        injectAndromiumInstrumentation();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(android.R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SubActivity.class));
            }
        });
    }
}
