package com.kmarlow.custominstrumentation;

import android.app.Activity;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.ArrayMap;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.Iterator;

import static com.kmarlow.custominstrumentation.AndromiumInstrumentationInjector.ACTIVITY_PACKAGE;
import static com.kmarlow.custominstrumentation.AndromiumInstrumentationInjector.getSuperclass;

public class ActivityStackManager {
    private static final String ANDROMIUM_ROOT_KEY = "Andromium";

    final ArrayMap<String, ActivityRecord> activityStack = new ArrayMap<>();
    private ADMStack viewStack = ADMStack.single(ANDROMIUM_ROOT_KEY);
    private final StackDispatcher dispatcher;

    public ActivityStackManager(StackDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void set(@NonNull final String newTopKey) {
        if (newTopKey.equals(viewStack.top())) {
            dispatch(viewStack, StackDispatcher.Direction.REPLACE);
            return;
        }

        ADMStack.Builder builder = viewStack.buildUpon();
        int count = 0;
        // Search backward to see if we already have newTop on the stack
        String preservedInstance = null;
        for (Iterator<Object> it = viewStack.reverseIterator(); it.hasNext(); ) {
            Object entry = it.next();

            // If we find newTop on the stack, pop back to it.
            if (entry.equals(newTopKey)) {
                for (int i = 0; i < viewStack.size() - count; i++) {
                    preservedInstance = builder.pop();
                }
                break;
            } else {
                count++;
            }
        }

        ADMStack newHistory;
        if (preservedInstance != null) {
            // newTop was on the history. Put the preserved instance back on and dispatch.
            builder.push(preservedInstance);
            newHistory = builder.build();
            dispatch(newHistory, StackDispatcher.Direction.BACKWARD);
        } else {
            // newTop was not on the history. Push it on and dispatch.
            builder.push(newTopKey);
            newHistory = builder.build();
            dispatch(newHistory, StackDispatcher.Direction.FORWARD);
        }
    }

    private void dispatch(ADMStack viewStack, StackDispatcher.Direction direction) {
    }

    public boolean isShowingScreen(String className) {
        return className.equals(viewStack.top());
    }

    public ActivityRecord get(String className) {
        return activityStack.get(className);
    }

    public Activity peekTop() {
        Iterator<String> reverseIterator = viewStack.reverseIterator();
        if (reverseIterator.hasNext()) {
            ActivityRecord record = activityStack.get(reverseIterator.next());
            return record == null ? null : record.activity;
        }

        return null;
    }

    public Activity popTop() {
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

            return record == null ? null : record.activity;
        }

        return null;
    }

    public void addToTop(Activity activity) {
        Class<? extends Activity> activityClass = activity.getClass();

        String className = activityClass.getCanonicalName();
        ADMStack.Builder builder = viewStack.buildUpon();
        builder.push(className);

        viewStack = builder.build();

        Class<Activity> superActivityClazz = getSuperclass(activity, ACTIVITY_PACKAGE);
        if (superActivityClazz == null) {
            throw new IllegalStateException(ACTIVITY_PACKAGE + " not found.");
        }

        IBinder token = null;
        try {
            Method getActivityToken = superActivityClazz.getDeclaredMethod("getActivityToken");
            getActivityToken.setAccessible(true);
            token = (IBinder) getActivityToken.invoke(activity);
        } catch (Exception ignored) {
            Log.e("KEVIN", "Unable to get token: " + ignored.getLocalizedMessage());
        }

        if (token == null) {
            throw new IllegalStateException("No andromium token set for activity " + activityClass.getSimpleName());
        }

        ActivityRecord record = ADMToken.tokenToActivityRecordLocked(token);
        activityStack.put(className, record);
    }
}
