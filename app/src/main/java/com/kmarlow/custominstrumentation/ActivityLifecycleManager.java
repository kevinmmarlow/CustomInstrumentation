package com.kmarlow.custominstrumentation;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.ViewManager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.andromium.framework.ui.AndromiumPhoneWindow21;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.kmarlow.custominstrumentation.AndromiumInstrumentationInjector.ACTIVITY_PACKAGE;
import static com.kmarlow.custominstrumentation.AndromiumInstrumentationInjector.getSuperclass;

public class ActivityLifecycleManager {
    private static final String TAG = "jesse";

    public static final String MAKE_APPLICATION = "makeApplication";
    public static final String GET_PACKAGE_INFO_NO_CHECK = "getPackageInfoNoCheck";
    public static final String GET_CLASS_LOADER = "getClassLoader";
    public static final String ACTIVITY_CLIENT_RECORD = "ActivityClientRecord";
    public static final String PACKAGE_INFO = "packageInfo";
    public static final String TOKEN = "token";
    public static final String CREATE_BASE_CONTEXT_FOR_ACTIVITY = "createBaseContextForActivity";
    public static final String NON_CONFIGURATION_INSTANCES = "NonConfigurationInstances";
    public static final String COM_ANDROID_INTERNAL_APP_IVOICE_INTERACTOR = "com.android.internal.app.IVoiceInteractor";
    public static final String ATTACH = "attach";
    public static final String M_CALLED = "mCalled";
    public static final String M_FINISHED = "mFinished";
    public static final String PERFORM_START = "performStart";
    public static final String PERFORM_RESUME = "performResume";
    private final Instrumentation instrumentation;

    public interface LifecycleControllerCallbacks {

        void callActivityOnCreate(Activity activity, Bundle icicle);

        void callActivityOnRestoreInstanceState(Activity activity, Bundle icicle);

        void callActivityOnPostCreate(Activity activity, Bundle icicle);

        void callActivityOnResume(Activity activity);

    }

    private final LifecycleControllerCallbacks callbacks;
    private final Object activityThread;
    private final IBinder serviceToken;

    public ActivityLifecycleManager(LifecycleControllerCallbacks callbacks, Instrumentation instrumentation, Object activityThread, IBinder serviceToken) {
        this.callbacks = callbacks;
        this.instrumentation = instrumentation;
        this.activityThread = activityThread;
        this.serviceToken = serviceToken;
    }

