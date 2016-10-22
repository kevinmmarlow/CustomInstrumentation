# Overriding the Instrumentation class in Android, a How-To.

First off, what is Instrumentation, and why do we want to override it? Let's look at two classes inside of Android.

1. ActivityThread  
   - Responsible for managing the execution of the main thread in an application process, scheduling and executing activities, broadcasts, and other operations on it as the activity manager requests.

2. Instrumentation  
  - Base class for implementing application callbacks code.  When running with callbacks turned on, this class will be instantiated for you before any of the application code, allowing you to monitor all of the interaction the system has with the application.  An Instrumentation implementation is described to the system through an AndroidManifest.xml's <callbacks> tag.

Now, from these descriptions, one would think that ActivityThread is responsible for talking to Activities, Services, and the Application as a whole.
One would also think that Instrumentation must sit as an observer to ActivityThread in order to profile, right? **Nope**

In reality, `Instrumentation.java` sits as a middle man between `ActivityThread` and the application's running `Activity` classes*. 
Calls made by the system are issued to the `ActivityManagerNative`, which in turn, just calls back into `Instrumentation.java` as seen below.

```java
public void startActivity(Context context, Intent intent, Bundle options) {
    ActivityThread thread = ActivityThread.currentActivityThread();
    thread.getInstrumentation().execStartActivityFromAppTask(context,
        thread.getApplicationThread(), mAppTaskImpl, intent, options);
}
```

* There might be a caveat to this rule when dealing with `startActivityForResult` return values.

So, as you can see, if we could somehow replace `Instrumentation.java` with our own version, then we could HARNESS THE POWERS OF THE UNIVERSE (of Android).

#### Okay, but how do I replace it?
To be continued... or look at the code for now.
