package com.zarbosoft.merman.editor.gap;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.extensions.ExtensionContext;

import java.util.List;

public interface GapCompletionEngine {
  public State createGapCompletionState(ExtensionContext context, String baseType);

  public State createSuffixGapCompletionState(
      ExtensionContext context, List<Atom> preceding, String baseType);

  public static interface State {
    public void update(Context context, String text);

    public List<TwoColumnChoice> choices();
  }
}
