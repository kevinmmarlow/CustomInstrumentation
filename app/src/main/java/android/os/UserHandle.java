package android.os;

import android.annotation.UserIdInt;

/**
 * THIS IS A SHADOW OF THE ANDROID SOURCE CLASS. IT EXISTS ONLY TO PROVIDE UNHIDDEN
 * REFERENCES TO COMPILE AGAINST.
 */
public final class UserHandle implements Parcelable {

    public static final int PER_USER_RANGE = 100000;

    public static final boolean MU_ENABLED = true;

    public static final
    @UserIdInt
    int USER_NULL = -10000;

    public static final
    @UserIdInt
    int USER_SYSTEM = 0;

    final int mHandle;

    public UserHandle(int h) {
        mHandle = h;
    }

    public static
    @UserIdInt
    int myUserId() {
        return getUserId(Process.myUid());
    }

    public static
    @UserIdInt
    int getUserId(int uid) {
        if (MU_ENABLED) {
            return uid / PER_USER_RANGE;
        } else {
            return UserHandle.USER_SYSTEM;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mHandle);
    }

    /**
     * Write a UserHandle to a Parcel, handling null pointers.  Must be
     * read with {@link #readFromParcel(Parcel)}.
     *
     * @param h   The UserHandle to be written.
     * @param out The Parcel in which the UserHandle will be placed.
     * @see #readFromParcel(Parcel)
     */
    public static void writeToParcel(UserHandle h, Parcel out) {
        if (h != null) {
            h.writeToParcel(out, 0);
        } else {
            out.writeInt(USER_NULL);
        }
    }

    /**
     * Read a UserHandle from a Parcel that was previously written
     * with {@link #writeToParcel(UserHandle, Parcel)}, returning either
     * a null or new object as appropriate.
     *
     * @param in The Parcel from which to read the UserHandle
     * @return Returns a new UserHandle matching the previously written
     * object, or null if a null had been written.
     * @see #writeToParcel(UserHandle, Parcel)
     */
    public static UserHandle readFromParcel(Parcel in) {
        int h = in.readInt();
        return h != USER_NULL ? new UserHandle(h) : null;
    }

    public static final Parcelable.Creator<UserHandle> CREATOR
            = new Parcelable.Creator<UserHandle>() {
        public UserHandle createFromParcel(Parcel in) {
            return new UserHandle(in);
        }

        public UserHandle[] newArray(int size) {
            return new UserHandle[size];
        }
    };

    /**
     * Instantiate a new UserHandle from the data in a Parcel that was
     * previously written with {@link #writeToParcel(Parcel, int)}.  Note that you
     * must not use this with data written by
     * {@link #writeToParcel(UserHandle, Parcel)} since it is not possible
     * to handle a null UserHandle here.
     *
     * @param in The Parcel containing the previously written UserHandle,
     *           positioned at the location in the buffer where it was written.
     */
    public UserHandle(Parcel in) {
        mHandle = in.readInt();
    }
}
