package com.zarbosoft.merman.editor.display;

import com.zarbosoft.merman.syntax.style.ModelColor;

public interface DrawingContext {
    void setLineColor(ModelColor color);

    void setLineCapRound();

    void setLineThickness(double lineThickness);

    void setLineCapFlat();

    void setFillColor(ModelColor color);

    void beginStrokePath();

    void beginFillPath();

    void moveTo(int c, int t);

    void lineTo(int c, int t);

    void closePath();

    void arcTo(int c, int t, int c2, int t2, int radius);

    void translate(int c, int t);
}
