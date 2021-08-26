package com.zarbosoft.merman.core.document.fields;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.visual.visuals.VisualFieldPrimitive;
import com.zarbosoft.merman.core.syntax.back.BackSpecData;
import com.zarbosoft.merman.core.syntax.back.BaseBackPrimitiveSpec;

import java.util.HashSet;
import java.util.Set;

public class FieldPrimitive extends Field {
  public final BaseBackPrimitiveSpec back;
  public final Set<Listener> listeners = new HashSet<>();
  public VisualFieldPrimitive visual;
  public StringBuilder data;

  public FieldPrimitive(final BaseBackPrimitiveSpec back, final String data) {
    this.back = back;
    this.data = new StringBuilder(data);
  }

  @Override
  public boolean selectInto(final Context context) {
    if (context.window) context.windowAdjustMinimalTo(this);
    visual.select(context, true, data.length(), data.length());
    return true;
  }

  public boolean selectInto(final Context context, boolean leadFirst, int start, int end) {
    if (context.window) context.windowAdjustMinimalTo(this);
    visual.select(context, leadFirst, start, end);
    return true;
  }

  @Override
  public Object syntaxLocateStep(Context.SyntaxLocateQueue segments) {
    segments.consumeRemaining();
    return this;
  }

  public void addListener(final Listener listener) {
    listeners.add(listener);
  }

  public void removeListener(final Listener listener) {
    listeners.remove(listener);
  }

  public String get() {
    return data.toString();
  }

  public int length() {
    return data.length();
  }

  @Override
  public BackSpecData back() {
    return back;
  }

  public interface Listener {
    void changed(Context context, int index, int remove, String add);
  }
}
