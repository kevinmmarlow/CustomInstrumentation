package com.kmarlow.custominstrumentation.sdk;

public class Preconditions {

    /**
     * @throws java.lang.IllegalArgumentException if condition is false.
     */
    static void checkArgument(boolean condition, String errorMessage) {
        if (!condition) {
            throw new IllegalArgumentException(errorMessage);
        }
    }
}
