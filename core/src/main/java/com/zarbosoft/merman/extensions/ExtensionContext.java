package com.zarbosoft.merman.extensions;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.details.DetailsPage;
import com.zarbosoft.merman.editor.gap.GapCompletionEngine;
import com.zarbosoft.merman.syntax.Syntax;

public class ExtensionContext {
  private final Context context;

  public GapCompletionEngine gapCompletionEngine;

  public ExtensionContext(Context context) {
    this.context = context;
  }

  public Syntax syntax() {
    return context.syntax;
  }

  public void addSelectionListener(final Context.SelectionListener listener) {
    context.selectionListeners.add(listener);
  }

  public void removeSelectionListener(final Context.SelectionListener listener) {
    context.selectionListeners.remove(listener);
  }

  public void addHoverListener(final Context.HoverListener listener) {
    context.hoverListeners.add(listener);
  }

  public void removeHoverListener(final Context.HoverListener listener) {
    context.hoverListeners.remove(listener);
  }

  public void addBannerPage(DetailsPage page) {
    context.details.addPage(context, page);
  }

  public void removeBannerPage(DetailsPage page) {
    context.details.removePage(context, page);
  }
}
