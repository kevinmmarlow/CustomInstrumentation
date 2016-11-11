package com.kmarlow.custominstrumentation.sdk;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.IntentCompat;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.andromium.framework.AndromiumApi;
import com.andromium.framework.ui.AndromiumAdapterFrameworkStub;
import com.andromium.framework.ui.WindowConfig;
import com.kmarlow.custominstrumentation.SubActivity;

public class AndromiumControllerService extends AndromiumAdapterFrameworkStub {

    private AndromiumControllerServiceImpl app;

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public AndromiumApi getAndromiumAppInstance(int appId) {
        if (app == null) {
            app = new AndromiumControllerServiceImpl(this, launchIntent, appId);
        }
        return app;
    }

    public void performClose(int appId) {
        close(appId);
    }
}

class AndromiumControllerServiceImpl extends AndromiumApi implements AndromiumLifecycleCallbacks {
    private static final String TAG = AndromiumControllerServiceImpl.class.getSimpleName();

    private final AndromiumControllerService controllerService;
    private final ActivityStackManager stackManager;
    private final AndromiumInstrumentation instrumentation;
    private final ActivityLifecycleManager lifecycleManager;

    AndromiumControllerServiceImpl(AndromiumControllerService controllerService, Intent launchIntent, int appId) {
        super(controllerService, launchIntent, appId);
        this.controllerService = controllerService;
        this.stackManager = new ActivityStackManager();

        // Uncomment to check for leaks
        // RefWatcher refWatcher = InstApplication.getRefWatcher(controllerService);
        // refWatcher.watch(this);

        Pair<AndromiumInstrumentation, ActivityLifecycleManager> pair = AndromiumInstrumentationInjector.inject(controllerService, this);
        if (pair == null) {
            throw new IllegalStateException("Unable to inject Andromium instrumentation");
        }
        this.instrumentation = pair.first;
        this.lifecycleManager = pair.second;

        controllerService.initWindow(this, appId);

        // TODO: Call into the LAUNCHER activity
        // startLauncherActivity();
        Intent intent = new Intent(controllerService, SubActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        controllerService.startActivity(intent);
    }

    private void startLauncherActivity() {
        final PackageManager pm = controllerService.getPackageManager();
        Intent launcherIntent = pm.getLaunchIntentForPackage(context.getPackageName());

        if (launcherIntent == null) {
            throw new IllegalStateException("No launcher activity found for " + context.getPackageName());
        }

        ComponentName componentName = launcherIntent.getComponent();
        // Make sure to clear the stack of activities.
        Intent mainIntent = IntentCompat.makeRestartActivityTask(componentName);
        context.startActivity(mainIntent);
    }

    @Override
    public int getAppBodyLayoutXml() {
        // No-op
        return 0;
    }

    @Override
    public void initializeAndPopulateBody(View view) {
        // No-op
    }

    @Override
    public WindowConfig getWindowConfiguration() {
        return new WindowConfig(600, 500, true);
    }

    @Override
    public void execStartActivity(Context who, IBinder token, Intent intent) {
        Toast.makeText(who, "Start " + intent.getComponent().getShortClassName(), Toast.LENGTH_SHORT).show();

        if (controllerService.isTransitioning()) {
            return;
        }

        String className = intent.getComponent().getClassName();
        boolean showingScreen = stackManager.isShowingScreen(className);
        if (showingScreen) {

            ActivityRecord activityRecord = stackManager.get(className);
            // Activity exists in back stack, just redeliver intent.
            instrumentation.callActivityOnNewIntent(activityRecord.activity, intent);
            return;
        }

        ActivityRecord current = stackManager.peekTop();
        if (current != null && current.activity != null) {
            Bundle outState = lifecycleManager.pauseAndStopActivity(current.activity);
            current.state = outState;
            // lifecycleManager.finishActivity(current);
        }

        Activity activity = lifecycleManager.createAndStartActivity(who, token, intent);
        if (activity != null) {
            stackManager.addToTop(activity);
            // TODO: Figure out the best way to manage view stack and how to add this activity to the stack manager.
        }
    }

    @Override
    public boolean attemptFinishActivity(IBinder token, int resultCode, Intent resultData, boolean finishTask) {
        if (controllerService.isTransitioning()) {
            return false;
        }

        ADMToken admToken = (ADMToken) token;
        ActivityRecord currentRecord = ADMToken.tokenToActivityRecordLocked(admToken);

        ActivityRecord current = stackManager.popTop();
        if (current != null && current.activity != null) {
            // Don't keep state, this is a full tear down.
            lifecycleManager.pauseAndStopActivity(current.activity);
            lifecycleManager.finishActivity(current.activity);
        }

        ActivityRecord previous = stackManager.peekTop();

        if (previous == null) {
            controllerService.performClose(appId);
        } else if (previous.activity != null) {
            try {
                lifecycleManager.restartActivity(previous.activity);
            } catch (Exception error) {
                Log.e(TAG, error.getLocalizedMessage());
            }
        }

        return true;
    }

    @Override
    public void postActivityOnCreate(Activity activity) {
        Log.d(TAG, "This is the post Activity on Create");
        Toast.makeText(activity.getApplicationContext(), "This is postActivityOnCreate", Toast.LENGTH_SHORT).show();
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        controllerService.transitionToScreen(appId, decorView);
        Log.d(TAG, "This is the activity decorWindow: " + decorView);

        //TODO: pass the decor view in and show it on screen. Right now we try to draw an empty window so it doesn't show up because there is nothing to show.
    }

    @Override
    public void postActivityOnResume() {
        Log.d(TAG, "This is the post Activity on resume");
        Toast.makeText(controllerService.getApplicationContext(), "This is postActivityOnResume", Toast.LENGTH_SHORT).show();
    }
}
