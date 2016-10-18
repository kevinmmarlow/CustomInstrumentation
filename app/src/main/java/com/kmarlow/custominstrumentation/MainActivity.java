package com.kmarlow.custominstrumentation;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

    public MainActivity() {
// AndromiumInstrumentationInjector.inject(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
