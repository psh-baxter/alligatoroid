package com.zarbosoft.merman.core.example;

import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.style.ModelColor;

public class SyntaxOut {
  public final ModelColor choiceText;
  public final ModelColor choiceCursor;
  public final ModelColor choiceBg;
  public final Syntax syntax;

  public SyntaxOut(
      ModelColor choiceText, ModelColor choiceCursor, ModelColor choiceBg, Syntax syntax) {
    this.choiceText = choiceText;
    this.choiceCursor = choiceCursor;
    this.choiceBg = choiceBg;
    this.syntax = syntax;
  }
}
