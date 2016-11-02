package com.kmarlow.custominstrumentation;

import android.app.Activity;
import android.content.Intent;

public interface AndromiumLifecycleCallbacks {

    void postActivityOnCreate(Activity activity);

    void postActivityOnResume();

    boolean activityIsShowing(Intent intent);
}
