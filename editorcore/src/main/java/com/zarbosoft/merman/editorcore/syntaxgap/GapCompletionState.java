package com.zarbosoft.merman.editorcore.syntaxgap;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.gap.GapCompletionEngine;
import com.zarbosoft.merman.editor.gap.TwoColumnChoice;
import com.zarbosoft.pidgoon.Grammar;
import com.zarbosoft.pidgoon.bytes.Position;
import com.zarbosoft.pidgoon.parse.Parse;
import com.zarbosoft.rendaw.common.Pair;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GapCompletionState implements GapCompletionEngine.State {
  private final List<SyntacticGapChoice> choices = new ArrayList<>();
  public Grammar grammar;
  public String currentText;

  public GapCompletionState() {
  }

  @Override
  public void update(Context context, String string) {
    // If the whole text matches, try to auto complete
    // Display info on matches and not-yet-mismatches
    this.currentText = string;
    choices.clear();
    final Pair<Parse, Position> longest = new com.zarbosoft.pidgoon.bytes.ParseBuilder<>()
      .grammar(grammar)
      .longestMatchFromStart(new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)));
    for (Object result : longest.first.results) {
      choices.add((SyntacticGapChoice) result);
    }
    for (com.zarbosoft.pidgoon.State leaf : longest.first.leaves) {
      choices.add(leaf.color());
    }
    if (longest.second.absolute == string.length()) {
      for (final SyntacticGapChoice choice : choices) {
        if (choices.size() <= choice.type.autoChooseAmbiguity) {
          choice.choose(context);
          choices.clear();
          return;
        }
      }
    } else if (longest.second.absolute >= 1) {
      // When the text stops matching (new element started?) go ahead and choose a
      // previous choice
      for (final SyntacticGapChoice choice : choices) {
        choice.choose(context);
        choices.clear();
        return;
      }
    }
  }

  @Override
  public List<TwoColumnChoice> choices() {
    return (List<TwoColumnChoice>) (List) choices;
  }
}
