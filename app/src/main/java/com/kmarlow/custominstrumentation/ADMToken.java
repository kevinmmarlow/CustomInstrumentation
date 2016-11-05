package com.kmarlow.custominstrumentation;

import android.os.IBinder;
import android.os.RemoteException;
import android.view.IApplicationToken;

import java.lang.ref.WeakReference;

public class ADMToken extends IApplicationToken.Stub {

    private final WeakReference<ActivityRecord> weakActivity;

    public ADMToken(ActivityRecord activityRecord) {
        this.weakActivity = new WeakReference<ActivityRecord>(activityRecord);
    }

    @Override
    public long getKeyDispatchingTimeout() throws RemoteException {
        return 0;
    }

    @Override
    public boolean keyDispatchingTimedOut(String s) throws RemoteException {
        return false;
    }

    @Override
    public void windowsDrawn() throws RemoteException {

    }

    @Override
    public void windowsGone() throws RemoteException {

    }

    @Override
    public void windowsVisible() throws RemoteException {

    }

    public static final ActivityRecord tokenToActivityRecordLocked(IBinder token) {
        if (token != null && token instanceof ADMToken) {
            return ((ADMToken) token).weakActivity.get();
        }

        return null;
    }
}
