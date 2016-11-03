package com.kmarlow.custominstrumentation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public interface AndromiumLifecycleCallbacks {

    void execStartActivity(Context who, IBinder token, Intent intent);

    void postActivityOnCreate(Activity activity);

    void postActivityOnResume();
}
