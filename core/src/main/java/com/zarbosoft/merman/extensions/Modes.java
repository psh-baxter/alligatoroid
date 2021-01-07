package com.zarbosoft.merman.extensions;

import com.google.common.collect.ImmutableSet;
import com.zarbosoft.merman.editor.Action;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.tags.GlobalTag;
import com.zarbosoft.merman.editor.visual.tags.Tag;
import com.zarbosoft.merman.editor.visual.tags.TagsChange;
import com.zarbosoft.rendaw.common.Pair;

import java.util.List;
import java.util.stream.Collectors;

import static com.zarbosoft.rendaw.common.Common.enumerate;

public class Modes extends Extension {

  public List<String> states;

  @Override
  public State create(final ExtensionContext context) {
    return new ModuleState(context);
  }

  private abstract static class ActionBase extends Action {
    public static String group() {
      return "modes module";
    }
  }

  private class ModuleState extends State {
    private int state = 0;

    ModuleState(final Context context) {
      context.addActions(
          this,
          enumerate(states.stream())
              .map(
                  pair -> {
                    return new ActionMode(pair);
                  })
              .collect(Collectors.toList()));
      context.changeGlobalTags(new TagsChange(ImmutableSet.of(getTag(state)), ImmutableSet.of()));
    }

    private Tag getTag(final int state) {
      return new GlobalTag(String.format("mode_%s", states.get(state)));
    }

    @Override
    public void destroy(final ExtensionContext context) {
      context.changeGlobalTags(new TagsChange(ImmutableSet.of(), ImmutableSet.of(getTag(state))));
      context.removeActions(this);
    }

    @Action.StaticID(id = "mode_%s (%s = mode id)")
    private class ActionMode extends ActionBase {
      private final Pair<Integer, String> pair;

      public ActionMode(final Pair<Integer, String> pair) {
        this.pair = pair;
      }

      @Override
      public boolean run(final Context context) {
        context.changeGlobalTags(
            new TagsChange(ImmutableSet.of(getTag(pair.first)), ImmutableSet.of(getTag(state))));
        state = pair.first;
        return true;
      }

      @Override
      public String id() {
        return String.format("mode_%s", pair.second);
      }
    }
  }
}
