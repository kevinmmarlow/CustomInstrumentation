package android.app;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * THIS IS A SHADOW OF THE ANDROID SOURCE CLASS. IT EXISTS ONLY TO PROVIDE UNHIDDEN
 * REFERENCES TO COMPILE AGAINST.
 */
public class ActivityManager {

    public static class TaskDescription implements Parcelable {

        private TaskDescription(Parcel source) {
            readFromParcel(source);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
        }

        public void readFromParcel(Parcel source) {
        }

        public static final Creator<TaskDescription> CREATOR
                = new Creator<TaskDescription>() {
            public TaskDescription createFromParcel(Parcel source) {
                return new TaskDescription(source);
            }
            public TaskDescription[] newArray(int size) {
                return new TaskDescription[size];
            }
        };
    }

    public static class RecentTaskInfo implements Parcelable {

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
        }

        public void readFromParcel(Parcel source) {
        }

        public static final Creator<RecentTaskInfo> CREATOR
                = new Creator<RecentTaskInfo>() {
            public RecentTaskInfo createFromParcel(Parcel source) {
                return new RecentTaskInfo(source);
            }
            public RecentTaskInfo[] newArray(int size) {
                return new RecentTaskInfo[size];
            }
        };

        private RecentTaskInfo(Parcel source) {
            readFromParcel(source);
        }
    }

    public static class RunningTaskInfo implements Parcelable {

        public RunningTaskInfo() {
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
        }

        public void readFromParcel(Parcel source) {
        }

        public static final Creator<RunningTaskInfo> CREATOR = new Creator<RunningTaskInfo>() {
            public RunningTaskInfo createFromParcel(Parcel source) {
                return new RunningTaskInfo(source);
            }
            public RunningTaskInfo[] newArray(int size) {
                return new RunningTaskInfo[size];
            }
        };

        private RunningTaskInfo(Parcel source) {
            readFromParcel(source);
        }
    }

    public static class TaskThumbnail implements Parcelable {

        public TaskThumbnail() {
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
        }

        public void readFromParcel(Parcel source) {
        }

        public static final Creator<TaskThumbnail> CREATOR = new Creator<TaskThumbnail>() {
            public TaskThumbnail createFromParcel(Parcel source) {
                return new TaskThumbnail(source);
            }
            public TaskThumbnail[] newArray(int size) {
                return new TaskThumbnail[size];
            }
        };

        private TaskThumbnail(Parcel source) {
            readFromParcel(source);
        }
    }

    public static class RunningServiceInfo implements Parcelable {

        public RunningServiceInfo() {
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
        }

        public void readFromParcel(Parcel source) {
        }

        public static final Creator<RunningServiceInfo> CREATOR = new Creator<RunningServiceInfo>() {
            public RunningServiceInfo createFromParcel(Parcel source) {
                return new RunningServiceInfo(source);
            }
            public RunningServiceInfo[] newArray(int size) {
                return new RunningServiceInfo[size];
            }
        };

        private RunningServiceInfo(Parcel source) {
            readFromParcel(source);
        }
    }

    public static class MemoryInfo implements Parcelable {

        public MemoryInfo() {
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
        }

        public void readFromParcel(Parcel source) {
        }

        public static final Creator<MemoryInfo> CREATOR
                = new Creator<MemoryInfo>() {
            public MemoryInfo createFromParcel(Parcel source) {
                return new MemoryInfo(source);
            }
            public MemoryInfo[] newArray(int size) {
                return new MemoryInfo[size];
            }
        };

        private MemoryInfo(Parcel source) {
            readFromParcel(source);
        }
    }

    public static class StackInfo implements Parcelable {

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

        }

        public void readFromParcel(Parcel source) {

        }

        public static final Creator<StackInfo> CREATOR = new Creator<StackInfo>() {
            @Override
            public StackInfo createFromParcel(Parcel source) {
                return new StackInfo(source);
            }
            @Override
            public StackInfo[] newArray(int size) {
                return new StackInfo[size];
            }
        };

        public StackInfo() {
        }

        private StackInfo(Parcel source) {
            readFromParcel(source);
        }

        @Override
        public String toString() {
            return "";
        }
    }

    public static class ProcessErrorStateInfo implements Parcelable {

        public ProcessErrorStateInfo() {
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
        }

        public void readFromParcel(Parcel source) {
        }

        public static final Creator<ProcessErrorStateInfo> CREATOR =
                new Creator<ProcessErrorStateInfo>() {
                    public ProcessErrorStateInfo createFromParcel(Parcel source) {
                        return new ProcessErrorStateInfo(source);
                    }
                    public ProcessErrorStateInfo[] newArray(int size) {
                        return new ProcessErrorStateInfo[size];
                    }
                };

        private ProcessErrorStateInfo(Parcel source) {
            readFromParcel(source);
        }
    }

    public static class RunningAppProcessInfo implements Parcelable {
        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {

        }

        public void readFromParcel(Parcel source) {

        }

        public static final Creator<RunningAppProcessInfo> CREATOR =
                new Creator<RunningAppProcessInfo>() {
                    public RunningAppProcessInfo createFromParcel(Parcel source) {
                        return new RunningAppProcessInfo(source);
                    }
                    public RunningAppProcessInfo[] newArray(int size) {
                        return new RunningAppProcessInfo[size];
                    }
                };

        private RunningAppProcessInfo(Parcel source) {
            readFromParcel(source);
        }
    }
    
}
