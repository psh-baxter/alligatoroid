package com.zarbosoft.merman.webview;

import com.zarbosoft.merman.editor.DelayEngine;
import def.dom.Globals;

public class JSDelayEngine implements DelayEngine {
  @Override
  public Handle delay(long ms, Runnable r) {
    return new Handle(Globals.window.setTimeout(r, ms));
  }

  public static class Handle implements DelayEngine.Handle {
    final double id;

    public Handle(double id) {
      this.id = id;
    }

    @Override
    public void cancel() {
      Globals.window.clearTimeout(id);
    }
  }
}
