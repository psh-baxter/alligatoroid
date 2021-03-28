package com.zarbosoft.merman.webview.display;

import com.zarbosoft.merman.core.editor.display.FreeDisplayNode;
import com.zarbosoft.merman.core.editor.visual.Vector;
import elemental2.dom.HTMLElement;

public abstract class JSFreeDisplayNode extends JSDisplayNode implements FreeDisplayNode {
    double converse;
    double transverse;

    protected JSFreeDisplayNode(JSDisplay display, HTMLElement element) {
        super(display, element);
    }

    @Override
    public double converse() {
        return converse;
    }

    @Override
    public double transverse() {
        return transverse;
    }

    @Override
    public void setConverse(double converse, boolean animate) {
        this.converse = converse;
        fixPosition(animate);
    }

    @Override
    public void setPosition(Vector vector, boolean animate) {
        this.converse = vector.converse;
        this.transverse = vector.transverse;
        fixPosition(animate);
    }


    @Override
    protected double transverseCorner() {
        return transverse;
    }

    @Override
    protected double converseCorner() {
        return converse;
    }
}
