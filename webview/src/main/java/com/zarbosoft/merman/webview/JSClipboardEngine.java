package com.zarbosoft.merman.webview;

import com.zarbosoft.merman.editor.ClipboardEngine;
import com.zarbosoft.merman.syntax.BackType;
import def.dom.DataTransfer;
import def.dom.DataTransferItem;
import def.js.Promise;

import java.util.function.Consumer;

import static jsweet.util.Lang.$insert;

public class JSClipboardEngine extends ClipboardEngine {
  public final String mime;

  public JSClipboardEngine(BackType backType) {
    this.mime = backType.mime();
  }

  @Override
  public void set(Object bytes) {
    $insert(
        "(navigator.clipboard as any).write(["
            + "new (window as any).ClipboardItem({[this.mime]: bytes})"
            + "]);");
  }

  @Override
  public void get(Consumer<Object> cb) {
    ((Promise<DataTransfer>) $insert("(navigator.clipboard as any).read()"))
        .then(
            d -> {
              DataTransferItem backup = null;
              for (Object f0 : d.items) {
                DataTransferItem f = (DataTransferItem) f0;
                if (mime.equals(f.type)) {
                  f.getAsString(
                      s -> {
                        cb.accept(s);
                      });
                  return;
                } else if (backup == null && "text/plain".equals(f.type)) {
                  backup = f;
                }
              }
              if (backup != null) {
                backup.getAsString(
                    s -> {
                      cb.accept(s);
                    });
              }
            });
  }

  @Override
  public void getString(Consumer<String> cb) {
    ((Promise<String>) $insert("navigator.clipboard.readText()")).then(cb);
  }

  @Override
  public void setString(String string) {
    $insert("(navigator.clipboard as any).writeText(string);");
  }
}
