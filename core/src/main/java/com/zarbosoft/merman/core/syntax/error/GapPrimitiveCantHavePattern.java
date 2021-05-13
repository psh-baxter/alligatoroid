package com.zarbosoft.merman.core.syntax.error;

public class GapPrimitiveCantHavePattern extends BaseKVError{
    public GapPrimitiveCantHavePattern(String gapId) {
        put("gap", gapId);
    }

    @Override
    protected String description() {
        return "gap primitive has pattern";
    }
}
