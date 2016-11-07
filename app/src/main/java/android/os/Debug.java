package android.os;

public class Debug {

    public static class MemoryInfo implements Parcelable {
        /** The proportional set size for dalvik heap.  (Doesn't include other Dalvik overhead.) */
        public int dalvikPss;
        /** The proportional set size that is swappable for dalvik heap. */
        /** @hide We may want to expose this, eventually. */
        public int dalvikSwappablePss;
        /** The private dirty pages used by dalvik heap. */
        public int dalvikPrivateDirty;
        /** The shared dirty pages used by dalvik heap. */
        public int dalvikSharedDirty;
        /** The private clean pages used by dalvik heap. */
        /** @hide We may want to expose this, eventually. */
        public int dalvikPrivateClean;
        /** The shared clean pages used by dalvik heap. */
        /** @hide We may want to expose this, eventually. */
        public int dalvikSharedClean;
        /** The dirty dalvik pages that have been swapped out. */
        /** @hide We may want to expose this, eventually. */
        public int dalvikSwappedOut;

        /** The proportional set size for the native heap. */
        public int nativePss;
        /** The proportional set size that is swappable for the native heap. */
        /** @hide We may want to expose this, eventually. */
        public int nativeSwappablePss;
        /** The private dirty pages used by the native heap. */
        public int nativePrivateDirty;
        /** The shared dirty pages used by the native heap. */
        public int nativeSharedDirty;
        /** The private clean pages used by the native heap. */
        /** @hide We may want to expose this, eventually. */
        public int nativePrivateClean;
        /** The shared clean pages used by the native heap. */
        /** @hide We may want to expose this, eventually. */
        public int nativeSharedClean;
        /** The dirty native pages that have been swapped out. */
        /** @hide We may want to expose this, eventually. */
        public int nativeSwappedOut;

        /** The proportional set size for everything else. */
        public int otherPss;
        /** The proportional set size that is swappable for everything else. */
        /** @hide We may want to expose this, eventually. */
        public int otherSwappablePss;
        /** The private dirty pages used by everything else. */
        public int otherPrivateDirty;
        /** The shared dirty pages used by everything else. */
        public int otherSharedDirty;
        /** The private clean pages used by everything else. */
        /** @hide We may want to expose this, eventually. */
        public int otherPrivateClean;
        /** The shared clean pages used by everything else. */
        /** @hide We may want to expose this, eventually. */
        public int otherSharedClean;
        /** The dirty pages used by anyting else that have been swapped out. */
        /** @hide We may want to expose this, eventually. */
        public int otherSwappedOut;

        /** @hide */
        public static final int NUM_OTHER_STATS = 16;

        /** @hide */
        public static final int NUM_DVK_STATS = 5;

        /** @hide */
        public static final int NUM_CATEGORIES = 7;

        /** @hide */
        public static final int offsetPss = 0;
        /** @hide */
        public static final int offsetSwappablePss = 1;
        /** @hide */
        public static final int offsetPrivateDirty = 2;
        /** @hide */
        public static final int offsetSharedDirty = 3;
        /** @hide */
        public static final int offsetPrivateClean = 4;
        /** @hide */
        public static final int offsetSharedClean = 5;
        /** @hide */
        public static final int offsetSwappedOut = 6;

        private int[] otherStats = new int[(NUM_OTHER_STATS+NUM_DVK_STATS)*NUM_CATEGORIES];

        public MemoryInfo() {
        }

        /**
         * Return total PSS memory usage in kB.
         */
        public int getTotalPss() {
            return dalvikPss + nativePss + otherPss;
        }

        /**
         * @hide Return total PSS memory usage in kB.
         */
        public int getTotalUss() {
            return dalvikPrivateClean + dalvikPrivateDirty
                    + nativePrivateClean + nativePrivateDirty
                    + otherPrivateClean + otherPrivateDirty;
        }

        /**
         * Return total PSS memory usage in kB.
         */
        public int getTotalSwappablePss() {
            return dalvikSwappablePss + nativeSwappablePss + otherSwappablePss;
        }

        /**
         * Return total private dirty memory usage in kB.
         */
        public int getTotalPrivateDirty() {
            return dalvikPrivateDirty + nativePrivateDirty + otherPrivateDirty;
        }

        /**
         * Return total shared dirty memory usage in kB.
         */
        public int getTotalSharedDirty() {
            return dalvikSharedDirty + nativeSharedDirty + otherSharedDirty;
        }

        /**
         * Return total shared clean memory usage in kB.
         */
        public int getTotalPrivateClean() {
            return dalvikPrivateClean + nativePrivateClean + otherPrivateClean;
        }

