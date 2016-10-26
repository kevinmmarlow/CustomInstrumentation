package com.kmarlow.custominstrumentation;

import android.app.Activity;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.andromium.framework.AndromiumApi;
import com.andromium.framework.ui.AndromiumHackFrameworkStub;
import com.andromium.framework.ui.WindowConfig;

public class AndromiumControllerService extends AndromiumHackFrameworkStub {

    private AndromiumControllerServiceImpl app;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
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
    private AndromiumControllerService mContext;
    private int mAppId;

    AndromiumControllerServiceImpl(AndromiumControllerService context, Intent launchIntent, int appId) {
        super(context, launchIntent, appId);
        mContext = context;
        mAppId = appId;
        AndromiumInstrumentation andromiumInstrumentation = AndromiumInstrumentationInjector.inject(context, this);

        Intent intent = new Intent(context, SubActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    public int getAppBodyLayoutXml() {
        return 0;
    }

    @Override
    public void initializeAndPopulateBody(View view) {

    }

    @Override
    public WindowConfig getWindowConfiguration() {
        return new WindowConfig(600, 500, true);
    }

    @Override
    public void postActivityOnCreate(Activity activity) {
        Log.d("jesse", "this is the post Activity on Create");
        Toast.makeText(activity.getApplicationContext(), "This is postActivityOnCreate", Toast.LENGTH_SHORT);
        View decorView = activity.getWindow().getDecorView();
        Log.d("jesse", "this is the activity decorWindow: " + decorView);
        mContext.initWindow(this, appId);

        //TODO: pass the decor view in and show it on screen. Right now we try to draw an empty window so it doesn't show up because there is nothing to show.
    }

    @Override
    public void postActivityOnResume() {
        Log.d("jesse", "this is the post Activity on resume");
        Toast.makeText(mContext.getApplicationContext(), "this is postActivityOnResume", Toast.LENGTH_SHORT);
    }
}
