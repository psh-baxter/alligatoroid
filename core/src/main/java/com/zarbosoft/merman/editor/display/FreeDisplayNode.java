package com.zarbosoft.merman.editor.display;

import com.zarbosoft.merman.editor.visual.Vector;

public interface FreeDisplayNode extends DisplayNode{
    void setPosition(Vector vector, boolean animate);
}
