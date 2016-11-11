package com.kmarlow.custominstrumentation.sdk;

import android.app.Activity;
import android.app.ActivityThread;
import android.app.Application;
import android.app.Instrumentation;
import android.app.LoadedApk;
import android.app.ResultInfo;
import android.app.VoiceInteractor;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.andromium.framework.ui.AndromiumPhoneWindow21;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class ActivityLifecycleManager {
    private static final String TAG = ActivityLifecycleManager.class.getSimpleName();

    private static final String ACTIVITY_CLIENT_RECORD = "ActivityClientRecord";
    private static final String PACKAGE_INFO = "packageInfo";
    private static final String TOKEN = "token";
    private static final String CREATE_BASE_CONTEXT_FOR_ACTIVITY = "createBaseContextForActivity";
    private static final String NON_CONFIGURATION_INSTANCES = "NonConfigurationInstances";
    private static final String ATTACH = "attach";
    private static final String M_CALLED = "mCalled";
    private static final String M_FINISHED = "mFinished";
    private static final String PERFORM_START = "performStart";
    private static final String PERFORM_RESUME = "performResume";
    private final Instrumentation instrumentation;

    private final ActivityThread activityThread;
    private final IBinder serviceToken;

    public ActivityLifecycleManager(Instrumentation instrumentation, ActivityThread activityThread, IBinder serviceToken) {
        this.instrumentation = instrumentation;
        this.activityThread = activityThread;
        this.serviceToken = serviceToken;
    }

    public Activity createAndStartActivity(Context who, IBinder token, Intent intent) {
        ResolveInfo resolveInfo = who.getPackageManager().resolveActivity(intent, 0);
        ApplicationInfo aInfo = resolveInfo.activityInfo.applicationInfo;

        try {

            LoadedApk loadedApk = createLoadedApk(aInfo);
            Activity activity = instantiateActivity(intent, loadedApk);

            Application application = loadedApk.makeApplication(false, instrumentation);

            ActivityRecord activityRecord = new ActivityRecord();
            activityRecord.activity = activity;
            activityRecord.packageInfo = loadedApk;
            activityRecord.compatInfo = loadedApk.getCompatibilityInfo();
            activityRecord.activityInfo = resolveInfo.activityInfo;
            activityRecord.intent = intent;
            activityRecord.intent.setExtrasClassLoader(loadedApk.getClassLoader());

            // TODO: Should we null the state here?
            activityRecord.state = null;
            // activityRecord.state = new Bundle();
            // activityRecord.state.setClassLoader(loadedApk.getClassLoader());

            activityRecord.persistentState = new PersistableBundle();

            ADMToken admToken = new ADMToken(activityRecord);
            Class<Activity> superActivityClazz = attachActivity(admToken, intent, resolveInfo.activityInfo, loadedApk, activity, application);

            injectAndromiumWindow(activity, superActivityClazz);

            // Apply the theme
            int theme = resolveInfo.activityInfo.getThemeResource();
            if (theme != 0) {
                activity.setTheme(theme);
            }

            return setupActivity(activity, superActivityClazz, activityRecord);
        } catch (Exception e) {
            Log.wtf(TAG, e);
            Toast.makeText(who, "Andromium is unsupported on this version", Toast.LENGTH_SHORT).show();
        }

        return null;
    }

    @Nullable
    public Activity restartActivity(Activity activity) throws NoSuchFieldException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException, ClassNotFoundException, InstantiationException {

        Class<Activity> superActivityClazz = AndromiumInstrumentationInjector.getSuperclass(activity, AndromiumInstrumentationInjector.ACTIVITY_PACKAGE);
        if (superActivityClazz == null) {
            throw new IllegalStateException(AndromiumInstrumentationInjector.ACTIVITY_PACKAGE + " not found.");
        }

        Method getActivityToken = superActivityClazz.getDeclaredMethod("getActivityToken");
        getActivityToken.setAccessible(true);
        IBinder previousToken = (IBinder) getActivityToken.invoke(activity);

        ActivityRecord activityRecord = ADMToken.tokenToActivityRecordLocked(previousToken);

        return setupActivity(activity, superActivityClazz, activityRecord);
    }

    @Nullable
    private Activity setupActivity(Activity activity, Class<Activity> superActivityClazz, ActivityRecord activityRecord) throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        // SAVED INSTANCE STATE
        Bundle icicle = activityRecord.state;

        // Basically, we should keep a track of the activities ourselves,
        // manage the parent/child flow, and setup the next intent appropriately

        Field called = superActivityClazz.getDeclaredField(M_CALLED);
        called.setAccessible(true);
        called.setBoolean(activity, false);

        // -------------------------------------------------
        // ---------------- CREATE ACTIVITY ----------------
        // -------------------------------------------------

        instrumentation.callActivityOnCreate(activity, icicle);

        checkIfSuperCalled(activity, called);

        activityRecord.stopped = true;

        // -------------------------------------------------
        // ---------------- CHECK IF FINISH ----------------
        // -------------------------------------------------

        Field finished = superActivityClazz.getDeclaredField(M_FINISHED);
        finished.setAccessible(true);

        if (finished.getBoolean(activity)) {
            Log.i(TAG, "Activity finish called in onCreate");
            activityRecord.paused = true;
            return activity;
        }

        startActivity(activity, superActivityClazz);
        activityRecord.stopped = false;

        if (finished.getBoolean(activity)) {
            Log.i(TAG, "Activity finish called in onStart");
            activityRecord.paused = true;
            return activity;
        }

        if (icicle != null) {
            restoreActivityState(activity, icicle);
        }

        if (finished.getBoolean(activity)) {
            Log.i(TAG, "Activity finish called in restoreActivityState");
            activityRecord.paused = true;
            return activity;
        }

        called.setBoolean(activity, false);
        instrumentation.callActivityOnPostCreate(activity, icicle);

        checkIfSuperCalled(activity, called);

        activityRecord.paused = true;

        resumeActivity(activity, superActivityClazz, activityRecord);

        return activity;
    }

    public Bundle pauseAndStopActivity(Activity activity) {

        try {
            Class<Activity> superActivityClazz = AndromiumInstrumentationInjector.getSuperclass(activity, AndromiumInstrumentationInjector.ACTIVITY_PACKAGE);
            Field finished = superActivityClazz.getDeclaredField(M_FINISHED);
            finished.setAccessible(true);
            boolean isFinished = finished.getBoolean(activity);

            if (isFinished) {
                return null;
            }

            Field called = superActivityClazz.getDeclaredField(M_CALLED);
            called.setAccessible(true);
            called.setBoolean(activity, false);

            instrumentation.callActivityOnPause(activity);

            // FIXME: Check if super is called

            Bundle outState = new Bundle();
            Method setAllowFds = outState.getClass().getDeclaredMethod("setAllowFds", boolean.class);
            setAllowFds.setAccessible(true);
            setAllowFds.invoke(outState, false);

            instrumentation.callActivityOnSaveInstanceState(activity, outState);

            // STOP ACTIVITY

            Method performStop = superActivityClazz.getDeclaredMethod("performStop");
            performStop.setAccessible(true);
            performStop.invoke(activity);

            return outState;

        } catch (Exception e) {
            Log.wtf(TAG, e);
            Toast.makeText(activity, "Andromium is unsupported on this version", Toast.LENGTH_SHORT).show();
        }

        return null;
    }

    public void finishActivity(Activity activity) {
        instrumentation.callActivityOnDestroy(activity);

        Context baseContext = activity.getBaseContext();
        // TODO: Cleanup Context
        // removeContextRegistrations

        // TODO: Determine how to GC resources
    }

    private void injectAndromiumWindow(Activity activity, Class superActivityClazz) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        Window window = activity.getWindow();
        AndromiumPhoneWindow21 andromiumWindow = new AndromiumPhoneWindow21(activity);

        if (!(window instanceof AndromiumPhoneWindow21)) {
            Log.d(TAG, "WINDOW: " + window.getClass().getName());

            try {
                Field field = superActivityClazz.getDeclaredField("mWindow");
                field.setAccessible(true);
                field.set(activity, andromiumWindow);
            } catch (Exception error) {
                Log.d(TAG, "GetField Activity Error: " + error);
            }
        }

        // We need to re-setup the window here
