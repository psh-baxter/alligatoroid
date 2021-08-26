package com.zarbosoft.merman.core.document.fields;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.syntax.back.BackIdSpec;
import com.zarbosoft.merman.core.syntax.back.BackSpecData;

import java.util.HashSet;
import java.util.Set;

public class FieldId extends Field {
  public final BackIdSpec back;
  public final Set<Listener> listeners = new HashSet<>();
  public int id;

  public FieldId(final BackIdSpec back, final int id) {
    this.back = back;
    this.id = id;
  }

  @Override
  public boolean selectInto(final Context context) {
    return false;
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

  @Override
  public BackSpecData back() {
    return back;
  }

  public interface Listener {
    void changed(Context context, int newId);
  }
}
