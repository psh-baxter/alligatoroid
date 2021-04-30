package com.zarbosoft.pidgoon;

import com.zarbosoft.pidgoon.errors.GrammarTooUncertain;
import com.zarbosoft.pidgoon.errors.InvalidStream;
import com.zarbosoft.pidgoon.model.Grammar;
import com.zarbosoft.pidgoon.model.MismatchCause;
import com.zarbosoft.pidgoon.model.Parent;
import com.zarbosoft.pidgoon.model.Parse;
import com.zarbosoft.pidgoon.model.Position;
import com.zarbosoft.pidgoon.model.Store;
import com.zarbosoft.rendaw.common.ROPair;
import com.zarbosoft.rendaw.common.TSList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Pidgoon {
    public static Parse prepare(
        final Grammar grammar,
        final Object root,
        final Store initialStore,
        final int errorHistoryLimit,
        final int uncertaintyLimit,
        final boolean dumpAmbiguity) {
      final Parse context = new Parse(grammar, errorHistoryLimit, uncertaintyLimit, dumpAmbiguity);
      context.errorHistory = new ArrayList<>();
      grammar
          .getNode(root)
          .context(
              context,
              initialStore,
              new Parent() {
                @Override
                public void advance(final Parse step, final Store store, final MismatchCause cause) {
                    step.completed.add(store);
                }

                @Override
                public void error(final Parse step, final Store store, final MismatchCause cause) {
                  step.errors.add(cause);
                }

                @Override
                public long size(final Parent stopAt, final long start) {
                  throw new UnsupportedOperationException();
                }
              },
              null);
      return context;
    }

    /**
     * Advance the parse through the next position
     *
     * @param parse
     * @param position the next event to parse
     * @return parse after consuming current position, or null if EOF reached
     */
    public static Parse step(Parse parse, final Position position) {
      if (position.isEOF()) throw new RuntimeException("Cannot step; end of file reached.");

      if (parse.branches.isEmpty()) return null;

      final Parse nextStep = new Parse(parse);

      for (final Parse.Branch leaf : parse.branches) leaf.parse(nextStep, position);

      if (parse.errorHistoryLimit > 0) {
        if (nextStep.errors.isEmpty()) {
          nextStep.errorHistory = parse.errorHistory;
          if (nextStep.errorHistory == null) nextStep.errorHistory = new ArrayList<>();
        } else {
          nextStep.errorHistory = new ArrayList<>();
          nextStep.errorHistory.add(new ROPair<>(position, nextStep.errors));
          for (ROPair<Position, TSList<MismatchCause>> s : parse.errorHistory) {
            if (nextStep.errorHistory.size() >= parse.errorHistoryLimit) break;
            nextStep.errorHistory.add(s);
          }
        }
      }
      if (nextStep.ambiguityHistory != null) {
        int dupeCount = 0;
        final Set<String> unique = new HashSet<>();
        for (final Parse.Branch leaf : nextStep.branches) {
          if (unique.contains(leaf.toString())) {
            dupeCount += 1;
          } else {
            unique.add(leaf.toString());
          }
        }
        nextStep.ambiguityHistory =
            nextStep.ambiguityHistory.push(
                new AmbiguitySample(
                    nextStep.ambiguityHistory.top().step + 1,
                    nextStep.branches.size(),
                    position,
                    dupeCount));
      }
      if (nextStep.branches.size() > nextStep.uncertaintyLimit)
        throw new GrammarTooUncertain(nextStep, position);
      if (nextStep.branches.isEmpty() && nextStep.errors.size() == parse.branches.size())
        throw new InvalidStream(nextStep, position);

      return nextStep;
    }
}
