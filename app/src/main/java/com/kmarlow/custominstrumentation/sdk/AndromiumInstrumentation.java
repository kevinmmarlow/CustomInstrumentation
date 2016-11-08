package com.kmarlow.custominstrumentation.sdk;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.UserHandle;

public class AndromiumInstrumentation extends Instrumentation {

    private final AndromiumLifecycleCallbacks lifecycleCallbacks;

    public AndromiumInstrumentation(AndromiumLifecycleCallbacks lifecycleCallbacks) {
        this.lifecycleCallbacks = lifecycleCallbacks;
    }

    // Intercept Activity construction

    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {
        lifecycleCallbacks.execStartActivity(who, token, intent);
        return null;
    }

    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, String target,
            Intent intent, int requestCode, Bundle options) {
        lifecycleCallbacks.execStartActivity(who, token, intent);
        return null;
    }

    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options, UserHandle user) {
        lifecycleCallbacks.execStartActivity(who, token, intent);
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
