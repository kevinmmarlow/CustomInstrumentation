package com.kmarlow.custominstrumentation;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.UserHandle;
import android.util.Log;
import android.widget.Toast;

public class AndromiumInstrumentation extends Instrumentation implements ActivityLifecycleManager.LifecycleControllerCallbacks {

    private static final String TAG = "jesse";

    private final ActivityManager mActivityManager;
    private final Object mActivityThread;
    private final IBinder serviceToken;
    private final ActivityLifecycleManager lifecycleManager;
    private final AndromiumLifecycleCallbacks lifecycleCallbacks;

    public AndromiumInstrumentation(Context context, Object realActivityThread, IBinder serviceToken, AndromiumLifecycleCallbacks lifecycleCallbacks) {
        mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        this.serviceToken = serviceToken;
        mActivityThread = realActivityThread;
        this.lifecycleManager = new ActivityLifecycleManager(this, this, mActivityThread, serviceToken);
        this.lifecycleCallbacks = lifecycleCallbacks;
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

    @Override
    public void callActivityOnPostCreate(Activity activity, Bundle icicle) {
        super.callActivityOnPostCreate(activity, icicle);
        lifecycleCallbacks.postActivityOnCreate(activity);
    }

    @Override
    public void callActivityOnResume(Activity activity) {
        super.callActivityOnResume(activity);
        lifecycleCallbacks.postActivityOnResume();
    }
}
