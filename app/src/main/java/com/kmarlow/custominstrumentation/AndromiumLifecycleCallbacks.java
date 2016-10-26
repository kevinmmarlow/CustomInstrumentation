package com.kmarlow.custominstrumentation;

import android.app.Activity;

public interface AndromiumLifecycleCallbacks {

    void postActivityOnCreate(Activity activity);

    void postActivityOnResume();
}
