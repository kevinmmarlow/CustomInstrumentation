package com.kmarlow.custominstrumentation;

import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import android.app.IActivityManager;
import android.app.Service;
import android.content.ContextWrapper;
import android.os.IBinder;
import android.util.Log;
import android.util.Pair;
import android.util.Singleton;

import java.lang.reflect.Field;

public final class AndromiumInstrumentationInjector {
    private static final String TAG = AndromiumInstrumentationInjector.class.getName();

    private static final String SERVICE_PACKAGE = "android.app.Service";
    public static final String ACTIVITY_PACKAGE = "android.app.Activity";

    private static final String ACTIVITY_THREAD_PACKAGE = "android.app.ActivityThread";
    private static final String INSTRUMENTATION_PACKAGE = "android.app.Instrumentation";
    private static final String ACTIVITY_THREAD_VAR_IN_SERVICE = "mThread";
    private static final String INSTRUMENTATION_FIELD = "mInstrumentation";
    public static final String SERVICE_TOKEN = "mToken";


    public static Pair<AndromiumInstrumentation, ActivityLifecycleManager> inject(Service service, AndromiumLifecycleCallbacks andromiumLifecycleCallbacks) {
        if (!hasActivityThread()) return null;

        Class superClazz = getSuperclass(service, SERVICE_PACKAGE);
        if (superClazz == null) {
            return null;
        }

        try {
            Field activityThread = getField(superClazz, ACTIVITY_THREAD_VAR_IN_SERVICE, ACTIVITY_THREAD_PACKAGE);
            activityThread.setAccessible(true);
            ActivityThread realActivityThread = (ActivityThread) activityThread.get(service);

            Field token = getField(superClazz, SERVICE_TOKEN, IBinder.class.getCanonicalName());
            token.setAccessible(true);
            IBinder serviceToken = (IBinder) token.get(service);

            Field instrumentation = getField(realActivityThread.getClass(), INSTRUMENTATION_FIELD, INSTRUMENTATION_PACKAGE);
            instrumentation.setAccessible(true);
            AndromiumInstrumentation andromiumInstrumentation = new AndromiumInstrumentation(andromiumLifecycleCallbacks);
            instrumentation.set(realActivityThread, andromiumInstrumentation);

            ActivityLifecycleManager activityLifecycleManager = new ActivityLifecycleManager(andromiumInstrumentation, realActivityThread, serviceToken);

            IActivityManager aDefault = ActivityManagerNative.getDefault();
            Field mRemote = aDefault.getClass().getDeclaredField("mRemote");
            mRemote.setAccessible(true);
            IBinder remoteBinder = (IBinder) mRemote.get(aDefault);

            Field gDefault = ActivityManagerNative.class.getDeclaredField("gDefault");
            gDefault.setAccessible(true);
            Singleton<IActivityManager> realGDefault = (Singleton<IActivityManager>) gDefault.get(ActivityManagerNative.class);

            Field mInstance = gDefault.getType().getDeclaredField("mInstance");
            mInstance.setAccessible(true);
            mInstance.set(realGDefault, new ADMActivityManagerProxy(remoteBinder, andromiumLifecycleCallbacks));

            return new Pair<>(andromiumInstrumentation, activityLifecycleManager);
        } catch (Exception e) {
            // Something crazy happened, rethrow, or potentially just don't open that app.
            throw new RuntimeException(e);
        }
    }


    // Helper functions

    private static Field getField(Class containingClazz, String fieldName, String packageName) throws NoSuchFieldException {
        Field theField;

        try {
            theField = containingClazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException delayed) {

            Field[] allFields = containingClazz.getDeclaredFields();
            for (Field field : allFields) {
                if (field.getType().getName().equals(packageName)) {
                    return field;
                }
            }

            Log.e(TAG, containingClazz.getSimpleName() + " no longer contains " + packageName);
            throw delayed;
        }

        return theField;
    }

    public static <T extends ContextWrapper> Class<T> getSuperclass(T clazz, String packageName) {
        Class clazzWithActivityThread = clazz.getClass();

        while (clazzWithActivityThread != null && !clazzWithActivityThread.getName().equals(packageName)) {
            clazzWithActivityThread = clazzWithActivityThread.getSuperclass();
        }

        return clazzWithActivityThread;
    }

    private static boolean hasActivityThread() {
        Class activityThreadClazz = null;
        try {
            activityThreadClazz = Class.forName(ACTIVITY_THREAD_PACKAGE);
        } catch (ClassNotFoundException ignored) {
            // Just return
            Log.e(TAG, "ActivityThread class has changed package structure.");
        }

        return activityThreadClazz != null;
    }
}
