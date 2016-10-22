package com.kmarlow.custominstrumentation;

import android.app.Activity;
import android.app.ActivityManager;
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

import com.andromium.framework.ui.AndromiumPhoneWindow21;

import java.lang.reflect.Field;


public class AndromiumInstrumentation extends Instrumentation implements ActivityLifecycleManager.LifecycleControllerCallbacks {

    private static final String TAG = "jesse";

    private final ActivityManager mActivityManager;
    private final Object mActivityThread;
    private final IBinder serviceToken;
    private final ActivityLifecycleManager lifecycleManager;

    public AndromiumInstrumentation(Context context, Object realActivityThread, IBinder serviceToken) {
        mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        this.serviceToken = serviceToken;
        mActivityThread = realActivityThread;
        this.lifecycleManager = new ActivityLifecycleManager(this, this, mActivityThread, serviceToken);
    }

    // Intercept Activity construction

    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {
        Toast.makeText(who, "Start " + intent.getComponent().getShortClassName(), Toast.LENGTH_SHORT).show();

        lifecycleManager.createAndStartActivity(who, token, intent);

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

        Log.d(TAG, "WINDOW: " + window.getClass().getName() + ". DecorView: " + view.getClass().getName());

        try {
            Field field = activity.getClass().getField("mWindow");
            field.setAccessible(true);
            field.set(activity, new AndromiumPhoneWindow21(activity));
        } catch (Exception error) {
            Log.d("jesse", "GetField Activity Error: " + error);
        }


        window = activity.getWindow();
        view = window.peekDecorView();

        Log.d(TAG, "WINDOW: " + window.getClass().getName() + ". DecorView: " + view.getClass().getName());
        return activity;
    }
}
