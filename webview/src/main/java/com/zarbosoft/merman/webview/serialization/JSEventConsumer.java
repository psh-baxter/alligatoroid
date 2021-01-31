package com.zarbosoft.merman.webview.serialization;

import com.zarbosoft.merman.editor.serialization.EventConsumer;

public interface JSEventConsumer extends EventConsumer {
    byte[] resultOne();
    byte[] resultMany();
}
