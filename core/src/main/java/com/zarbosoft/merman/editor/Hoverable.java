package com.zarbosoft.merman.editor;

import com.zarbosoft.merman.editor.visual.Visual;
import com.zarbosoft.merman.editor.visual.tags.Tags;
import com.zarbosoft.merman.editor.visual.visuals.VisualAtom;
import com.zarbosoft.merman.misc.TSSet;
import com.zarbosoft.merman.syntax.style.Style;

public abstract class Hoverable {
  protected abstract void clear(Context context);

  public abstract void click(Context context);

  public abstract VisualAtom atom();

  public abstract Visual visual();

  public abstract void tagsChanged(Context context);

  public Style getBorderStyle(final Context context, final TSSet<String> tags) {
    return context.getStyle(tags.add(Tags.TAG_HOVER).ro());
  }
}
