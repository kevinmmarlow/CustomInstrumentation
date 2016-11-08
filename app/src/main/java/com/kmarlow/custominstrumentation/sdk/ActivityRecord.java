package com.kmarlow.custominstrumentation.sdk;

import android.app.Activity;
import android.app.LoadedApk;
import android.app.ResultInfo;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.CompatibilityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.view.Window;

import com.android.internal.app.IVoiceInteractor;

import java.util.List;

public class ActivityRecord {
    IBinder token;
    int ident;
    Intent intent;
    String referrer;
    IVoiceInteractor voiceInteractor;
    Bundle state;
    PersistableBundle persistentState;
    Activity activity;
    Window window;
    Activity parent;
    String embeddedID;
    boolean paused;
    boolean stopped;
    boolean hideForNow;

    ActivityInfo activityInfo;
    CompatibilityInfo compatInfo;
    LoadedApk packageInfo;

    List<ResultInfo> pendingResults;
    List<Intent> pendingIntents;

    public ActivityRecord() {
        parent = null;
        embeddedID = null;
        paused = false;
        stopped = false;
        hideForNow = false;
    }
}
