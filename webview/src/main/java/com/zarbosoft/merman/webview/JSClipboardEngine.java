package com.zarbosoft.merman.webview;

import com.zarbosoft.merman.core.ClipboardEngine;
import com.zarbosoft.merman.core.syntax.BackType;
import com.zarbosoft.merman.webview.compat.CompatOverlay;
import elemental2.dom.DataTransfer;
import elemental2.dom.DataTransferItem;
import elemental2.promise.IThenable;

import java.util.function.Consumer;

public class JSClipboardEngine extends ClipboardEngine {
  public final String mime;

  public JSClipboardEngine(BackType backType) {
    this.mime = backType.mime();
  }

  @Override
  public void set(Object bytes) {
    CompatOverlay.mmCopy(mime, (String)bytes);
  }

  @Override
  public void get(Consumer<Object> cb) {
    CompatOverlay.mmUncopy()
        .then(
            new IThenable.ThenOnFulfilledCallbackFn<DataTransfer, Object>() {
              @Override
              public IThenable<Object> onInvoke(DataTransfer d) {
                DataTransferItem backup = null;
                for (int i = 0; i < d.items.length; ++i) {
                  DataTransferItem f = d.items.getAt(i);
                  if (mime.equals(f.type)) {
                    f.getAsString(
                        new DataTransferItem.GetAsStringCallbackFn() {
                          @Override
                          public Object onInvoke(String p0) {
                            cb.accept(p0);
                            return null;
                          }
                        });
                    return null;
                  } else if (backup == null && "text/plain".equals(f.type)) {
                    backup = f;
                  }
                }
                if (backup != null) {
                  backup.getAsString(
                      s -> {
                        cb.accept(s);
                        return null;
                      });
                }
                return null;
              }
            });
  }

  @Override
  public void getString(Consumer<String> cb) {
    CompatOverlay.mmUncopyText()
        .then(
            new IThenable.ThenOnFulfilledCallbackFn<String, Object>() {
              @Override
              public IThenable<Object> onInvoke(String p0) {
                cb.accept(p0);
                return null;
              }
            });
  }

  @Override
  public void setString(String string) {
    CompatOverlay.mmCopyText(string);
  }
}
