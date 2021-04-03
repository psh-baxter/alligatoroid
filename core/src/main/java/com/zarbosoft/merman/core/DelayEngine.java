package com.zarbosoft.merman.core;

public interface DelayEngine {
    Handle delay(long ms, Runnable r);

    public interface Handle {
        void cancel();
    }
}
