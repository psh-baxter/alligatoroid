package com.zarbosoft.merman.extensions;

import com.zarbosoft.merman.editor.Action;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.tags.TagsChange;
import com.zarbosoft.merman.misc.TSSet;
import com.zarbosoft.rendaw.common.Pair;

import java.util.List;
import java.util.stream.Collectors;

import static com.zarbosoft.rendaw.common.Common.enumerate;

public class ModesExtension {
  private final List<String> states;
  private int state = 0;

  public ModesExtension(Context context, List<String> states) {
    this.states = states;
    context.addActions(
        this,
        enumerate(states.stream())
            .map(
                pair -> {
                  return new ActionMode(pair);
                })
            .collect(Collectors.toList()));
    context.changeGlobalTags(TagsChange.add(getTag(state)));
  }

  private String getTag(final int state) {
    return String.format("mode_%s", states.get(state));
  }

  private class ActionMode extends Action {
    private final Pair<Integer, String> pair;

    public ActionMode(final Pair<Integer, String> pair) {
      this.pair = pair;
    }

    @Override
    public boolean run(final Context context) {
      context.changeGlobalTags(
          new TagsChange(
              new TSSet<String>().add(getTag(pair.first)), new TSSet<String>().add(getTag(state))));
      state = pair.first;
      return true;
    }

    @Override
    public String id() {
      return String.format("mode_%s", pair.second);
    }
  }
}
