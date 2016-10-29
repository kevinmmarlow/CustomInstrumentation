package com.kmarlow.custominstrumentation;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.UserHandle;
import android.widget.Toast;

public class AndromiumInstrumentation extends Instrumentation {

    private final Object mActivityThread;
    private final IBinder serviceToken;
    private final ActivityLifecycleManager lifecycleManager;
    private final AndromiumLifecycleCallbacks lifecycleCallbacks;

    public AndromiumInstrumentation(Object realActivityThread, IBinder serviceToken, AndromiumLifecycleCallbacks lifecycleCallbacks) {
        this.serviceToken = serviceToken;
        mActivityThread = realActivityThread;
        this.lifecycleManager = new ActivityLifecycleManager(this, mActivityThread, serviceToken);
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
        lifecycleManager.createAndStartActivity(who, token, intent);
        return null;
    }

    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options, UserHandle user) {
        Toast.makeText(who, "Start " + intent.getComponent().getShortClassName(), Toast.LENGTH_SHORT).show();
        lifecycleManager.createAndStartActivity(who, token, intent);
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
