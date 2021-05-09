package com.zarbosoft.merman.core.visual.visuals;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.Hoverable;
import com.zarbosoft.merman.core.visual.Visual;
import com.zarbosoft.merman.core.visual.attachments.BorderAttachment;

abstract class ArrayHoverable extends Hoverable {
    public final VisualFrontArray visual;
    final BorderAttachment border;

    ArrayHoverable(VisualFrontArray visual, final Context context) {
        this.visual = visual;
        border = new BorderAttachment(context, context.syntax.hoverStyle.obbox);
    }

    @Override
    protected void clear(final Context context) {
        border.destroy(context);
        if (visual.hoverable == this) visual.hoverable = null;
    }

    @Override
    public Visual visual() {
        return visual;
    }

    public abstract void notifyRangeAdjusted(Context context, int index, int removed, int added);

    public abstract void notifySelected(Context context, int start, int end);
}
