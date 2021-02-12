package com.zarbosoft.merman.extensions;

import com.zarbosoft.merman.editor.Action;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.tags.TagsChange;
import com.zarbosoft.rendaw.common.Format;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;
import com.zarbosoft.rendaw.common.TSSet;

import java.util.List;

public class ModesExtension {
  private final List<String> states;
  private int state = 0;

  public ModesExtension(Context context, List<String> states) {
    this.states = states;
    TSList<Action> actions = new TSList<>();
    for (int i = 0; i < states.size(); ++i) {
      actions.add(new ActionMode(new ROPair<>(i, states.get(i))));
    }
    context.addActions(actions);
    context.changeGlobalTags(TagsChange.add(getTag(state)));
  }

  private String getTag(final int state) {
    return Format.format("mode_%s", states.get(state));
  }

  private class ActionMode implements Action {
    private final ROPair<Integer, String> pair;

    public ActionMode(final ROPair<Integer, String> pair) {
      this.pair = pair;
    }

    @Override
    public void run(final Context context) {
      context.changeGlobalTags(
          new TagsChange(
              new TSSet<String>().add(getTag(pair.first)), new TSSet<String>().add(getTag(state))));
      state = pair.first;
    }

    @Override
    public String id() {
      return Format.format("mode_%s", pair.second);
    }
  }
}
