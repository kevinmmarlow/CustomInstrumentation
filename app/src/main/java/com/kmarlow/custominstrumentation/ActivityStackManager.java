package com.kmarlow.custominstrumentation;

import android.app.Activity;
import android.support.annotation.NonNull;

import java.util.Iterator;
import java.util.Stack;

public class ActivityStackManager {
    private static final String ANDROMIUM_ROOT_KEY = "Andromium";

    private final ADMStack activityStack = ADMStack.single(ANDROMIUM_ROOT_KEY);

    public ActivityStackManager() {
    }

    public void set(@NonNull final String newTopKey) {
        if (newTopKey.equals(activityStack.top())) {
            dispatch(activityStack, Direction.REPLACE);
            return;
        }

        ADMStack.Builder builder = activityStack.buildUpon();
        int count = 0;
        // Search backward to see if we already have newTop on the stack
        String preservedInstance = null;
        for (Iterator<Object> it = activityStack.reverseIterator(); it.hasNext(); ) {
            Object entry = it.next();

            // If we find newTop on the stack, pop back to it.
            if (entry.equals(newTopKey)) {
                for (int i = 0; i < activityStack.size() - count; i++) {
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
            dispatch(newHistory, Direction.BACKWARD);
        } else {
            // newTop was not on the history. Push it on and dispatch.
            builder.push(newTopKey);
            newHistory = builder.build();
            dispatch(newHistory, Direction.FORWARD);
        }
    }

    public boolean isShowingScreen(String className) {
        return className.equals(activityStack.top());
    }
}
