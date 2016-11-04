package com.kmarlow.custominstrumentation;

import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

import java.io.FileDescriptor;

import static android.app.IActivityManager.FINISH_ACTIVITY_AFFINITY_TRANSACTION;
import static android.app.IActivityManager.FINISH_ACTIVITY_TRANSACTION;
import static android.app.IActivityManager.FINISH_SUB_ACTIVITY_TRANSACTION;

public class RemoteBinderProxy implements IBinder {
    private final IBinder remote;
    private final ActivityLifecycleManager lifecycleManager;

    public RemoteBinderProxy(IBinder remoteBinder, ActivityLifecycleManager activityLifecycleManager) {
        this.remote = remoteBinder;
        this.lifecycleManager = activityLifecycleManager;
    }

    @Override
    public String getInterfaceDescriptor() throws RemoteException {
        return remote.getInterfaceDescriptor();
    }

    @Override
    public boolean pingBinder() {
        return remote.pingBinder();
    }

    @Override
    public boolean isBinderAlive() {
        return remote.isBinderAlive();
    }

    @Override
    public IInterface queryLocalInterface(String descriptor) {
        return remote.queryLocalInterface(descriptor);
    }

    @Override
    public void dump(FileDescriptor fd, String[] args) throws RemoteException {
        remote.dump(fd, args);
    }

    @Override
    public void dumpAsync(FileDescriptor fd, String[] args) throws RemoteException {
        remote.dumpAsync(fd, args);
    }

    @Override
    public boolean transact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {

        if (code == FINISH_ACTIVITY_TRANSACTION) {
//            lifecycleManager.finishActivity();
            return false;
        }

        if (code == FINISH_ACTIVITY_AFFINITY_TRANSACTION) {
            return false;
        }

        if (code == FINISH_SUB_ACTIVITY_TRANSACTION) {
            return false;
        }

        return remote.transact(code, data, reply, flags);
    }

    @Override
    public void linkToDeath(DeathRecipient recipient, int flags) throws RemoteException {
        remote.linkToDeath(recipient, flags);
    }

    @Override
    public boolean unlinkToDeath(DeathRecipient recipient, int flags) {
        return remote.unlinkToDeath(recipient, flags);
    }
}
