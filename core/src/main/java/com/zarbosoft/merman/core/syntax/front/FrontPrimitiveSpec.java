package com.zarbosoft.merman.core.syntax.front;

import com.zarbosoft.merman.core.Context;
import com.zarbosoft.merman.core.SyntaxPath;
import com.zarbosoft.merman.core.document.Atom;
import com.zarbosoft.merman.core.misc.MultiError;
import com.zarbosoft.merman.core.syntax.AtomType;
import com.zarbosoft.merman.core.syntax.back.BaseBackPrimitiveSpec;
import com.zarbosoft.merman.core.syntax.style.Style;
import com.zarbosoft.merman.core.visual.Visual;
import com.zarbosoft.merman.core.visual.VisualParent;
import com.zarbosoft.merman.core.visual.visuals.VisualFrontPrimitive;
import com.zarbosoft.rendaw.common.TSSet;

public class FrontPrimitiveSpec extends FrontSpec {
  public final String fieldId;
  public final Style firstStyle;
  public final Style hardStyle;
  public final Style softStyle;
  public final Style.SplitMode splitMode;
  public BaseBackPrimitiveSpec field;

  public FrontPrimitiveSpec(Config config) {
    fieldId = config.fieldId;
    if (config.firstStyle == null) firstStyle = new Style.Config().create();
    else firstStyle = config.firstStyle;
    if (config.hardStyle == null) hardStyle = new Style.Config().create();
    else hardStyle = config.hardStyle;
    if (config.softStyle == null) softStyle = new Style.Config().create();
    else softStyle = config.softStyle;
    splitMode = config.splitMode;
  }

  @Override
  public Visual createVisual(
      final Context context,
      final VisualParent parent,
      final Atom atom,
      final int visualDepth,
      final int depthScore) {
    return new VisualFrontPrimitive(context, parent, this, field.get(atom.fields), visualDepth);
  }

  @Override
  public void finish(
      MultiError errors,
      SyntaxPath typePath,
      final AtomType atomType,
      final TSSet<String> middleUsed) {
    middleUsed.add(fieldId);
    this.field = atomType.getDataPrimitive(errors, typePath, fieldId);
  }

  @Override
  public String fieldId() {
    return fieldId;
  }

  @Override
  public void dispatch(final DispatchHandler handler) {
    handler.handle(this);
  }

  public static class Config {
    public final String fieldId;
    public Style.SplitMode splitMode = Style.SplitMode.NEVER;
    /** First line (highest priority) */
    public Style firstStyle;
    /** Hard new line */
    public Style hardStyle;
    /** Soft new line */
    public Style softStyle;

    public Config(String fieldId) {
      this.fieldId = fieldId;
    }

    public Config firstStyle(Style c) {
      firstStyle = c;
      return this;
    }

    public Config hardStyle(Style c) {
      hardStyle = c;
      return this;
    }

    public Config softStyle(Style c) {
      softStyle = c;
      return this;
    }

    public Config style(Style c) {
      softStyle = c;
      hardStyle = c;
      firstStyle = c;
      return this;
    }

    public Config splitMode(Style.SplitMode splitMode) {
      this.splitMode = splitMode;
      return this;
    }
  }
}
