package com.kmarlow.custominstrumentation;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.Window;


public class AndromiumInstrumentation extends Instrumentation {
    private static final String TAG = AndromiumInstrumentation.class.getSimpleName();

    public AndromiumInstrumentation() {

    }

    // Intercept Activity construction

//    public ActivityResult execStartActivity(
//            Context who, IBinder contextThread, IBinder token, Activity target,
//            Intent intent, int requestCode, Bundle options) {
//        return null;
//    }
//
//    public ActivityResult execStartActivity(
//            Context who, IBinder contextThread, IBinder token, String target,
//            Intent intent, int requestCode, Bundle options) {
//        return null;
//    }
//
//    public ActivityResult execStartActivity(
//            Context who, IBinder contextThread, IBinder token, Activity target,
//            Intent intent, int requestCode, Bundle options, UserHandle user) {
//        return null;
//    }


    // Intercept activity attach

    @Override
    public Activity newActivity(Class<?> clazz, Context context,
                                IBinder token, Application application, Intent intent, ActivityInfo info,
                                CharSequence title, Activity parent, String id,
                                Object lastNonConfigurationInstance) throws InstantiationException, IllegalAccessException {
        Log.e(TAG, "newActivity -> " + clazz.getName());
        Activity activity = super.newActivity(clazz, context, token, application, intent, info, title, parent, id, lastNonConfigurationInstance);

        Window window = activity.getWindow();
        View view = window.peekDecorView();

        Log.i(TAG, "WINDOW: " + window.getClass().getName() + ". DecorView: " + view.getClass().getName());
        return activity;
    }
}
