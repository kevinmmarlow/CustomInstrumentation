### The Android Zygote

Every application starts as the fork of a half-instantiated process called a [Zygote](https://anatomyofandroid.com/2013/10/15/zygote/).
This happens at the linux level.


### How Apps are launched from the Zygote

Once the Zygote has been started, it calls into the `SystemServer.java` class in order to get the java-level code up and running. Here is the main entry point.

```
/**
 * The main entry point from zygote.
 */
public static void main(String[] args) {
    new SystemServer().run();
}
```

The `run()` method is responsible for all of the system-wide initialization. This includes things such as guaranteeing the system millis are not less than the 1970 minimum, setting up the system context, preparing all of the system services, and setting up the looper. The following code shows the `ActivityThread.java` class being initialized for the `systemMain` thread.

```
private void createSystemContext() {
    ActivityThread activityThread = ActivityThread.systemMain();
    mSystemContext = activityThread.getSystemContext();
    mSystemContext.setTheme(android.R.style.Theme_DeviceDefault_Light_DarkActionBar);
}
```

That method looks like this:

```
public static ActivityThread systemMain() {
    ...
    ActivityThread thread = new ActivityThread();
    thread.attach(true);
    return thread;
}
```

and the attach looks like this:

```
private void attach(boolean system) {
    sCurrentActivityThread = this;
    mSystemThread = system;
    if (!system) {
        ...
    } else {
        // Don't set application object here -- if the system crashes,
        // we can't display an alert, we just want to die die die.
        android.ddm.DdmHandleAppName.setAppName("system_process",
                UserHandle.myUserId());
        try {
            mInstrumentation = new Instrumentation();
            ContextImpl context = ContextImpl.createAppContext(
                    this, getSystemContext().mPackageInfo);
            mInitialApplication = context.mPackageInfo.makeApplication(true, null);
            mInitialApplication.onCreate();
        } catch (Exception e) {
            throw new RuntimeException(
                    "Unable to instantiate Application():" + e.toString(), e);
        }
    }
}
```
From then on, our system-level context will live in a system-level instance of the `ActivityThread.java` class. It is lazily initialized (very) shortly after the attach is called.

```
public ContextImpl getSystemContext() {
    synchronized (this) {
        if (mSystemContext == null) {
            mSystemContext = ContextImpl.createSystemContext(this);
        }
        return mSystemContext;
    }
}
```

Now that we have a system-level context, we can initialize all of the system services that we love so much. For now, we want to focus on the `ActivityManagerService.java` class, because it runs the show (there's even a code comment that says so).

We initialize the first round of these services in the `startBootstrapServices()` method of the `SystemServer.java` class. Here is the part we care about.

```
private void startBootstrapServices() {
	...
	
    // Activity manager runs the show.
    mActivityManagerService = mSystemServiceManager.startService(
            ActivityManagerService.Lifecycle.class).getService();
    mActivityManagerService.setSystemServiceManager(mSystemServiceManager);
    mActivityManagerService.setInstaller(installer);
    
    ...
    
    // Set up the Application instance for the system process and get started.
    mActivityManagerService.setSystemProcess();
    
    ...
}
```


```
public static final class Lifecycle extends SystemService {
    private final ActivityManagerService mService;

    public Lifecycle(Context context) {
        super(context);
        mService = new ActivityManagerService(context);
    }

    @Override
    public void onStart() {
        mService.start();
    }

    public ActivityManagerService getService() {
        return mService;
    }
}

public ActivityManagerService(Context systemContext) {
    mContext = systemContext;
    mFactoryTest = FactoryTest.getMode();
    mSystemThread = ActivityThread.currentActivityThread();

    ...
    
    mRecentTasks = new RecentTasks(this);
    mStackSupervisor = new ActivityStackSupervisor(this, mRecentTasks);
    
    ...
}
```

```
/**
 * Starts a miscellaneous grab bag of stuff that has yet to be refactored
 * and organized.
 */
private void startOtherServices() {
    // We now tell the activity manager it is okay to run third party
    // code.  It will call back into us once it has gotten to the state
    // where third party code can really run (but before it has actually
    // started launching the initial applications), for us to complete our
    // initialization.
    mActivityManagerService.systemReady(new Runnable() {
            @Override
            public void run() {
                Slog.i(TAG, "Making services ready");
                mSystemServiceManager.startBootPhase(
                        SystemService.PHASE_ACTIVITY_MANAGER_READY);

                try {
                    mActivityManagerService.startObservingNativeCrashes();
                } catch (Throwable e) {
                    reportWtf("observing native crashes", e);
                }

                Slog.i(TAG, "WebViewFactory preparation");
                WebViewFactory.prepareWebViewInSystemServer();

                try {
                    startSystemUi(context);
                } catch (Throwable e) {
                    reportWtf("starting System UI", e);
                }

                ...
            }
    });
}
```


```
public void systemReady(final Runnable goingCallback) {
    ...
    synchronized(this) {
        ...
        // Start up initial activity.
        mBooting = true;
        startHomeActivityLocked(mCurrentUserId, "systemReady");
    }
    ...
}
```

```
static final void startSystemUi(Context context) {
    Intent intent = new Intent();
    intent.setComponent(new ComponentName("com.android.systemui",
                "com.android.systemui.SystemUIService"));
    //Slog.d(TAG, "Starting service: " + intent);
    context.startServiceAsUser(intent, UserHandle.OWNER);
}
```


### How the system launches your app when the user clicks the icon

The `PhoneWindowManager.java` class keeps track of all of the events happening on your application's phone window. It then works with the `Intent` objects cached inside the `ShortcutManager.java` class in order to retrieve the selected application intent.

Then, it calls `startActivityAsUser(Intent intent, UserHandle handle)` to start the activity.

This call just calls into the `ContextImpl.java` class like so.

```
@Override
public void startActivityAsUser(Intent intent, Bundle options, UserHandle user) {
    try {
        ActivityManagerNative.getDefault().startActivityAsUser(
            mMainThread.getApplicationThread(), getBasePackageName(), intent,
            intent.resolveTypeIfNeeded(getContentResolver()),
            null, null, 0, Intent.FLAG_ACTIVITY_NEW_TASK, null, options,
            user.getIdentifier());
    } catch (RemoteException e) {
        throw new RuntimeException("Failure from system", e);
    }
}
```

As you can see, it is then directed to the `ActivityManagerNative.getDefault()` instance, which we know is a system-wide SINGLETON that points to the service set up by the `mActivityManagerService.setSystemProcess()` from above (it is inside `SystemServer.java`).

Here it is again.

```
public void setSystemProcess() {
    try {
        ServiceManager.addService(Context.ACTIVITY_SERVICE, this, true);
        ServiceManager.addService(ProcessStats.SERVICE_NAME, mProcessStats);
    } ...
}
```

It may seem confusing (it is confusing) because there are several seemingly similar classes. There is `ActivityManagerNative.java`, `ActivityManagerService.java`, and `ActivityManager.java`.

![image](https://memecrunch.com/image/509852c2afa96f2d52000009.jpg?w=400)


Okay, so here's how it works. `ActivityManagerService` **is an extension** of `ActivityManagerNative`. It is set up by the `SystemServer` class and it keeps track of all of the processes of the system.

Whereas, `ActivityManager` is the class that is registered as the `Context.ACTIVITY_SERVICE` inside the `SystemServiceRegistry.java`.

For our purposes, we are interested in where the activities are registered, so we will look at the `startActivityAsUser` function that resides inside the `ActivityManagerService` class.

```
@Override
public final int startActivityAsUser(IApplicationThread caller, String callingPackage,
        Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode,
        int startFlags, ProfilerInfo profilerInfo, Bundle options, int userId) {
    enforceNotIsolatedCaller("startActivity");
    userId = handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId,
            false, ALLOW_FULL_ONLY, "startActivity", null);
    // TODO: Switch to user app stacks here.
    return mStackSupervisor.startActivityMayWait(caller, -1, callingPackage, intent,
            resolvedType, null, null, resultTo, resultWho, requestCode, startFlags,
            profilerInfo, null, null, options, false, userId, null, null);
}
```

As you can see, this code calls into the `ActivityStackSupervisor.java` in order to start the actvity. Let's look at that method now.

