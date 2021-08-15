package com.zarbosoft.alligatoroid.compiler;

import com.zarbosoft.rendaw.common.ROList;

public interface Target {
    public TargetCode mergeScoped(Context context, Location location, Iterable<TargetCode> values);
    public TargetCode merge(Context context, Location location,Iterable<TargetCode> values);
}
