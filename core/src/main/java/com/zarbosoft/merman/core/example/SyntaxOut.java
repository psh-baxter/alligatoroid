package com.zarbosoft.merman.core.example;

import com.zarbosoft.merman.core.syntax.Syntax;
import com.zarbosoft.merman.core.syntax.style.ModelColor;
import com.zarbosoft.rendaw.common.ROSetRef;

public class SyntaxOut {
  public final ModelColor choiceText;
  public final ModelColor choiceCursor;
  public final ModelColor choiceBg;
  public final Syntax syntax;
  public final ROSetRef<String> suffixOnPatternMismatch;

  public SyntaxOut(
      ModelColor choiceText,
      ModelColor choiceCursor,
      ModelColor choiceBg,
      Syntax syntax,
      ROSetRef<String> suffixOnPatternMismatch) {
    this.choiceText = choiceText;
    this.choiceCursor = choiceCursor;
    this.choiceBg = choiceBg;
    this.syntax = syntax;
    this.suffixOnPatternMismatch = suffixOnPatternMismatch;
  }
}
