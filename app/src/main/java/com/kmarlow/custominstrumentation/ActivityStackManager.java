package com.kmarlow.custominstrumentation;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.ArrayMap;

import java.util.Iterator;

public class ActivityStackManager {
    private static final String ANDROMIUM_ROOT_KEY = "Andromium";

    final ArrayMap<String, ActivityRecord> activityStack = new ArrayMap<>();
    private final ADMStack viewStack = ADMStack.single(ANDROMIUM_ROOT_KEY);
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

    public Activity getTop() {
        Iterator<String> reverseIterator = viewStack.reverseIterator();
        if (reverseIterator.hasNext()) {
            return activityStack.get(reverseIterator.next()).activity;
        }

        return null;
    }
}