//        mWindow.setCallback(this);
//        mWindow.setOnWindowDismissedCallback(this);
//        mWindow.getLayoutInflater().setPrivateFactory(this);
//        if (info.softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED) {
//            mWindow.setSoftInputMode(info.softInputMode);
//        }
//        if (info.uiOptions != 0) {
//            mWindow.setUiOptions(info.uiOptions);
//        }

        andromiumWindow.setCallback(window.getCallback());

        // andromiumWindow.setOnWindowDismissedCallback();
        Class<Window> windowClass = (Class<Window>) andromiumWindow.getClass().getSuperclass();
        Class windowCallbacksClass = null;

        for (Class clazz : windowClass.getDeclaredClasses()) {
            if (clazz.getSimpleName().equals("OnWindowDismissedCallback")) {
                windowCallbacksClass = clazz;
                break;
            }
        }

        Method setOnWindowDismissedCallback = windowClass.getDeclaredMethod("setOnWindowDismissedCallback", windowCallbacksClass);
        setOnWindowDismissedCallback.setAccessible(true);
        setOnWindowDismissedCallback.invoke(andromiumWindow, activity);

        Field layoutInflater = andromiumWindow.getClass().getDeclaredField("mLayoutInflater");
        layoutInflater.setAccessible(true);
        layoutInflater.set(andromiumWindow, window.getLayoutInflater());

        andromiumWindow.setSoftInputMode(window.getAttributes().softInputMode);

        Field uiOptions = window.getClass().getDeclaredField("mUiOptions");
        uiOptions.setAccessible(true);
        int uiOptionsInt = uiOptions.getInt(window);
        andromiumWindow.setUiOptions(uiOptionsInt);
    }

    private void checkIfSuperCalled(Activity activity, Field called) throws IllegalAccessException {
        boolean calledSet = called.getBoolean(activity);
        if (!calledSet) {
            throw new IllegalStateException("Activity " + activity.getClass().getSimpleName() +
                    " did not call through to super.onCreate()");
        }
    }

    /**
     * Start the Activity
     *
     * @param activity
     * @param superActivityClazz
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private void startActivity(Activity activity, Class<Activity> superActivityClazz) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method performStart = superActivityClazz.getDeclaredMethod(PERFORM_START);
        performStart.setAccessible(true);
        performStart.invoke(activity);
    }

    /**
     * Restore activity state
     *
     * @param activity
     * @param icicle
     * @return
     * @throws IllegalAccessException
     */
    private void restoreActivityState(Activity activity, Bundle icicle) throws IllegalAccessException {
        instrumentation.callActivityOnRestoreInstanceState(activity, icicle);
    }

    /**
     * Instantiate the new activity using the class loader
     *
     * @param intent
     * @param loadedApk
     * @return
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     */
    private Activity instantiateActivity(Intent intent, LoadedApk loadedApk) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        ClassLoader cl = loadedApk.getClassLoader();
        return (Activity) cl.loadClass(intent.getComponent().getClassName()).newInstance();
    }

    /**
     * Create the loadedApk, this contains our package info
     *
     * @param aInfo
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private LoadedApk createLoadedApk(ApplicationInfo aInfo) {
        return activityThread.getPackageInfoNoCheck(aInfo, null);
    }

    /**
     * Attach Activity
     *
     * @param token
     * @param intent
     * @param resolveInfo
     * @param loadedApk
     * @param activity
     * @param application
     * @return
     * @throws NoSuchMethodException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchFieldException
     * @throws ClassNotFoundException
     */
    private Class<Activity> attachActivity(IBinder token, Intent intent, ActivityInfo activityInfo,
                                           LoadedApk loadedApk, Activity activity, Application application)
            throws NoSuchMethodException, InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchFieldException, ClassNotFoundException {

        Class<?> activityClientRecord = null;
        Class<?>[] declaredClasses = activityThread.getClass().getDeclaredClasses();

        for (Class clazz : declaredClasses) {
            if (clazz.getSimpleName().equals(ACTIVITY_CLIENT_RECORD)) {
                activityClientRecord = clazz;
                break;
            }
        }

        if (activityClientRecord == null) {
            throw new IllegalStateException(ACTIVITY_CLIENT_RECORD + " not found.");
        }

        Constructor<?> defaultConstructor = activityClientRecord.getDeclaredConstructor();
        defaultConstructor.setAccessible(true);

        Object realActivityClientRecord = defaultConstructor.newInstance();

        Class<?> activityClientRecordClass = realActivityClientRecord.getClass();
        Field packageInfo = activityClientRecordClass.getDeclaredField(PACKAGE_INFO);
        packageInfo.setAccessible(true);
        packageInfo.set(realActivityClientRecord, loadedApk);

        Field theToken = activityClientRecordClass.getDeclaredField(TOKEN);
        theToken.setAccessible(true);

        if (token == null) {
            token = serviceToken;
        }

        theToken.set(realActivityClientRecord, token);

        Method createBaseContextForActivity = activityThread.getClass().getDeclaredMethod(CREATE_BASE_CONTEXT_FOR_ACTIVITY, activityClientRecord, Activity.class);
        createBaseContextForActivity.setAccessible(true);
        Context appContext = (Context) createBaseContextForActivity.invoke(activityThread, realActivityClientRecord, activity);

        CharSequence title = activityInfo.loadLabel(appContext.getPackageManager());

        Class<Activity> superActivityClazz = AndromiumInstrumentationInjector.getSuperclass(activity, AndromiumInstrumentationInjector.ACTIVITY_PACKAGE);
        if (superActivityClazz == null) {
            throw new IllegalStateException(AndromiumInstrumentationInjector.ACTIVITY_PACKAGE + " not found.");
        }

        Class nonConfigInstances = null;
        Class<?>[] activityInnerClasses = superActivityClazz.getDeclaredClasses();
        for (Class clazz : activityInnerClasses) {
            if (clazz.getSimpleName().equals(NON_CONFIGURATION_INSTANCES)) {
                nonConfigInstances = clazz;
                break;
            }
        }

        if (nonConfigInstances == null) {
            throw new IllegalStateException(NON_CONFIGURATION_INSTANCES + " not found.");
        }

        Method attachMethod = null;

        try {
            attachMethod = superActivityClazz.getDeclaredMethod(ATTACH, Context.class, activityThread.getClass(),
                    Instrumentation.class, IBinder.class, int.class, Application.class, Intent.class,
                    ActivityInfo.class, CharSequence.class, Activity.class, String.class, nonConfigInstances,
                    Configuration.class, String.class, VoiceInteractor.class);
        } catch (Exception ignored) {
            Log.wtf(TAG, ignored.getLocalizedMessage());
        }

        if (attachMethod == null) {
            for (Method method : superActivityClazz.getDeclaredMethods()) {
                if (method.getName().equals(ATTACH)) {
                    attachMethod = method;
                    break;
                }
            }
        }

        if (attachMethod == null) {
            throw new IllegalStateException(ATTACH + " not found.");
        }

        // TODO: Pull parent somehow
        Activity parent = null;

        attachMethod.setAccessible(true);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            attachMethod.invoke(activity, appContext, activityThread, instrumentation, token,
                    0, application, intent, activityInfo, title, parent,
                    null, null, new Configuration(),
                    null);
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            // Adds (String referrer)
            attachMethod.invoke(activity, appContext, activityThread, instrumentation, token,
                    0, application, intent, activityInfo, title, parent,
                    null, null, new Configuration(),
                    null, null);
        } else {
            // Adds (Window window)
            attachMethod.invoke(activity, appContext, activityThread, instrumentation, token,
                    0, application, intent, activityInfo, title, parent,
                    null, null, new Configuration(),
                    null, null, null);
        }

        return superActivityClazz;
    }

    /**
     * Resume Activity
     *
     * @param activity
     * @param superActivityClazz
     * @param activityRecord
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private void resumeActivity(Activity activity, Class<Activity> superActivityClazz, ActivityRecord activityRecord) throws
            NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.onStateNotSaved();
        }

        // Now doing activity.mFragments.noteStateNotSaved();
        Field fragments = superActivityClazz.getDeclaredField("mFragments");
        fragments.setAccessible(true);

        // Could be either FragmentController or FragmentManagerImpl
        Object fragController = fragments.get(activity);

        Method noteStateNotSaved = fragController.getClass().getDeclaredMethod("noteStateNotSaved");
        noteStateNotSaved.setAccessible(true);
        noteStateNotSaved.invoke(fragController);

        // -------------------------------------------------
        // --------------- INTENT REDELIVERY ---------------
        // -------------------------------------------------


        if (activityRecord.pendingIntents != null) {
            List<Intent> intents = activityRecord.pendingIntents;
            // deliverNewIntents(activityRecord, intents);
            final int N = intents.size();
            for (int i = 0; i < N; i++) {
                Intent intent = intents.get(i);
                intent.setExtrasClassLoader(activity.getClassLoader());
                noteStateNotSaved.invoke(fragController);
                instrumentation.callActivityOnNewIntent(activity, intent);
            }
            activityRecord.pendingIntents = null;
        }

        // -------------------------------------------------
        // ---------------- RESULT DELIVERY ----------------
        // -------------------------------------------------

        // For this, we will need to keep track of when activities call
        // setResult(...) and then have an override for finish(...) in order to send
        // the results back.

        if (activityRecord.pendingResults != null) {
            // deliverResults(r, r.pendingResults);
            List<ResultInfo> results = activityRecord.pendingResults;
            final int N = results.size();
            for (int i = 0; i < N; i++) {
                ResultInfo ri = results.get(i);

                if (ri.mData != null) {
                    ri.mData.setExtrasClassLoader(activity.getClassLoader());
                }

                Method dispatchActivityResult = superActivityClazz.getDeclaredMethod("dispatchActivityResult",
                        String.class, int.class, int.class, Intent.class);
                dispatchActivityResult.setAccessible(true);
                dispatchActivityResult.invoke(activity, ri.mResultWho,
                        ri.mRequestCode, ri.mResultCode, ri.mData);
            }

            activityRecord.pendingResults = null;
        }

        Method performResume = superActivityClazz.getDeclaredMethod(PERFORM_RESUME);
        performResume.setAccessible(true);
        performResume.invoke(activity);

        Log.d(TAG, "Performing Resume");
        instrumentation.callActivityOnResume(activity);

        activityRecord.paused = false;
        activityRecord.stopped = false;
        activityRecord.state = null;
        activityRecord.persistentState = null;
    }
}
