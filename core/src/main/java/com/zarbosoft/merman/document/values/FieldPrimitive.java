package com.zarbosoft.merman.document.values;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.visuals.VisualFrontPrimitive;
import com.zarbosoft.merman.syntax.back.BackSpecData;
import com.zarbosoft.merman.syntax.back.BaseBackPrimitiveSpec;

import java.util.HashSet;
import java.util.Set;

public class FieldPrimitive extends Field {
  public VisualFrontPrimitive visual;
  public final BaseBackPrimitiveSpec middle;
  public StringBuilder data;
  public final Set<Listener> listeners = new HashSet<>();

  public FieldPrimitive(final BaseBackPrimitiveSpec middle, final String data) {
    this.middle = middle;
    this.data = new StringBuilder(data);
  }

  @Override
  public boolean selectInto(final Context context) {
    if (context.window) context.windowAdjustMinimalTo(this);
    visual.select(context, true, data.length(), data.length());
    return true;
  }

  @Override
  public Object syntaxLocateStep(String segment) {
    return null;
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
    return middle;
  }

  public interface Listener {
    void set(Context context, String value);

    void added(Context context, int index, String value);

    void removed(Context context, int index, int count);
  }
}
