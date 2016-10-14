package com.kmarlow.custominstrumentation;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.UserHandle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;


public class AndromiumInstrumentation extends Instrumentation {
    private static final String TAG = AndromiumInstrumentation.class.getSimpleName();

    public AndromiumInstrumentation() {
    }

    // Intercept Activity construction

    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {
        Toast.makeText(who, "Start " + intent.getComponent().getShortClassName(), Toast.LENGTH_SHORT).show();

//        try {
//            Class<Instrumentation> instrumentation = (Class<Instrumentation>) getClass().getSuperclass();
//            Method execStartActivity = instrumentation.getDeclaredMethod("execStartActivity", new Class[]{Context.class, IBinder.class, IBinder.class, Activity.class, Intent.class, int.class, Bundle.class});
//            execStartActivity.setAccessible(true);
//
//            return (ActivityResult) execStartActivity.invoke(this, who, contextThread, token, target, intent, requestCode, options);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        return null;
    }

    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, String target,
            Intent intent, int requestCode, Bundle options) {
        Toast.makeText(who, "Start " + intent.getComponent().getShortClassName(), Toast.LENGTH_SHORT).show();

//        try {
//            Class<Instrumentation> instrumentation = (Class<Instrumentation>) getClass().getSuperclass();
//            Method execStartActivity = instrumentation.getDeclaredMethod("execStartActivity", new Class[]{Context.class, IBinder.class, IBinder.class, String.class, Intent.class, int.class, Bundle.class});
//            execStartActivity.setAccessible(true);
//
//            return (ActivityResult) execStartActivity.invoke(this, who, contextThread, token, target, intent, requestCode, options);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        return null;
    }

    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options, UserHandle user) {
        Toast.makeText(who, "Start " + intent.getComponent().getShortClassName(), Toast.LENGTH_SHORT).show();

//        try {
//            Class<Instrumentation> instrumentation = (Class<Instrumentation>) getClass().getSuperclass();
//            Method execStartActivity = instrumentation.getDeclaredMethod("execStartActivity", new Class[]{Context.class, IBinder.class, IBinder.class, Activity.class, Intent.class, int.class, Bundle.class, UserHandle.class});
//            execStartActivity.setAccessible(true);
//
//            return (ActivityResult) execStartActivity.invoke(this, who, contextThread, token, target, intent, requestCode, options, user);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        return null;
    }


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
