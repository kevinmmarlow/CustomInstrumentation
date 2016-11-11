package com.kmarlow.custominstrumentation.sdk;

import android.app.Activity;
import android.os.IBinder;
import android.util.ArrayMap;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.Iterator;

public class ActivityStackManager {
    private static final String ANDROMIUM_ROOT_KEY = "Andromium";
    private static final String TAG = ActivityStackManager.class.getCanonicalName();

    final ArrayMap<String, ActivityRecord> activityStack = new ArrayMap<>();
    private ADMStack viewStack = ADMStack.single(ANDROMIUM_ROOT_KEY);

    public ActivityStackManager() {
    }

    public boolean isShowingScreen(String className) {
        return className.equals(viewStack.top());
    }

    public ActivityRecord get(String className) {
        return activityStack.get(className);
    }

    public ActivityRecord peekTop() {
        Iterator<String> reverseIterator = viewStack.reverseIterator();
        if (reverseIterator.hasNext()) {
            return activityStack.get(reverseIterator.next());
        }

        return null;
    }

    public ActivityRecord popTop() {
        if (viewStack.size() > 0) {

            ADMStack.Builder builder = viewStack.buildUpon();
            String key = builder.peek();

            if (key == null || key.equals(ANDROMIUM_ROOT_KEY)) {
                return null;
            }

            builder.pop();

            ActivityRecord record = activityStack.get(key);

            viewStack = builder.build();
            activityStack.remove(key);

            return record;
        }

        return null;
    }

    public void addToTop(Activity activity) {
        Class<? extends Activity> activityClass = activity.getClass();

        String className = activityClass.getCanonicalName();
        ADMStack.Builder builder = viewStack.buildUpon();
        builder.push(className);

        viewStack = builder.build();

        Class<Activity> superActivityClazz = AndromiumInstrumentationInjector.getSuperclass(activity, AndromiumInstrumentationInjector.ACTIVITY_PACKAGE);
        if (superActivityClazz == null) {
            throw new IllegalStateException(AndromiumInstrumentationInjector.ACTIVITY_PACKAGE + " not found.");
        }

        IBinder token = null;
        try {
            Method getActivityToken = superActivityClazz.getDeclaredMethod("getActivityToken");
            getActivityToken.setAccessible(true);
            token = (IBinder) getActivityToken.invoke(activity);
        } catch (Exception ignored) {
            Log.e(TAG, "Unable to get token: " + ignored.getLocalizedMessage());
        }

        if (token == null) {
            throw new IllegalStateException("No andromium token set for activity " + activityClass.getSimpleName());
        }

        ActivityRecord record = ADMToken.tokenToActivityRecordLocked(token);
        activityStack.put(className, record);
    }
}
