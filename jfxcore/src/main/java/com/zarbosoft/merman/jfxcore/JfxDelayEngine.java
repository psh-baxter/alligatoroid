package com.zarbosoft.merman.jfxcore;

import com.zarbosoft.merman.core.editor.DelayEngine;
import javafx.application.Platform;

import java.util.Timer;
import java.util.TimerTask;

public class JfxDelayEngine implements DelayEngine {
  @Override
  public Handle delay(long ms, Runnable r) {
    Timer t = new Timer();
    try {
      t.schedule(
          new TimerTask() {
            @Override
            public void run() {
              Platform.runLater(r);
            }
          },
          ms);
    } catch (IllegalStateException ignore) {
      // When trying to schedule while shutting down
    }
    return new Handle(t);
  }

  public static class Handle implements DelayEngine.Handle {
    private final Timer t;

    public Handle(Timer t) {
      this.t = t;
    }

    @Override
    public void cancel() {
      t.cancel();
    }
  }
}
