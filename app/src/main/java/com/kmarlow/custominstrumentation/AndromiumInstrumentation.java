package com.kmarlow.custominstrumentation;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.os.UserHandle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static com.kmarlow.custominstrumentation.AndromiumInstrumentationInjector.ACTIVITY_PACKAGE;
import static com.kmarlow.custominstrumentation.AndromiumInstrumentationInjector.getSuperclass;


public class AndromiumInstrumentation extends Instrumentation {
    private static final String TAG = AndromiumInstrumentation.class.getSimpleName();
    private final ActivityManager mActivityManager;
    private final Object mActivityThread;
    private final IBinder serviceToken;

    public AndromiumInstrumentation(Context context, Object realActivityThread, IBinder serviceToken) {
        mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        this.serviceToken = serviceToken;
        mActivityThread = realActivityThread;
    }

    // Intercept Activity construction

    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {
        Toast.makeText(who, "Start " + intent.getComponent().getShortClassName(), Toast.LENGTH_SHORT).show();

        ResolveInfo resolveInfo = who.getPackageManager().resolveActivity(intent, 0);
        ApplicationInfo aInfo = resolveInfo.activityInfo.applicationInfo;

        try {

            Method[] declaredMethods = mActivityThread.getClass().getDeclaredMethods();
            Method packageInfoCheck = null;

            for (Method method : declaredMethods) {
                if (method.getName().equals("getPackageInfoNoCheck")) {
                    packageInfoCheck = method;
                    break;
                }
            }

            if (packageInfoCheck == null) {
                throw new RuntimeException();
            }

            Object loadedApk = packageInfoCheck.invoke(mActivityThread, aInfo, null);
            Method getClassLoader = loadedApk.getClass().getDeclaredMethod("getClassLoader");

            java.lang.ClassLoader cl = (ClassLoader) getClassLoader.invoke(loadedApk);

            Activity activity = (Activity) cl.loadClass(intent.getComponent().getClassName()).newInstance();

            Method makeApplication = loadedApk.getClass().getDeclaredMethod("makeApplication", boolean.class, Instrumentation.class);

            Application application = (Application) makeApplication.invoke(loadedApk, false, this);

            Class<?> activityClientRecord = null;
            Class<?>[] declaredClasses = mActivityThread.getClass().getDeclaredClasses();

            for (Class clazz : declaredClasses) {
                if (clazz.getSimpleName().equals("ActivityClientRecord")) {
                    activityClientRecord = clazz;
                    break;
                }
            }

            if (activityClientRecord == null) {
                throw new RuntimeException();
            }

            Constructor<?> defaultConstructor = activityClientRecord.getDeclaredConstructor();
            defaultConstructor.setAccessible(true);

            Object realActivityClientRecord = defaultConstructor.newInstance();

            Class<?> activityClientRecordClass = realActivityClientRecord.getClass();
            Field packageInfo = activityClientRecordClass.getDeclaredField("packageInfo");
            packageInfo.setAccessible(true);
            packageInfo.set(realActivityClientRecord, loadedApk);

            Field theToken = activityClientRecordClass.getDeclaredField("token");
            theToken.setAccessible(true);

            if (token == null) {
                token = serviceToken;
            }

            theToken.set(realActivityClientRecord, token);

            Method createBaseContextForActivity = mActivityThread.getClass().getDeclaredMethod("createBaseContextForActivity", activityClientRecord, Activity.class);
            createBaseContextForActivity.setAccessible(true);
            Context appContext = (Context) createBaseContextForActivity.invoke(mActivityThread, realActivityClientRecord, activity);

            CharSequence title = resolveInfo.activityInfo.loadLabel(appContext.getPackageManager());

            Class superActivityClazz = getSuperclass(activity, ACTIVITY_PACKAGE);
            if (superActivityClazz == null) {
                return null;
            }

            Class nonConfigInstances = null;
            Class<?>[] activityInnerClasses = superActivityClazz.getDeclaredClasses();
            for (Class clazz : activityInnerClasses) {
                if (clazz.getSimpleName().equals("NonConfigurationInstances")) {
                    nonConfigInstances = clazz;
                    break;
                }
            }

            if (nonConfigInstances == null) {
                throw new RuntimeException();
            }

            Class<?> voiceInteractorClass = Class.forName("com.android.internal.app.IVoiceInteractor");

            Method attachMethod = null;

            try {
                attachMethod = superActivityClazz.getDeclaredMethod("attach", Context.class, mActivityThread.getClass(),
                        Instrumentation.class, IBinder.class, int.class, Application.class, Intent.class,
                        ActivityInfo.class, CharSequence.class, Activity.class, String.class, nonConfigInstances,
                        Configuration.class, String.class, voiceInteractorClass);
            } catch (Exception ignored) {
                Log.wtf(TAG, ignored.getLocalizedMessage());
            }

            if (attachMethod == null) {
                Method[] declaredMethods1 = superActivityClazz.getDeclaredMethods();
                for (Method method : declaredMethods1) {
                    if (method.getName().equals("attach")) {
                        attachMethod = method;
                        break;
                    }
                }
            }

            if (attachMethod == null) {
                throw new RuntimeException();
            }

            // TODO: Pull parent somehow
            Activity parent = null;

            attachMethod.setAccessible(true);
            attachMethod.invoke(activity, appContext, mActivityThread, this, serviceToken,
                    0, application, intent, resolveInfo.activityInfo, title, parent,
                    null, null, new Configuration(),
                    null, null);

            int theme = resolveInfo.activityInfo.getThemeResource();
            if (theme != 0) {
                activity.setTheme(theme);
            }

            // Basically, we should keep a track of the activities ourselves,
            // manage the parent/child flow, and setup the next intent appropriately

            Field called = superActivityClazz.getDeclaredField("mCalled");
            called.setAccessible(true);
            called.set(activity, false);

            // TODO: Save the bundle when we should stop the activity, "restore" it when we start
            callActivityOnCreate(activity, null /* SAVED BUNDLE HERE */);

            boolean calledSet = (boolean) called.get(activity);
            if (!calledSet) {
                throw new RuntimeException("Activity " + intent.getComponent().toShortString() +
                        " did not call through to super.onCreate()");
            }

        } catch (Exception e) {
            Log.wtf(TAG, e);
            Toast.makeText(who, "Andromium is unsupported on this version", Toast.LENGTH_SHORT).show();
        }

        return null;
    }

    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, String target,
            Intent intent, int requestCode, Bundle options) {
        Toast.makeText(who, "Start " + intent.getComponent().getShortClassName(), Toast.LENGTH_SHORT).show();

//        try {
//            Class<Instrumentation> instrumentation = (Class<Instrumentation>) getClass().getSuperclass();
//            Method execStartActivity = instrumentation.getDeclaredMethod("execStartActivity", new Class[]{Context.class, IBinder.class, IBinder.class, String.class, Intent.class, int.class, Bundle.class});
//            execStartActivity.setAccessible(true);
//
//            return (ActivityResult) execStartActivity.invoke(this, who, contextThread, token, target, intent, requestCode, options);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        return null;
    }

    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options, UserHandle user) {
        Toast.makeText(who, "Start " + intent.getComponent().getShortClassName(), Toast.LENGTH_SHORT).show();

//        try {
//            Class<Instrumentation> instrumentation = (Class<Instrumentation>) getClass().getSuperclass();
//            Method execStartActivity = instrumentation.getDeclaredMethod("execStartActivity", new Class[]{Context.class, IBinder.class, IBinder.class, Activity.class, Intent.class, int.class, Bundle.class, UserHandle.class});
//            execStartActivity.setAccessible(true);
//
//            return (ActivityResult) execStartActivity.invoke(this, who, contextThread, token, target, intent, requestCode, options, user);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        return null;
    }


    // Intercept activity attach

    @Override
    public Activity newActivity(Class<?> clazz, Context context,
                                IBinder token, Application application, Intent intent, ActivityInfo info,
                                CharSequence title, Activity parent, String id,
                                Object lastNonConfigurationInstance) throws InstantiationException, IllegalAccessException {
        Log.e(TAG, "newActivity -> " + clazz.getName());
        Activity activity = super.newActivity(clazz, context, token, application, intent, info, title, parent, id, lastNonConfigurationInstance);

        Window window = activity.getWindow();
        View view = window.peekDecorView();

        Log.i(TAG, "WINDOW: " + window.getClass().getName() + ". DecorView: " + view.getClass().getName());
        return activity;
    }
}
