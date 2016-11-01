package com.kmarlow.custominstrumentation;

import android.app.Activity;

import java.util.Stack;

public class ADMBackStack {

    private final Stack<Activity> activityBackStack = new Stack<>();

    public ADMBackStack() {
    }

    public boolean addActivityToBackStack(Activity activity) {
        if (activityBackStack.empty()) {
            activityBackStack.add(activity);
            return true;
        }

        String key = activity.getClass().getCanonicalName();
        Activity existing = activityBackStack.peek();

        if (existing.getClass().getCanonicalName().equals(key)) {
            // Activity is already showing
            return false;
        }

        activityBackStack.add(activity);
        return true;
    }

    public Activity removeTopActivity() {
        boolean empty = activityBackStack.empty();
        if (empty) {
            return null;
        }

        Activity peek = activityBackStack.peek();
        if (peek == null) {
            return null;
        }

        return activityBackStack.pop();
    }

    public void clearBackStack() {
        activityBackStack.clear();
    }
}
