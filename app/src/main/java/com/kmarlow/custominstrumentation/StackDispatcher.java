package com.kmarlow.custominstrumentation;

public interface StackDispatcher {

    enum Direction {
        REPLACE,
        FORWARD,
        BACKWARD
    }
}
