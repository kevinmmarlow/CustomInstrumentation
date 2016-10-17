### Main Learnings

1. Nearly everything in the `android.app.*` package is accessable via reflection.
2. Android System Server code lives in `com.android.server.*`. I have yet to find a way to access code here.
3. All communication between these two layers happens via `IBinder`.
4. The System Server layer handles the `ActivityStack` primarily using `ActivityManagerService` and `ActivityStackSupervisor`. It is possible to see the current running tasks from the package manager, but unsure if this is exactly the same as the `ActivityStack` and again, unsure if we can access the aforementioned classes.
5. We will need to build an AndromiumActivityStack class since we never register the activities with the system.
6. We will also need to manage the Intent lifecycle, and appropriately pass the data along.
7. We **won't** care about display configuration changes that are rotation specific.


### Questions to be Answered

1. What settings/configurations will we need to reflectively pull in order for everything to run smoothly? How is the best way to pull these?
2. How do fragments play into the lifecycle? They seem to just be handled by the Activity class, is this true?
3. How do we handle `BroadcastReceivers`, `ContentProviders`, and custom `Service` classes?
4. Will we need to remove all of the `<activity .../>` fields from their Manifest?
5. What are the questions that we still don't even know to ask?




