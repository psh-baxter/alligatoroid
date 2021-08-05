package com.zarbosoft.merman.editorcore.history;

import com.zarbosoft.merman.core.document.fields.FieldId;

import java.util.ArrayList;
import java.util.List;

public class FileIds {
    private final List<Boolean> fileIds = new ArrayList<>();
    public void remove(int id) {
        fileIds.set(id, false);
    }
    public Integer take(Integer desired) {
        if (desired != null && desired >= 0 && desired < fileIds.size() && !fileIds.get(desired)) return null;
        for (int i = 0; i < fileIds.size(); ++i) {
            if (!fileIds.get(i)) {
                fileIds.set(i, true);
                return i;
            }
        }
        fileIds.add(true);
        return fileIds.size() - 1;
    }
}
