package com.zarbosoft.merman.webview;

import com.zarbosoft.merman.core.editor.DelayEngine;
import elemental2.dom.DomGlobal;

public class JSDelayEngine implements DelayEngine {
  @Override
  public Handle delay(long ms, Runnable r) {
    return new Handle(
        DomGlobal.setTimeout(
            new DomGlobal.SetTimeoutCallbackFn() {
              @Override
              public void onInvoke(Object... p0) {
                r.run();
              }
            },
            ms));
  }

  public static class Handle implements DelayEngine.Handle {
    final double id;

    public Handle(double id) {
      this.id = id;
    }

    @Override
    public void cancel() {
      DomGlobal.clearTimeout(id);
    }
  }
}
