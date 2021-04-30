package com.zarbosoft.merman.editorcore.history;

@FunctionalInterface
public interface ModifiedStateListener {
    void changed(boolean modified);
}
