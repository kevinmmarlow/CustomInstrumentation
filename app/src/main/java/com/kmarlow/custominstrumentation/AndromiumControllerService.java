package com.kmarlow.custominstrumentation;

import android.content.Intent;
import android.os.IBinder;
import android.view.View;

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

class AndromiumControllerServiceImpl extends AndromiumApi {

    AndromiumControllerServiceImpl(AndromiumControllerService context, Intent launchIntent, int appId) {
        super(context, launchIntent, appId);
        AndromiumInstrumentation andromiumInstrumentation = AndromiumInstrumentationInjector.inject(context);

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
}