    public boolean createAndStartActivity(Context who, IBinder token, Intent intent) {
        ResolveInfo resolveInfo = who.getPackageManager().resolveActivity(intent, 0);
        ApplicationInfo aInfo = resolveInfo.activityInfo.applicationInfo;

        try {

            Object loadedApk = createLoadedApk(aInfo);
            Activity activity = instantiateActivity(intent, loadedApk);

            Method makeApplication = loadedApk.getClass().getDeclaredMethod(MAKE_APPLICATION, boolean.class, Instrumentation.class);
            Application application = (Application) makeApplication.invoke(loadedApk, false, instrumentation);

            Class superActivityClazz = attachActivity(token, intent, resolveInfo, loadedApk, activity, application);

            Window window = activity.getWindow();

            Log.d(TAG, "WINDOW: " + window.getClass().getName());

            try {
                Field field = superActivityClazz.getDeclaredField("mWindow");
                field.setAccessible(true);
                field.set(activity, new AndromiumPhoneWindow21(activity));
            } catch (Exception error) {
                Log.d("jesse", "GetField Activity Error: " + error);
            }

            // Apply the theme
            int theme = resolveInfo.activityInfo.getThemeResource();
            if (theme != 0) {
                activity.setTheme(theme);
            }

            // FAKE SAVED INSTANCE STATE
            Bundle icicle = new Bundle();

            // Basically, we should keep a track of the activities ourselves,
            // manage the parent/child flow, and setup the next intent appropriately

            Field called = superActivityClazz.getDeclaredField(M_CALLED);
            called.setAccessible(true);
            called.setBoolean(activity, false);

            // -------------------------------------------------
            // ---------------- CREATE ACTIVITY ----------------
            // -------------------------------------------------

            // TODO: Save the bundle when we should stop the activity, "restore" it when we start
            callbacks.callActivityOnCreate(activity, icicle /* SAVED BUNDLE HERE */);

            // -------------------------------------------------
            // ---------------- CHECK IF FINISH ----------------
            // -------------------------------------------------

            Field finished = superActivityClazz.getDeclaredField(M_FINISHED);
            finished.setAccessible(true);
            boolean isFinished = finished.getBoolean(activity);

            if (isFinished) {
                Log.i(TAG, "Activity finish called in onCreate");
//                callbacks.callActivityOnPostCreate(activity, icicle /* SAVED BUNDLE HERE */);
                return true;
            }

            if (startActivity(activity, superActivityClazz, finished)) return true;

            checkIfSuperCalled(intent, activity, called);

            if (restoreActivityState(activity, icicle, finished)) return true;

            callbacks.callActivityOnPostCreate(activity, icicle /* SAVED BUNDLE HERE */);

            resumeActivity(activity, superActivityClazz);


        } catch (Exception e) {
            Log.wtf(TAG, e);
            Toast.makeText(who, "Andromium is unsupported on this version", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void checkIfSuperCalled(Intent intent, Activity activity, Field called) throws IllegalAccessException {
        boolean calledSet = called.getBoolean(activity);
        if (!calledSet) {
            throw new IllegalStateException("Activity " + intent.getComponent().toShortString() +
                    " did not call through to super.onCreate()");
        }
    }

    /**
     * Start the Activity
     *
     * @param activity
     * @param superActivityClazz
     * @param finished
     * @return
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private boolean startActivity(Activity activity, Class superActivityClazz, Field finished) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method performStart = superActivityClazz.getDeclaredMethod(PERFORM_START);
        performStart.setAccessible(true);
        performStart.invoke(activity);

        boolean isFinished = finished.getBoolean(activity);

        if (isFinished) {
            Log.i(TAG, "Activity finish called in onStart");
        }

        return isFinished;
    }

    /**
     * Restore activity state
     *
     * @param activity
     * @param icicle
     * @param finished
     * @return
     * @throws IllegalAccessException
     */
    private boolean restoreActivityState(Activity activity, Bundle icicle, Field finished) throws IllegalAccessException {
        callbacks.callActivityOnRestoreInstanceState(activity, icicle /* SAVED BUNDLE HERE */);

        boolean isFinished = (boolean) finished.get(activity);

        if (isFinished) {
            Log.i(TAG, "Activity finish called in onStart");
        }

        return isFinished;
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
    private Activity instantiateActivity(Intent intent, Object loadedApk) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, ClassNotFoundException {
        Method getClassLoader = loadedApk.getClass().getDeclaredMethod(GET_CLASS_LOADER);
        ClassLoader cl = (ClassLoader) getClassLoader.invoke(loadedApk);

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
    private Object createLoadedApk(ApplicationInfo aInfo) throws IllegalAccessException, InvocationTargetException {
        Method[] declaredMethods = activityThread.getClass().getDeclaredMethods();
        Method packageInfoCheck = null;

        for (Method method : declaredMethods) {
            if (method.getName().equals(GET_PACKAGE_INFO_NO_CHECK)) {
                packageInfoCheck = method;
                break;
            }
        }

        if (packageInfoCheck == null) {
            throw new IllegalStateException(GET_PACKAGE_INFO_NO_CHECK + " does not exist.");
        }

        return packageInfoCheck.invoke(activityThread, aInfo, null);
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
    private Class attachActivity(IBinder token, Intent intent, ResolveInfo resolveInfo, Object loadedApk, Activity activity, Application application) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchFieldException, ClassNotFoundException {
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

        CharSequence title = resolveInfo.activityInfo.loadLabel(appContext.getPackageManager());

        Class superActivityClazz = getSuperclass(activity, ACTIVITY_PACKAGE);
        if (superActivityClazz == null) {
            throw new IllegalStateException(ACTIVITY_PACKAGE + " not found.");
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

        Class<?> voiceInteractorClass = Class.forName(COM_ANDROID_INTERNAL_APP_IVOICE_INTERACTOR);

        Method attachMethod = null;

        try {
            attachMethod = superActivityClazz.getDeclaredMethod(ATTACH, Context.class, activityThread.getClass(),
                    Instrumentation.class, IBinder.class, int.class, Application.class, Intent.class,
                    ActivityInfo.class, CharSequence.class, Activity.class, String.class, nonConfigInstances,
                    Configuration.class, String.class, voiceInteractorClass);
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
            attachMethod.invoke(activity, appContext, activityThread, instrumentation, serviceToken,
                    0, application, intent, resolveInfo.activityInfo, title, parent,
                    null, null, new Configuration(),
                    null);
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            // Adds (String referrer)
            attachMethod.invoke(activity, appContext, activityThread, instrumentation, serviceToken,
                    0, application, intent, resolveInfo.activityInfo, title, parent,
                    null, null, new Configuration(),
                    null, null);
        } else {
            // Adds (Window window)
            attachMethod.invoke(activity, appContext, activityThread, instrumentation, serviceToken,
                    0, application, intent, resolveInfo.activityInfo, title, parent,
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
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private void resumeActivity(Activity activity, Class superActivityClazz) throws
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

        // FIXME: deliverNewIntents relies on activity stack checking
//            r.activity.mFragments.noteStateNotSaved();
//            if (r.pendingIntents != null) {
//                deliverNewIntents(r, r.pendingIntents);
//                r.pendingIntents = null;
//            }

        // -------------------------------------------------
        // ---------------- RESULT DELIVERY ----------------
        // -------------------------------------------------

        // For this, we will need to keep track of when activities call
        // setResult(...) and then have an override for finish(...) in order to send
        // the results back.

//            if (r.pendingResults != null) {
//                deliverResults(r, r.pendingResults);
//                r.pendingResults = null;
//            }

        Method performResume = superActivityClazz.getDeclaredMethod(PERFORM_RESUME);
        performResume.setAccessible(true);
        performResume.invoke(activity);

        Log.d("jesse", "Performing Resume");
        callbacks.callActivityOnResume(activity);

        Window window = activity.getWindow();
        View decor = window.getDecorView();
        decor.setVisibility(View.INVISIBLE);
        ViewManager wm = activity.getWindowManager();
        WindowManager.LayoutParams l = window.getAttributes();


        // activity.mDecor = decor;
        Field mDecor = superActivityClazz.getDeclaredField("mDecor");
        mDecor.setAccessible(true);
        mDecor.set(activity, decor);

        l.type = WindowManager.LayoutParams.TYPE_BASE_APPLICATION;

        // activity.mVisibleFromClient
//        Field mVisibleFromClient = superActivityClazz.getDeclaredField("mVisibleFromClient");
//        mVisibleFromClient.setAccessible(true);
//        boolean visibleFromClient = mVisibleFromClient.getBoolean(activity);

//        if (visibleFromClient) {
//            // activity.mWindowAdded = true;
//            Field mWindowAdded = superActivityClazz.getDeclaredField("mWindowAdded");
//            mWindowAdded.setAccessible(true);
//            mWindowAdded.setBoolean(activity, true);
//
//            wm.addView(decor, l);
//        }
    }
}
