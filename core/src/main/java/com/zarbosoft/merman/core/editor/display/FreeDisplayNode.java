package com.zarbosoft.merman.core.editor.display;

import com.zarbosoft.merman.core.editor.visual.Vector;

public interface FreeDisplayNode extends DisplayNode{
    void setPosition(Vector vector, boolean animate);
}
