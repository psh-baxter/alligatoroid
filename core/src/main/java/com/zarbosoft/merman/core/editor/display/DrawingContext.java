package com.zarbosoft.merman.core.editor.display;

import com.zarbosoft.merman.core.syntax.style.ModelColor;

public interface DrawingContext {
    void setLineColor(ModelColor color);

    void setLineCapRound();

    void setLineThickness(double lineThickness);

    void setLineCapFlat();

    void setFillColor(ModelColor color);

    void beginStrokePath();

    void beginFillPath();

    void moveTo(double c, double t);

    void lineTo(double c, double t);

    void closePath();

    void arcTo(double c, double t, double c2, double t2, double radius);

    void translate(double c, double t);
}