        /**
         * Return total shared clean memory usage in kB.
         */
        public int getTotalSharedClean() {
            return dalvikSharedClean + nativeSharedClean + otherSharedClean;
        }

        /**
         * Return total swapped out memory in kB.
         * @hide
         */
        public int getTotalSwappedOut() {
            return dalvikSwappedOut + nativeSwappedOut + otherSwappedOut;
        }

        /** @hide */
        public int getOtherPss(int which) {
            return otherStats[which*NUM_CATEGORIES + offsetPss];
        }


        /** @hide */
        public int getOtherSwappablePss(int which) {
            return otherStats[which*NUM_CATEGORIES + offsetSwappablePss];
        }


        /** @hide */
        public int getOtherPrivateDirty(int which) {
            return otherStats[which*NUM_CATEGORIES + offsetPrivateDirty];
        }

        /** @hide */
        public int getOtherSharedDirty(int which) {
            return otherStats[which*NUM_CATEGORIES + offsetSharedDirty];
        }

        /** @hide */
        public int getOtherPrivateClean(int which) {
            return otherStats[which*NUM_CATEGORIES + offsetPrivateClean];
        }

        /** @hide */
        public int getOtherSharedClean(int which) {
            return otherStats[which*NUM_CATEGORIES + offsetSharedClean];
        }

        /** @hide */
        public int getOtherSwappedOut(int which) {
            return otherStats[which*NUM_CATEGORIES + offsetSwappedOut];
        }

        /** @hide */
        public static String getOtherLabel(int which) {
            switch (which) {
                case 0: return "Dalvik Other";
                case 1: return "Stack";
                case 2: return "Cursor";
                case 3: return "Ashmem";
                case 4: return "Other dev";
                case 5: return ".so mmap";
                case 6: return ".jar mmap";
                case 7: return ".apk mmap";
                case 8: return ".ttf mmap";
                case 9: return ".dex mmap";
                case 10: return "code mmap";
                case 11: return "image mmap";
                case 12: return "Other mmap";
                case 13: return "Graphics";
                case 14: return "GL";
                case 15: return "Memtrack";
                case 16: return ".Heap";
                case 17: return ".LOS";
                case 18: return ".LinearAlloc";
                case 19: return ".GC";
                case 20: return ".JITCache";
                default: return "????";
            }
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(dalvikPss);
            dest.writeInt(dalvikSwappablePss);
            dest.writeInt(dalvikPrivateDirty);
            dest.writeInt(dalvikSharedDirty);
            dest.writeInt(dalvikPrivateClean);
            dest.writeInt(dalvikSharedClean);
            dest.writeInt(dalvikSwappedOut);
            dest.writeInt(nativePss);
            dest.writeInt(nativeSwappablePss);
            dest.writeInt(nativePrivateDirty);
            dest.writeInt(nativeSharedDirty);
            dest.writeInt(nativePrivateClean);
            dest.writeInt(nativeSharedClean);
            dest.writeInt(nativeSwappedOut);
            dest.writeInt(otherPss);
            dest.writeInt(otherSwappablePss);
            dest.writeInt(otherPrivateDirty);
            dest.writeInt(otherSharedDirty);
            dest.writeInt(otherPrivateClean);
            dest.writeInt(otherSharedClean);
            dest.writeInt(otherSwappedOut);
            dest.writeIntArray(otherStats);
        }

        public void readFromParcel(Parcel source) {
            dalvikPss = source.readInt();
            dalvikSwappablePss = source.readInt();
            dalvikPrivateDirty = source.readInt();
            dalvikSharedDirty = source.readInt();
            dalvikPrivateClean = source.readInt();
            dalvikSharedClean = source.readInt();
            dalvikSwappedOut = source.readInt();
            nativePss = source.readInt();
            nativeSwappablePss = source.readInt();
            nativePrivateDirty = source.readInt();
            nativeSharedDirty = source.readInt();
            nativePrivateClean = source.readInt();
            nativeSharedClean = source.readInt();
            nativeSwappedOut = source.readInt();
            otherPss = source.readInt();
            otherSwappablePss = source.readInt();
            otherPrivateDirty = source.readInt();
            otherSharedDirty = source.readInt();
            otherPrivateClean = source.readInt();
            otherSharedClean = source.readInt();
            otherSwappedOut = source.readInt();
            otherStats = source.createIntArray();
        }

        public static final Creator<MemoryInfo> CREATOR = new Creator<MemoryInfo>() {
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
}
