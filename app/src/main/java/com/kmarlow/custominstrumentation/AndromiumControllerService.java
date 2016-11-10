package com.kmarlow.custominstrumentation;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.content.IntentCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.andromium.framework.AndromiumApi;
import com.andromium.framework.ui.AndromiumAdapterFrameworkStub;
import com.andromium.framework.ui.WindowConfig;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

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
}

class AndromiumControllerServiceImpl extends AndromiumApi implements AndromiumLifecycleCallbacks {
    private static final long TIME_BETWEEN_PERMISSION_CHECK_INTERVAL = 60000;
    private AndromiumControllerService controllerService;
    private static long lastPermissionCheckAppUsage = 0;

    AndromiumControllerServiceImpl(AndromiumControllerService controllerService, Intent launchIntent, int appId) {
        super(controllerService, launchIntent, appId);
        this.controllerService = controllerService;

        if (!canDrawOverlays() &&
                (System.currentTimeMillis() - lastPermissionCheckAppUsage)
                        > TIME_BETWEEN_PERMISSION_CHECK_INTERVAL) {
            Log.d("jesse", "this is show app usage dialog");
            showOverlayDialogActivity();
        } else {
            Log.d("jesse", "this is not being hit");
            AndromiumInstrumentationInjector.inject(controllerService, this);
            controllerService.initWindow(this, appId);

            // TODO: Call into the LAUNCHER activity
            // startLauncherActivity();
            Intent intent = new Intent(controllerService, SubActivity.class);
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            controllerService.startActivity(intent);
        }
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
    public void postActivityOnCreate(Activity activity) {
        Log.d("jesse", "this is the post Activity on Create");
        Toast.makeText(activity.getApplicationContext(), "This is postActivityOnCreate", Toast.LENGTH_SHORT).show();
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        controllerService.transitionToScreen(appId, decorView);
        Log.d("jesse", "this is the activity decorWindow: " + decorView);

        //TODO: pass the decor view in and show it on screen. Right now we try to draw an empty window so it doesn't show up because there is nothing to show.
    }

    @Override
    public void postActivityOnResume() {
        Log.d("jesse", "this is the post Activity on resume");
        Toast.makeText(controllerService.getApplicationContext(), "this is postActivityOnResume", Toast.LENGTH_SHORT).show();
    }

    private void showOverlayDialogActivity() {
        Intent intent = new Intent(controllerService, OverlayDialogActivity.class);
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        controllerService.getApplicationContext().startActivity(intent);
    }

    /**
     * Check to see if user have granted appUsageData to Andromium OS
     *
     * @return
     */
    private boolean canDrawOverlays() {
        // for device running kitkat or below, we automatically have app access permission.
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        } else {
            return Settings.canDrawOverlays(controllerService);
        }
    }

}
