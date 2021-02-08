package com.zarbosoft.merman.editor;

public interface DelayEngine {
    Handle delay(long ms, Runnable r);

    public interface Handle {
        void cancel();
    }
}
